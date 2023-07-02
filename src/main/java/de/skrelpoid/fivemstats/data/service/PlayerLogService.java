package de.skrelpoid.fivemstats.data.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import de.skrelpoid.fivemstats.data.PlayerLogSeconds;
import de.skrelpoid.fivemstats.data.entity.Player;
import de.skrelpoid.fivemstats.data.entity.PlayerLog;

@Service
public class PlayerLogService {
	
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
    
    public List<PlayerLog> updateAll(final Iterable<PlayerLog> entities) {
    	return repository.saveAll(entities);
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
    
    /**
     * get all logs who have a log out time that is null, which means that the player belonging to the log is currently logged in
     * @return a list of playerlogs that may be empty
     */
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
    
    public List<PlayerLogSeconds> calculateAllLoggedInTime() {
    	return repository.calculateAllLoggedInTime(LocalDateTime.now());
    }
    
    public List<PlayerLogSeconds> calculateLoggedInTimeFromTo(final LocalDateTime from, final LocalDateTime to) {
    	return repository.calculateLoggedInTimeFromTo(LocalDateTime.now(), from, to);
    }

}
