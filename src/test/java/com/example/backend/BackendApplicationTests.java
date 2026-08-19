package com.example.backend;

import com.example.backend.config.DotenvInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(initializers = DotenvInitializer.class)
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
