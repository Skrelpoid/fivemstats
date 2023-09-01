package de.skrelpoid.fivemstats.data.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import de.skrelpoid.fivemstats.data.entity.Group;

@Service
public class GroupService {

	private final GroupRepository repository;

	public GroupService(final GroupRepository repository) {
		this.repository = repository;
	}

	public Optional<Group> get(final Long id) {
		return repository.findById(id);
	}

	public Group update(final Group entity) {
		return repository.save(entity);
	}

	public void delete(final Long id) {
		repository.deleteById(id);
	}

	public Page<Group> list(final Pageable pageable) {
		return repository.findAll(pageable);
	}

	public Page<Group> list(final Pageable pageable, final Specification<Group> filter) {
		return repository.findAll(filter, pageable);
	}

	public List<Group> listAll() {
		return repository.findAll();
	}

	public int count() {
		return (int) repository.count();
	}

}
