package br.gov.sifap.payment.infrastructure.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Traducao provisoria do codigo de situacao gravado em PAYMENT para o vocabulario da API.
 *
 * <p>A questao de projeto P3 permanece aberta: existem tres vocabularios divergentes para o
 * mesmo campo (PAYMENT.ddm:L73-L75, CALCBENF.NSN:L308-L320, BATCHREL.NSP:L177-L190). Enquanto
 * a maquina de estados nao for definida, um codigo desconhecido e reportado como PENDENTE,
 * que e o unico estado que nao afirma desfecho bancario algum.
 */
final class PaymentStatusView {

    private static final Logger log = LoggerFactory.getLogger(PaymentStatusView.class);

    private PaymentStatusView() {
    }

    static String of(String legacyCode) {
        return switch (legacyCode) {
            case "G" -> "PENDENTE";
            case "P" -> "PAGO";
            case "R" -> "REJEITADO";
            case "E" -> "ESTORNADO";
            default -> {
                log.warn("codigo de situacao de pagamento fora do dominio conhecido codigo={}", legacyCode);
                yield "PENDENTE";
            }
        };
    }
}
