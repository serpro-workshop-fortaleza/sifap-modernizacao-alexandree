package br.gov.sifap.beneficiary.domain;

import br.gov.sifap.beneficiary.api.BeneficiaryStatus;
import br.gov.sifap.beneficiary.api.BeneficiarySnapshot;
import br.gov.sifap.beneficiary.api.BeneficiarySummary;
import br.gov.sifap.sharedkernel.domain.Cpf;
import br.gov.sifap.sharedkernel.domain.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Origem: BENEFIC.ddm (FNR 150). Somente leitura nesta feature. */
@Entity
@Table(name = "beneficiary")
public class Beneficiary {

    @Id
    @Column(name = "registration_number", nullable = false)
    private Long registrationNumber;

    @Column(name = "cpf", nullable = false, length = 11)
    private String cpf;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Column(name = "program_code", nullable = false, length = 4)
    private String programCode;

    @Column(name = "region_code", nullable = false, length = 2)
    private String regionCode;

    @Column(name = "household_income", nullable = false, precision = 15, scale = 2)
    private BigDecimal householdIncome;

    @Column(name = "dependent_count", nullable = false)
    private Integer dependentCount;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "registered_at", nullable = false)
    private LocalDate registeredAt;

    protected Beneficiary() {
    }

    public BeneficiarySnapshot toSnapshot() {
        return new BeneficiarySnapshot(
                Cpf.of(cpf),
                registrationNumber,
                BeneficiaryStatus.fromCode(status),
                Money.of(householdIncome),
                birthDate,
                dependentCount,
                programCode,
                regionCode);
    }

    public BeneficiarySummary toSummary() {
        return new BeneficiarySummary(
                registrationNumber,
                Cpf.of(cpf),
                name,
                BeneficiaryStatus.fromCode(status),
                programCode,
                Money.of(householdIncome),
                registeredAt);
    }
}
