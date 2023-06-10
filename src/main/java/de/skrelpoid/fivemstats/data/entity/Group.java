package de.skrelpoid.fivemstats.data.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
	@ManyToMany
	@JoinTable(name = "player_in_group", joinColumns = @JoinColumn(name = "player_id"), inverseJoinColumns = @JoinColumn(name = "group_id"))
	private List<Player> players;

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public void setPlayers(final List<Player> players) {
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

}
