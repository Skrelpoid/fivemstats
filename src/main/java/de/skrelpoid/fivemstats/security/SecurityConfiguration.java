package de.skrelpoid.fivemstats.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.vaadin.flow.spring.security.VaadinWebSecurity;

import de.skrelpoid.fivemstats.views.login.LoginView;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
    	
    	http.rememberMe(Customizer.withDefaults());

        http.authorizeHttpRequests(atr -> atr.requestMatchers(new AntPathRequestMatcher("/images/*.png")).permitAll());
        http.authorizeHttpRequests(atr -> atr.requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll());

        // Icons from the line-awesome addon
        http.authorizeHttpRequests(atr -> atr.requestMatchers(new AntPathRequestMatcher("/line-awesome/**/*.svg")).permitAll());
        
        super.configure(http);
        setLoginView(http, LoginView.class);
    }

}
