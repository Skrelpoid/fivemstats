package de.skrelpoid.fivemstats.data.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import de.skrelpoid.fivemstats.data.entity.Heartbeat;

public interface HeartbeatRepository
        extends
            JpaRepository<Heartbeat, Long>,
            JpaSpecificationExecutor<Heartbeat> {
	
}
