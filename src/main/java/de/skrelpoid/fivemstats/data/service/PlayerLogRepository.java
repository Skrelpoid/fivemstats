package de.skrelpoid.fivemstats.data.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import de.skrelpoid.fivemstats.data.PlayerLogSeconds;
import de.skrelpoid.fivemstats.data.entity.PlayerLog;

public interface PlayerLogRepository extends JpaRepository<PlayerLog, Long>, JpaSpecificationExecutor<PlayerLog> {

	public static final String LOGGED_IN_TIME_QUERY_BEGIN = """
			SELECT player_id AS playerId, SUM(
			  TIMESTAMPDIFF(SECOND, log_in_time, CASE WHEN log_out_time IS NULL THEN
			    CAST(?1 AS DATETIME)
			  ELSE
			    log_out_time
			  END)) AS cumulatedSeconds
			FROM player_log
			""";

	public static final String LOGGED_IN_TIME_QUERY_WHERE = """
			WHERE
			?2 < CASE WHEN log_out_time IS NULL THEN
			    CAST(?1 AS DATETIME)
			  ELSE
			    log_out_time
			  END)) AND
			log_in_time < ?3
			""";

	public static final String LOGGED_IN_TIME_QUERY_ORDER_GROUP = """
			GROUP BY player_id
			ORDER BY cumulatedSeconds DESC
			""";

	public static final String ALL_LOGGED_IN_TIME_QUERY = LOGGED_IN_TIME_QUERY_BEGIN + LOGGED_IN_TIME_QUERY_ORDER_GROUP;
	public static final String LOGGED_IN_TIME_QUERY_BY_RANGE = LOGGED_IN_TIME_QUERY_BEGIN + LOGGED_IN_TIME_QUERY_WHERE
			+ LOGGED_IN_TIME_QUERY_ORDER_GROUP;

	List<PlayerLog> findAllByLogOutTimeIsNull();

	List<PlayerLog> findAllByLogOutTimeIsNotNull();

	@Query(value = ALL_LOGGED_IN_TIME_QUERY, nativeQuery = true)
	List<PlayerLogSeconds> calculateAllLoggedInTime(LocalDateTime now);
	
	@Query(value = LOGGED_IN_TIME_QUERY_BY_RANGE, nativeQuery = true)
	List<PlayerLogSeconds> calculateLoggedInTimeFromTo(LocalDateTime now, LocalDateTime from, LocalDateTime to);
}
