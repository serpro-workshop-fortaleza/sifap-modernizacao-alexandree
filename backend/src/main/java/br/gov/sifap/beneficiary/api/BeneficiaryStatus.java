package br.gov.sifap.beneficiary.api;

import java.util.Arrays;

/** Dominio de STAT-BENEFICIARY em BENEFIC.ddm:L72; motivos derivados de VALELEG.NSN:L133-L151. */
public enum BeneficiaryStatus {
    ACTIVE("A"),
    SUSPENDED("S"),
    CANCELLED("C"),
    TERMINATED("D"),
    INACTIVE("I");

    private final String code;

    BeneficiaryStatus(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public static BeneficiaryStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(status -> status.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Situacao cadastral desconhecida: " + code));
    }
}
