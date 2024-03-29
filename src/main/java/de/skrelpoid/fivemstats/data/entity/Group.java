package de.skrelpoid.fivemstats.data.entity;

import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "player_group")
public class Group extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@Column(length = 100)
	private String name;
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "player_in_group", joinColumns = @JoinColumn(name = "player_id"),
			inverseJoinColumns = @JoinColumn(name = "group_id"))
	private Set<Player> players;

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Set<Player> getPlayers() {
		return players;
	}

	public void setPlayers(final Set<Player> players) {
		this.players = players;
	}

	@Override
	public boolean equals(final Object obj) {
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		return getName();
	}

}
