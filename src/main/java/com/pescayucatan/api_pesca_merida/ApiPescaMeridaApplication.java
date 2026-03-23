package com.pescayucatan.api_pesca_merida;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // ⚠️ CRÍTICO: Habilita @Scheduled en IngestionServic
public class ApiPescaMeridaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiPescaMeridaApplication.class, args);
	}

}
