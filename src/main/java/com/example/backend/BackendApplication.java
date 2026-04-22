package com.example.backend;
import com.example.backend.config.DotenvInitializer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {

		new SpringApplicationBuilder(BackendApplication.class)
				.initializers(new DotenvInitializer()) // 🔥 important
				.run(args);
	}
}