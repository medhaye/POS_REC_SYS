package com.smartmssa.pos_recsys;

import com.smartmssa.pos_recsys.service.SPMFAnalyzer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PosRecSysApplication {

	public static void main(String[] args) {
		SpringApplication.run(PosRecSysApplication.class, args);
	}

//	@Bean
//	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
//		return args -> {
//			SPMFAnalyzer analyzer = ctx.getBean(SPMFAnalyzer.class);
//			// Set your desired values for minSupport and confidence here
//			analyzer.setMinSupport(0.0001);  // Here you can adjust the Minimum Support.
//			analyzer.setConfidence(0.1);   // Here you can adjust the confidence.
//			analyzer.init();  // Run the analysis with the updated parameters
//		};
//	}
}
