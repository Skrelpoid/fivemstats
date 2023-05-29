package de.skrelpoid.fivemstats.data.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import de.skrelpoid.fivemstats.data.entity.Player;

public interface PlayerRepository
        extends
            JpaRepository<Player, Long>,
            JpaSpecificationExecutor<Player> {
	
}
