package br.gov.sifap.socialprogram.domain;

import br.gov.sifap.sharedkernel.domain.Competence;
import br.gov.sifap.sharedkernel.domain.Money;
import br.gov.sifap.socialprogram.api.IncomeBand;
import br.gov.sifap.socialprogram.api.ProgramParameters;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Origem: SOCPROG.ddm (FNR 151). Somente leitura nesta feature. */
@Entity
@Table(name = "social_program")
public class SocialProgram {

    @Id
    @Column(name = "code", nullable = false, length = 4)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Column(name = "base_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "max_income", nullable = false, precision = 15, scale = 2)
    private BigDecimal maxIncome;

    @Column(name = "adjustment_factor", nullable = false, precision = 9, scale = 6)
    private BigDecimal adjustmentFactor;

    @Column(name = "valid_from", nullable = false, length = 6)
    private String validFrom;

    @Column(name = "valid_to", length = 6)
    private String validTo;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "program_code", nullable = false)
    @OrderBy("bandOrder ASC")
    private List<SocialProgramIncomeBand> incomeBands = new ArrayList<>();

    protected SocialProgram() {
    }

    public boolean isActiveOn(Competence competence) {
        if (!"A".equals(status)) {
            return false;
        }
        var reference = competence.toString();
        if (validFrom.compareTo(reference) > 0) {
            return false;
        }
        return validTo == null || validTo.compareTo(reference) >= 0;
    }

    public ProgramParameters toParameters(boolean active) {
        var bands = incomeBands.stream()
                .map(band -> new IncomeBand(Money.of(band.ceiling()), band.factor()))
                .toList();
        return new ProgramParameters(
                code, name, active, Money.of(baseAmount), Money.of(maxIncome), bands, adjustmentFactor);
    }

    public ProgramParameters toParameters() {
        return toParameters("A".equals(status));
    }
}
