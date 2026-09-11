package br.gov.sifap.payment.application;

import br.gov.sifap.beneficiary.api.BeneficiaryQuery;
import br.gov.sifap.beneficiary.api.BeneficiarySummary;
import br.gov.sifap.payment.api.PaymentQuery;
import br.gov.sifap.payment.api.PaymentView;
import br.gov.sifap.payment.api.PayrollHistoryPoint;
import br.gov.sifap.payment.api.PayrollResult;
import br.gov.sifap.payment.domain.Payment;
import br.gov.sifap.payment.domain.PaymentDiscount;
import br.gov.sifap.payment.domain.PayrollRun;
import br.gov.sifap.payment.infrastructure.PaymentRepository;
import br.gov.sifap.payment.infrastructure.PayrollRunRepository;
import br.gov.sifap.sharedkernel.domain.Competence;
import br.gov.sifap.sharedkernel.domain.Money;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class PaymentQueryService implements PaymentQuery {

    private final PaymentRepository paymentRepository;
    private final PayrollRunRepository payrollRunRepository;
    private final BeneficiaryQuery beneficiaryQuery;
    private final Clock clock;

    PaymentQueryService(
            PaymentRepository paymentRepository,
            PayrollRunRepository payrollRunRepository,
            BeneficiaryQuery beneficiaryQuery,
            Clock clock) {
        this.paymentRepository = paymentRepository;
        this.payrollRunRepository = payrollRunRepository;
        this.beneficiaryQuery = beneficiaryQuery;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentView> list(Optional<Competence> competence, int limit) {
        var payments = competence
                .map(value -> paymentRepository.findByCompetenceOrderByPaymentNumberAsc(value.toString()))
                .orElseGet(() -> paymentRepository.findAllByOrderByCompetenceDescPaymentNumberDesc(
                        PageRequest.of(0, limit)));

        var namesByCpf = resolveNames(payments);
        return payments.stream().map(payment -> toView(payment, namesByCpf)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentView> findByNumber(Long paymentNumber) {
        return paymentRepository.findById(paymentNumber)
                .map(payment -> toView(payment, resolveNames(List.of(payment))));
    }

    /**
     * O resumo descreve a <em>competencia</em>, nao a ultima tentativa: emitidos e totais vem
     * dos pagamentos gravados, enquanto rejeitados, ignorados e a data de termino vem da ultima
     * execucao registrada. Sem isso, reexecutar a folha ja processada zeraria o painel, porque
     * a guarda de REQ-010 ignora todo mundo na segunda passada.
     */
    @Override
    @Transactional(readOnly = true)
    public PayrollResult currentSummary(Optional<Competence> competence) {
        var resolved = competence.orElseGet(() -> Competence.current(clock));
        var lastRun = payrollRunRepository.findFirstByCompetenceOrderByFinishedAtDesc(resolved.toString())
                .map(PayrollRun::toResult);

        var totals = paymentRepository.aggregateByCompetence(resolved.toString());
        var issued = totals.map(view -> (int) view.getPaymentCount()).orElse(0);
        var gross = totals.map(view -> Money.of(view.getGrossTotal())).orElse(Money.ZERO);
        var net = totals.map(view -> Money.of(view.getNetTotal())).orElse(Money.ZERO);
        var rejected = lastRun.map(PayrollResult::paymentsRejected).orElse(0);

        return new PayrollResult(
                resolved,
                issued,
                rejected,
                lastRun.map(PayrollResult::ignoredBeneficiaries).orElse(0),
                gross,
                net,
                PayrollResult.resolveReturnCode(issued, rejected),
                lastRun.map(PayrollResult::finishedAt).orElse(null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollHistoryPoint> history() {
        return paymentRepository.aggregateByCompetence().stream()
                .map(view -> new PayrollHistoryPoint(
                        Competence.of(view.getCompetence()),
                        Money.of(view.getNetTotal()),
                        view.getPaymentCount()))
                .toList();
    }

    private Map<String, String> resolveNames(List<Payment> payments) {
        var cpfs = payments.stream().map(Payment::cpf).distinct().toList();
        return beneficiaryQuery.findSummariesByCpf(cpfs).stream()
                .collect(Collectors.toMap(
                        summary -> summary.cpf().value(),
                        BeneficiarySummary::name,
                        (first, second) -> first));
    }

    private static PaymentView toView(Payment payment, Map<String, String> namesByCpf) {
        var cpf = payment.cpf();
        return new PaymentView(
                payment.paymentNumber(),
                cpf,
                namesByCpf.getOrDefault(cpf.value(), "Beneficiario nao localizado"),
                payment.programCode(),
                payment.competence(),
                Money.of(payment.grossAmount()),
                Money.of(payment.discountTotal()),
                Money.of(payment.netAmount()),
                payment.status(),
                payment.generatedAt(),
                payment.discounts().stream().map(PaymentQueryService::toDiscountView).toList());
    }

    private static PaymentView.DiscountView toDiscountView(PaymentDiscount discount) {
        return new PaymentView.DiscountView(
                discount.discountType(), Money.of(discount.amount()), discount.isJudicial());
    }
}
