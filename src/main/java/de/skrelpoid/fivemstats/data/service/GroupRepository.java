package de.skrelpoid.fivemstats.data.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import de.skrelpoid.fivemstats.data.entity.Group;

public interface GroupRepository
        extends
            JpaRepository<Group, Long>,
            JpaSpecificationExecutor<Group> {
	
}
