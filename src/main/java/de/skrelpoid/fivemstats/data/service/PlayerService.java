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
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;

@Service
public class PlayerService {

	private final PlayerRepository repository;

	public PlayerService(final PlayerRepository repository) {
		this.repository = repository;
	}

	public Optional<Player> get(final Long id) {
		return repository.findById(id);
	}

	public Optional<Player> findByLicenseAndName(final String license, final String name) {
		return repository.findByLicenseAndName(license, name);
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

	public List<Player> findAllAndSortByCanonicalName() {
		final var list = repository.findAll();
		list.sort(Comparator.comparing(Player::toString));
		return list;
	}

	public Map<Long, Player> findAllAndGroupByID() {
		return repository.findAll().stream().collect(toMap(Player::getId, p -> p));
	}

	public <X> Predicate buildSearchPredicate(final String searchValue, final From<Player, Player> root,
			final CriteriaBuilder criteriaBuilder) {
		if (StringUtils.isBlank(searchValue)) {
			return criteriaBuilder.and(); // true
		}
		final String lowerCaseFilter = searchValue;
		final Predicate nameMatch =
				criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
						"%" + lowerCaseFilter + "%");
		final Predicate dcMatch = criteriaBuilder.like(
				criteriaBuilder.lower(root.get("discordIdentifier")),
				"%" + lowerCaseFilter + "%");
		final Predicate aliasesMatch = criteriaBuilder
				.like(criteriaBuilder.lower(root.get("aliases")),
						"%" + lowerCaseFilter + "%");
		final Predicate steamMatch = criteriaBuilder
				.equal(criteriaBuilder.lower(root.get("steamId")), lowerCaseFilter);
		final Predicate licenseMatch = criteriaBuilder
				.equal(criteriaBuilder.lower(root.get("license")), lowerCaseFilter);
		final Predicate license2Match = criteriaBuilder
				.equal(criteriaBuilder.lower(root.get("license2")), lowerCaseFilter);
		if (tryParseLong(lowerCaseFilter)) {
			final Predicate discordMatch =
					criteriaBuilder.equal(root.get("discordId"),
							lowerCaseFilter);
			final Predicate xboxLiveMatch =
					criteriaBuilder.equal(root.get("xboxLiveId"),
							lowerCaseFilter);
			final Predicate liveMatch =
					criteriaBuilder.equal(root.get("liveId"), lowerCaseFilter);
			final Predicate fivemMatch =
					criteriaBuilder.equal(root.get("fivemId"), lowerCaseFilter);
			return criteriaBuilder.or(nameMatch, dcMatch, aliasesMatch, discordMatch, steamMatch,
					licenseMatch, license2Match, xboxLiveMatch, liveMatch, fivemMatch);
		}
		return criteriaBuilder.or(nameMatch, dcMatch, aliasesMatch, steamMatch,
				licenseMatch, license2Match);
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
