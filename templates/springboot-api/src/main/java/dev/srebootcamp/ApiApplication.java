package ${group}.${packageDetails.name};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ${packageDetails.name|toFirstUpper}Application {
	public static void main(String[] args) {
		SpringApplication.run(${packageDetails.name|toFirstUpper}Application.class, args);
	}
}