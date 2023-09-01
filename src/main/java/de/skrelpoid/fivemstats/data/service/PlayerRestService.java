package de.skrelpoid.fivemstats.data.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.skrelpoid.fivemstats.data.entity.Player;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Service
public class PlayerRestService {

	private static final Logger logger = LoggerFactory.getLogger(PlayerRestService.class);

	private final WebTarget webTarget;

	private final ObjectMapper objectMapper;

	public PlayerRestService(final WebTarget webTarget, final ObjectMapper objectMapper) {
		this.webTarget = webTarget;
		this.objectMapper = objectMapper;
	}

	public List<Player> queryPlayers() {
		try {
			final Response response =
					webTarget.request(MediaType.APPLICATION_JSON).get(Response.class);
			if (response.getStatus() == 200) {
				final String rawJson = response.readEntity(String.class);
				logger.debug(rawJson);
				final Player[] players = objectMapper.readValue(rawJson, Player[].class);
				return Arrays.asList(players);
			}
		} catch (final JsonProcessingException ex) {
			logger.error("Error processing json", ex);
		} catch (final ProcessingException ex) {
			logger.error("Processing error", ex);
		} catch (final Exception ex) {
			logger.error("Unknown error", ex);
		}
		return Collections.emptyList();
	}
}
