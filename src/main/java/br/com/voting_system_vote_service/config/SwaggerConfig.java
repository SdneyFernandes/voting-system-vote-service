package br.com.voting_system_vote_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author fsdney
 */

@Configuration
public class SwaggerConfig {

 @Bean
 public OpenAPI votingServiceOpenAPI() {
     return new OpenAPI()
             .info(new Info()
                     .title("API - Vote Service")
                     .version("1.0.0")
                     .description("Microserviço responsável por gerenciamento de votos e sessões de votação.")
                     .contact(new Contact()
                             .name("Equipe de Desenvolvimento")
                             .email("contato@votingsystem.com")
                             .url("https://github.com/fsdney"))
                     .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT"))
             );
 }
}
