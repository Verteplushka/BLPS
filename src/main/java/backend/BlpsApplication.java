package backend;

import backend.Controllers.ApplicationController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackageClasses = ApplicationController.class)
public class BlpsApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlpsApplication.class, args);
	}

}
