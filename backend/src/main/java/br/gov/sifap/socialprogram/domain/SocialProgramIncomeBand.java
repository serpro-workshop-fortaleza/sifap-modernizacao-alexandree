package br.gov.sifap.socialprogram.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** Ocorrencia do grupo periodico de faixas de calculo; band_order preserva a ordem do PE. */
@Entity
@Table(name = "social_program_income_band")
public class SocialProgramIncomeBand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // program_code e mapeada pelo @JoinColumn do lado de SocialProgram; repeti-la aqui duplicaria a coluna.
    @Column(name = "band_order", nullable = false)
    private Integer bandOrder;

    @Column(name = "ceiling", nullable = false, precision = 15, scale = 2)
    private BigDecimal ceiling;

    @Column(name = "factor", nullable = false, precision = 9, scale = 6)
    private BigDecimal factor;

    protected SocialProgramIncomeBand() {
    }

    public BigDecimal ceiling() {
        return ceiling;
    }

    public BigDecimal factor() {
        return factor;
    }
}
