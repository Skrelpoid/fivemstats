package de.skrelpoid.fivemstats.data.service;

import static java.util.stream.Collectors.toMap;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import de.skrelpoid.fivemstats.data.entity.Player;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

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
    
    public List<Player> list(final Specification<Player> filter) {
    	return repository.findAll(filter);
    }

    public int count() {
        return (int) repository.count();
    }
    
    public List<Player> findAllAndSortByCanonicalName(){
    	final var list = repository.findAll();
    	list.sort(Comparator.comparing(Player::toString));
    	return list;
    }
    
    public Map<Long, Player> findAllAndGroupByID(){
    	return repository.findAll().stream().collect(toMap(Player::getDiscordId, p -> p));
    }
    
    public <X> Predicate buildSearchPredicate(final String searchValue, final Root<X> root,
			final CriteriaBuilder criteriaBuilder, final String subRoot ) {
    	if (StringUtils.isBlank(searchValue)) {
    		return criteriaBuilder.and(); // true
    	}
    	final String lowerCaseFilter = searchValue;
		final Predicate nameMatch = criteriaBuilder.like(criteriaBuilder.lower(getPath(root, subRoot, "name")),
				"%" + lowerCaseFilter + "%");
		final Predicate dcMatch = criteriaBuilder.like(
				criteriaBuilder.lower(getPath(root, subRoot, "discordIdentifier")),
				"%" + lowerCaseFilter + "%");
		final Predicate aliasesMatch = criteriaBuilder
				.like(criteriaBuilder.lower(getPath(root, subRoot, "aliases")), "%" + lowerCaseFilter + "%");
		final Predicate steamMatch = criteriaBuilder
				.equal(criteriaBuilder.lower(getPath(root, subRoot, "steamId")), lowerCaseFilter);
		final Predicate licenseMatch = criteriaBuilder
				.equal(criteriaBuilder.lower(getPath(root, subRoot, "license")), lowerCaseFilter);
		final Predicate license2Match = criteriaBuilder
				.equal(criteriaBuilder.lower(getPath(root, subRoot, "license2")), lowerCaseFilter);
		if (tryParseLong(lowerCaseFilter)) {
			final Predicate discordMatch = criteriaBuilder.equal(getPath(root, subRoot, "discordId"),
					lowerCaseFilter);
			final Predicate xboxLiveMatch = criteriaBuilder.equal(getPath(root, subRoot, "xboxLiveId"),
					lowerCaseFilter);
			final Predicate liveMatch = criteriaBuilder.equal(getPath(root, subRoot, "liveId"), lowerCaseFilter);
			final Predicate fivemMatch = criteriaBuilder.equal(getPath(root, subRoot, "fivemId"), lowerCaseFilter);
			return criteriaBuilder.or(nameMatch, dcMatch, aliasesMatch, discordMatch, steamMatch,
					licenseMatch, license2Match, xboxLiveMatch, liveMatch, fivemMatch);
		} else {
			return criteriaBuilder.or(nameMatch, dcMatch, aliasesMatch, steamMatch,
					licenseMatch, license2Match);
		}
    }
    
    private <X, Y> Path<Y> getPath(final Root<X> root, final String subRoot, final String attributeName) {
    	if (StringUtils.isNotBlank(subRoot)) {
    		return root.get(subRoot).get(attributeName);
    	}
    	return root.get(attributeName);
    }
    

	private boolean tryParseLong(final String lowerCaseFilter) {
		try {
			Long.parseLong(lowerCaseFilter);
			return true;
		} catch (final NumberFormatException ex) {
			return false;
		}
	}

    
}
