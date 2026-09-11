package br.gov.sifap.payment.api;

import br.gov.sifap.sharedkernel.domain.Competence;
import java.util.List;
import java.util.Optional;

/** Consultas do contexto Pagamento de Beneficios expostas pela camada web. */
public interface PaymentQuery {

    List<PaymentView> list(Optional<Competence> competence, int limit);

    Optional<PaymentView> findByNumber(Long paymentNumber);

    /**
     * Resultado da ultima execucao registrada da competencia. Sem execucao registrada,
     * o resumo e agregado a partir dos pagamentos gravados e {@code finishedAt} fica nulo.
     */
    PayrollResult currentSummary(Optional<Competence> competence);

    List<PayrollHistoryPoint> history();
}
