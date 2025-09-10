package br.com.voting_system_vote_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import org.springframework.cloud.client.loadbalancer.LoadBalanced; 


/**
 * @author fsdney
 */


@Configuration
public class AppConfig {

    @Bean
    @LoadBalanced 
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
