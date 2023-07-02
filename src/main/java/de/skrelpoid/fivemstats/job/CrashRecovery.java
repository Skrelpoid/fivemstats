package de.skrelpoid.fivemstats.job;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.skrelpoid.fivemstats.data.entity.Heartbeat;
import de.skrelpoid.fivemstats.data.entity.PlayerLog;
import de.skrelpoid.fivemstats.data.service.HeartbeatRepository;
import de.skrelpoid.fivemstats.data.service.PlayerLogService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * If the application crashed (or shut down when people were still logged in and
 * they couldn't be logged out), this class will make sure on startup to apply a
 * sensible log out time, which will either be the log in time + 1 min or the
 * last active time of the service logged in the heartbeat table
 * 
 * @author dustinkristen
 *
 */
@Service
public class CrashRecovery {

	private static final Logger logger = LoggerFactory.getLogger(CrashRecovery.class);
	private final HeartbeatRepository heartbeatRepository;
	private final PlayerLogService playerLogService;
	private volatile boolean finished;

	public CrashRecovery(final HeartbeatRepository heartbeatRepository, final PlayerLogService playerLogService) {
		this.heartbeatRepository = heartbeatRepository;
		this.playerLogService = playerLogService;
	}

	@PostConstruct
	void init() {
		logger.info("start crashrecovery");
		final Heartbeat lastKnownHeartbeat = heartbeatRepository.findAll().stream().findFirst().orElse(new Heartbeat(LocalDateTime.MIN));
		final LocalDateTime heartbeatTime = lastKnownHeartbeat.getLastActiveTime();
		
		final List<PlayerLog> activeLogs = playerLogService.getActiveLogs();
		for (final PlayerLog playerLog : activeLogs) {
			final var logInTime = playerLog.getLogInTime();
			if (logInTime.isBefore(heartbeatTime)) {
				playerLog.setLogOutTime(heartbeatTime);
			} else {
				final var logOutTime = logInTime.plusMinutes(1);
				playerLog.setLogOutTime(logOutTime);
			}
		}
		playerLogService.updateAll(activeLogs);
		finished = true;
		logger.info("finished crashrecovery");
	}
	
	@PreDestroy
    public void onShutdown() {
		logger.info("start log out on shutdown");
		final LocalDateTime now = LocalDateTime.now();
		final List<PlayerLog> activeLogs = playerLogService.getActiveLogs();
		for (final PlayerLog playerLog : activeLogs) {
			playerLog.setLogOutTime(now);
		}
		playerLogService.updateAll(activeLogs);
		logger.info("finished log out on shutdown");
	}

	public boolean isFinished() {
		return finished;
	}
}
