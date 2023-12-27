package scrips.dso.util.jirautil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "scrips.dso.*")
public class JiraUtilApplication {

	public static void main(String[] args) {
		SpringApplication.run(JiraUtilApplication.class, args);

	}

}
