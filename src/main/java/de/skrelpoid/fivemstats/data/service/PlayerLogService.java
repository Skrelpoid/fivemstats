package de.skrelpoid.fivemstats.data.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import de.skrelpoid.fivemstats.data.entity.Player;
import de.skrelpoid.fivemstats.data.entity.PlayerLog;

@Service
public class PlayerLogService {
	
	private static final Logger logger = LoggerFactory.getLogger(PlayerLogService.class);

    private final PlayerLogRepository repository;

    public PlayerLogService(final PlayerLogRepository repository) {
        this.repository = repository;
    }

    public Optional<PlayerLog> get(final Long id) {
        return repository.findById(id);
    }

    public PlayerLog update(final PlayerLog entity) {
        return repository.save(entity);
    }

    public void delete(final Long id) {
        repository.deleteById(id);
    }

    public Page<PlayerLog> list(final Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<PlayerLog> list(final Pageable pageable, final Specification<PlayerLog> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }
    
    public List<PlayerLog> getActiveLogs() {
    	return repository.findAllByLogOutTimeIsNull();
    }
    
    public List<Player> getLoggedInPlayers() {
    	return getActiveLogs().stream()
    			.map(PlayerLog::getPlayer)
    			.toList();
    }
    
    public List<PlayerLog> getPastLogs() {
    	return repository.findAllByLogOutTimeIsNotNull();
    }
    
    public void calculateAllLoggedInTime() {
    	final Object[][] result = repository.calculateAllLoggedInTime(LocalDateTime.now());
    	logger.info(Arrays.deepToString(result));
    }

}
