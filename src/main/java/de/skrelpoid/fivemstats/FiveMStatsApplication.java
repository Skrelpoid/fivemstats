package de.skrelpoid.fivemstats;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets and some desktop
 * browsers.
 *
 */
@SpringBootApplication
@Theme(value = "fivemstats", variant = Lumo.DARK)
@Push
public class FiveMStatsApplication implements AppShellConfigurator {

	private static final long serialVersionUID = 1L;

	public static void main(final String[] args) {
		SpringApplication.run(FiveMStatsApplication.class, args);
	}

	@Bean
	WebTarget webTarget(@Value("${fivemstats.target.hostname}") final String server,
			@Value("${fivemstats.target.port:30120}") final Integer port) {
		final Client client = ClientBuilder.newClient(); // NOSONAR close on shutdown
		return client.target("http://" + server + ":" + port + "/players.json");
	}

	@Bean
	ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
}
