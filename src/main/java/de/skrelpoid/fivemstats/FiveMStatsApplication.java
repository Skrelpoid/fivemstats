package de.skrelpoid.fivemstats;

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationProperties;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import de.skrelpoid.fivemstats.data.service.SamplePersonRepository;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@Theme(value = "fivemstats", variant = Lumo.DARK)
public class FiveMStatsApplication implements AppShellConfigurator {

	private static final long serialVersionUID = 1L;

	public static void main(final String[] args) {
        SpringApplication.run(FiveMStatsApplication.class, args);
    }

    @Bean
    SqlDataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer(final DataSource dataSource,
            final SqlInitializationProperties properties, final SamplePersonRepository repository) {
        // This bean ensures the database is only initialized when empty
        return new SqlDataSourceScriptDatabaseInitializer(dataSource, properties) {
            @Override
            public boolean initializeDatabase() {
                if (repository.count() == 0L) {
                    return super.initializeDatabase();
                }
                return false;
            }
        };
    }
    
    @Bean
    WebTarget webTarget() {
    	final Client client = ClientBuilder.newClient(); // NOSONAR close on shutdown
    	return client.target("http://cl-rp.de:30120/players.json");
    }
    
    @Bean
    ObjectMapper objectMapper() {
    	return new ObjectMapper();
    }
}
