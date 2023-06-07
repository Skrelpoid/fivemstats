package de.skrelpoid.fivemstats.job;

import static java.util.stream.Collectors.toSet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.skrelpoid.fivemstats.data.entity.Player;
import de.skrelpoid.fivemstats.data.entity.PlayerLog;
import de.skrelpoid.fivemstats.data.service.PlayerLogService;
import de.skrelpoid.fivemstats.data.service.PlayerRestService;
import de.skrelpoid.fivemstats.data.service.PlayerService;

@Component
@DisallowConcurrentExecution
public class QueryPlayerDataJob implements Job {

	private static final Logger logger = LoggerFactory.getLogger(QueryPlayerDataJob.class);
	
	@Autowired
	private PlayerRestService playerRestService;
	
	@Autowired
	private PlayerService playerService;
	
	@Autowired
	private PlayerLogService playerLogService;

	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		logger.info("Querying Player Data");
		final LocalDateTime now = LocalDateTime.now();
		final List<Player> players = playerRestService.queryPlayers();
		final List<Player> savedPlayers = new ArrayList<>();
		loadOrSavePlayers(players, savedPlayers);
		final List<PlayerLog> activeLogs = playerLogService.getActiveLogs();
		final Set<Player> loggedIn = activeLogs.stream()
				.map(PlayerLog::getPlayer)
				.collect(toSet());
		activeLogs.removeIf(log -> savedPlayers.contains(log.getPlayer()));
		// log out all players that were not in the rest response of the server
		for (final PlayerLog toLogOut : activeLogs) {
			toLogOut.setLogOutTime(now);
			playerLogService.update(toLogOut);
		}
		// only newly logged in players remain in savedPlayers
		savedPlayers.removeAll(loggedIn);
		for (final Player newlyLoggedIn : savedPlayers) {
			final PlayerLog log = new PlayerLog();
			log.setLogInTime(now);
			log.setPlayer(newlyLoggedIn);
			playerLogService.update(log);
		}
	}

	protected void loadOrSavePlayers(final List<Player> players, final List<Player> savedPlayers) {
		for (final Player player : players) {
			final Optional<Player> existing = playerService.get(player.getDiscordId());
			if (existing.isPresent()) {
				existing.get().setName(player.getName());
				savedPlayers.add(playerService.update(existing.get()));
			} else {
				savedPlayers.add(playerService.update(player));
			}
		}
	}

}
