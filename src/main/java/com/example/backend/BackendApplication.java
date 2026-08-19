package com.example.backend;

import com.example.backend.config.DotenvInitializer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class BackendApplication {

	public static void main(String[] args) {

		new SpringApplicationBuilder(BackendApplication.class)
				.initializers(new DotenvInitializer()) // 🔥 important
				.run(args);
	}
}