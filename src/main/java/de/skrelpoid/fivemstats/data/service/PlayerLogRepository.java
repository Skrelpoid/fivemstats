package de.skrelpoid.fivemstats.data.service;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import de.skrelpoid.fivemstats.data.entity.PlayerLog;

public interface PlayerLogRepository
        extends
            JpaRepository<PlayerLog, Long>,
            JpaSpecificationExecutor<PlayerLog> {

	List<PlayerLog> findAllByLogOutTimeIsNull();
	
	List<PlayerLog> findAllByLogOutTimeIsNotNull();
}
