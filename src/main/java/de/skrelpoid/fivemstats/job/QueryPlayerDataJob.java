package de.skrelpoid.fivemstats.job;

import java.util.List;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.skrelpoid.fivemstats.data.entity.Player;
import de.skrelpoid.fivemstats.data.service.PlayerRestService;

@Component
@DisallowConcurrentExecution
public class QueryPlayerDataJob implements Job {

	private static final Logger logger = LoggerFactory.getLogger(QueryPlayerDataJob.class);
	
	@Autowired
	private PlayerRestService playerRestService;

	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		logger.info("Querying Player Data");
		final List<Player> players = playerRestService.queryPlayers();
		logger.info("players: {}", players);
	}

}
