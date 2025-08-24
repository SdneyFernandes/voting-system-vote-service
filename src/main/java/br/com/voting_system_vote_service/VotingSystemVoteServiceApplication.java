package br.com.voting_system_vote_service;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDiscoveryClient  
@SpringBootApplication
public class VotingSystemVoteServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(VotingSystemVoteServiceApplication.class, args);
	}

}
