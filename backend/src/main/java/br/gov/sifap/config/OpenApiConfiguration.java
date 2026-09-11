package br.gov.sifap.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfiguration {

    @Bean
    OpenAPI sifapOpenApi() {
        return new OpenAPI().info(new Info()
                .title("SIFAP 2.0 — API")
                .version("v1")
                .description("""
                        Contexto delimitado 3 (Pagamento de Benefícios) com as superfícies de leitura
                        dos contextos 1 (Cadastro de Beneficiários) e 2 (Catálogo de Programas Sociais).

                        Cada operação cita os REQ-IDs de specs/001-benefit-calculation/spec.md que atende.
                        """)
                .license(new License().name("Uso interno da imersão")));
    }
}
