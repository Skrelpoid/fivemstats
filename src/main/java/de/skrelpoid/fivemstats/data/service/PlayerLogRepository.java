package de.skrelpoid.fivemstats.data.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import de.skrelpoid.fivemstats.data.entity.PlayerLog;

public interface PlayerLogRepository extends JpaRepository<PlayerLog, Long>, JpaSpecificationExecutor<PlayerLog> {

	public static final String ALL_LOGGED_IN_TIME_QUERY = """
			SELECT player_id, SUM(
			  TIMESTAMPDIFF(SECOND, log_in_time, CASE WHEN log_out_time IS NULL THEN
			    CAST(?1 AS DATETIME)
			  ELSE
			    log_out_time
			  END)) AS cumulated_time
			FROM player_log
			GROUP BY player_id
			""";

	List<PlayerLog> findAllByLogOutTimeIsNull();

	List<PlayerLog> findAllByLogOutTimeIsNotNull();

	@Query(value = ALL_LOGGED_IN_TIME_QUERY, nativeQuery = true)
	Object[][] calculateAllLoggedInTime(LocalDateTime now);
}
