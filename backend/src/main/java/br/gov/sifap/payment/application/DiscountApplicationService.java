package br.gov.sifap.payment.application;

import br.gov.sifap.audit.api.AuditEntry;
import br.gov.sifap.audit.api.AuditTrail;
import br.gov.sifap.payment.api.DiscountApplication;
import br.gov.sifap.payment.api.PaymentQuery;
import br.gov.sifap.payment.api.PaymentView;
import br.gov.sifap.payment.domain.DiscountPolicy;
import br.gov.sifap.payment.domain.PaymentDiscount;
import br.gov.sifap.payment.infrastructure.PaymentRepository;
import br.gov.sifap.sharedkernel.domain.Money;
import br.gov.sifap.sharedkernel.error.ResourceNotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DiscountApplicationService implements DiscountApplication {

    private static final String UPDATE_AUDIT_ACTION = "AL";

    private final PaymentRepository paymentRepository;
    private final PaymentQuery paymentQuery;
    private final DiscountPolicy discountPolicy;
    private final AuditTrail auditTrail;
    private final Clock clock;

    DiscountApplicationService(
            PaymentRepository paymentRepository,
            PaymentQuery paymentQuery,
            DiscountPolicy discountPolicy,
            AuditTrail auditTrail,
            Clock clock) {
        this.paymentRepository = paymentRepository;
        this.paymentQuery = paymentQuery;
        this.discountPolicy = discountPolicy;
        this.auditTrail = auditTrail;
        this.clock = clock;
    }

    @Override
    @Transactional
    public PaymentView apply(Long paymentNumber, List<DiscountRequest> requested) {
        var payment = paymentRepository.findById(paymentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento nao encontrado: " + paymentNumber));

        var assessment = discountPolicy.assess(
                Money.of(payment.grossAmount()),
                requested.stream()
                        .map(request -> new DiscountPolicy.Request(
                                request.type(), request.amount(), request.percentage(), request.caseNumber()))
                        .toList());

        var discounts = requested.stream()
                .map(request -> new PaymentDiscount(
                        request.type(), request.amount().amount(), request.percentage(), request.caseNumber()))
                .toList();

        payment.replaceDiscounts(discounts, assessment.totalApplied().amount());
        paymentRepository.save(payment);

        // REQ-014
        auditTrail.record(new AuditEntry(
                UPDATE_AUDIT_ACTION,
                "payment",
                "Payment",
                String.valueOf(paymentNumber),
                "api",
                LocalDateTime.now(clock)));

        return paymentQuery.findByNumber(paymentNumber)
                .orElseThrow(() -> new IllegalStateException("Pagamento recem-gravado nao pode desaparecer"));
    }
}
