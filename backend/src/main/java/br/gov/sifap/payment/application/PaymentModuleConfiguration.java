package br.gov.sifap.payment.application;

import br.gov.sifap.payment.domain.BenefitCalculator;
import br.gov.sifap.payment.domain.DiscountPolicy;
import br.gov.sifap.payment.domain.EligibilityPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Politicas de dominio do modulo payment; sao objetos sem estado e sem dependencia de Spring. */
@Configuration
class PaymentModuleConfiguration {

    @Bean
    EligibilityPolicy eligibilityPolicy() {
        return new EligibilityPolicy();
    }

    @Bean
    BenefitCalculator benefitCalculator() {
        return BenefitCalculator.withConfirmedFactorsOnly();
    }

    @Bean
    DiscountPolicy discountPolicy() {
        return new DiscountPolicy();
    }
}
