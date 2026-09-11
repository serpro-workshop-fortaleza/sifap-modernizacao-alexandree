package br.gov.sifap.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Relogio injetavel: REQ-011 deriva a competencia da data corrente e os testes precisam fixa-la. */
@Configuration
class ClockConfiguration {

    @Bean
    Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}
