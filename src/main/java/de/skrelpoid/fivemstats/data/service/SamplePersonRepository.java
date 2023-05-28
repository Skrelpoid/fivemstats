package de.skrelpoid.fivemstats.data.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import de.skrelpoid.fivemstats.data.entity.SamplePerson;

public interface SamplePersonRepository
        extends
            JpaRepository<SamplePerson, Long>,
            JpaSpecificationExecutor<SamplePerson> {

}
