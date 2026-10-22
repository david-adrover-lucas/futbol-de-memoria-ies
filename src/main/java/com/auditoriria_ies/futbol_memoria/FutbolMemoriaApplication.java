package com.auditoriria_ies.futbol_memoria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FutbolMemoriaApplication {

	public static void main(String[] args) {
		SpringApplication.run(FutbolMemoriaApplication.class, args);
	}

}
