package br.gov.sifap.payment.domain;

import br.gov.sifap.sharedkernel.domain.Competence;
import br.gov.sifap.sharedkernel.domain.Cpf;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @Column(name = "payment_number", nullable = false)
    private Long paymentNumber;

    @Column(name = "cpf", nullable = false, length = 11)
    private String cpf;

    @Column(name = "registration_number", nullable = false)
    private Long registrationNumber;

    @Column(name = "program_code", nullable = false, length = 4)
    private String programCode;

    @Column(name = "competence", nullable = false, length = 6)
    private String competence;

    @Column(name = "cycle", nullable = false)
    private Integer cycle;

    @Column(name = "gross_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "net_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "discount_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountTotal;

    @Column(name = "bonus_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal bonusAmount;

    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Column(name = "generated_at", nullable = false)
    private LocalDate generatedAt;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PaymentDiscount> discounts = new ArrayList<>();

    protected Payment() {
    }

    public Payment(
            Long paymentNumber,
            Cpf cpf,
            Long registrationNumber,
            String programCode,
            Competence competence,
            Integer cycle,
            BigDecimal grossAmount,
            BigDecimal netAmount,
            BigDecimal discountTotal,
            BigDecimal bonusAmount,
            String status) {
        this(paymentNumber, cpf, registrationNumber, programCode, competence, cycle,
                grossAmount, netAmount, discountTotal, bonusAmount, status, LocalDate.now());
    }

    public Payment(
            Long paymentNumber,
            Cpf cpf,
            Long registrationNumber,
            String programCode,
            Competence competence,
            Integer cycle,
            BigDecimal grossAmount,
            BigDecimal netAmount,
            BigDecimal discountTotal,
            BigDecimal bonusAmount,
            String status,
            LocalDate generatedAt) {
        this.paymentNumber = Objects.requireNonNull(paymentNumber, "Numero do pagamento nao pode ser nulo");
        this.cpf = Objects.requireNonNull(cpf, "CPF nao pode ser nulo").value();
        this.registrationNumber = Objects.requireNonNull(registrationNumber, "Numero de inscricao nao pode ser nulo");
        this.programCode = Objects.requireNonNull(programCode, "Codigo do programa nao pode ser nulo");
        this.competence = Objects.requireNonNull(competence, "Competencia nao pode ser nula").toString();
        this.cycle = Objects.requireNonNull(cycle, "Ciclo nao pode ser nulo");
        this.grossAmount = requireAmount(grossAmount, "Valor bruto");
        this.netAmount = requireAmount(netAmount, "Valor liquido");
        this.discountTotal = requireAmount(discountTotal, "Total de descontos");
        this.bonusAmount = requireAmount(bonusAmount, "Valor do bonus");
        this.status = Objects.requireNonNull(status, "Status nao pode ser nulo");
        this.generatedAt = Objects.requireNonNull(generatedAt, "Data de geracao nao pode ser nula");
    }

    public void addDiscount(PaymentDiscount discount) {
        Objects.requireNonNull(discount, "Desconto nao pode ser nulo");
        if (discounts.size() >= 8) {
            throw new IllegalArgumentException("Pagamento nao pode possuir mais de 8 descontos");
        }
        discounts.add(discount.attachTo(this));
    }

    /** O total pode ser menor que a soma das ocorrencias quando o teto de REQ-007 e aplicado. */
    public void replaceDiscounts(List<PaymentDiscount> newDiscounts, BigDecimal appliedTotal) {
        Objects.requireNonNull(newDiscounts, "Descontos nao podem ser nulos");
        discounts.clear();
        newDiscounts.forEach(this::addDiscount);
        this.discountTotal = requireAmount(appliedTotal, "Total de descontos");
        this.netAmount = requireAmount(grossAmount.subtract(this.discountTotal).max(BigDecimal.ZERO), "Valor liquido");
    }

    public Long paymentNumber() {
        return paymentNumber;
    }

    public Cpf cpf() {
        return Cpf.of(cpf);
    }

    public Competence competence() {
        return Competence.of(competence);
    }

    public String programCode() {
        return programCode;
    }

    public List<PaymentDiscount> discounts() {
        return List.copyOf(discounts);
    }

    public BigDecimal grossAmount() {
        return grossAmount;
    }

    public BigDecimal discountTotal() {
        return discountTotal;
    }

    public BigDecimal bonusAmount() {
        return bonusAmount;
    }

    public BigDecimal netAmount() {
        return netAmount;
    }

    public LocalDate generatedAt() {
        return generatedAt;
    }

    public String status() {
        return status;
    }

    private static BigDecimal requireAmount(BigDecimal amount, String field) {
        return Objects.requireNonNull(amount, field + " nao pode ser nulo").setScale(2, java.math.RoundingMode.DOWN);
    }
}
