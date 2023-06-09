package de.skrelpoid.fivemstats.data.service;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import de.skrelpoid.fivemstats.data.entity.Player;

@Service
public class PlayerService {

    private final PlayerRepository repository;

    public PlayerService(final PlayerRepository repository) {
        this.repository = repository;
    }

    public Optional<Player> get(final Long id) {
        return repository.findById(id);
    }

    public Player update(final Player entity) {
        return repository.save(entity);
    }

    public void delete(final Long id) {
        repository.deleteById(id);
    }

    public Page<Player> list(final Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Player> list(final Pageable pageable, final Specification<Player> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }
    
    public Map<Long, Player> findAllAndGroupByID(){
    	return repository.findAll().stream().collect(toMap(Player::getDiscordId, p -> p));
    }

}
