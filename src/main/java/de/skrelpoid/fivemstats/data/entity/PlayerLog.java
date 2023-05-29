package de.skrelpoid.fivemstats.data.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class PlayerLog implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column
	@GeneratedValue
	private Long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "playerId")
	private Player player;
	
	@Column
	private LocalDateTime logInTime; 
	
	@Column
	private LocalDateTime logOutTime;

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(final Player player) {
		this.player = player;
	}

	public LocalDateTime getLogInTime() {
		return logInTime;
	}

	public void setLogInTime(final LocalDateTime logInTime) {
		this.logInTime = logInTime;
	}

	public LocalDateTime getLogOutTime() {
		return logOutTime;
	}

	public void setLogOutTime(final LocalDateTime logOutTime) {
		this.logOutTime = logOutTime;
	}
	
	@Override
    public int hashCode() {
        if (getId() != null) {
            return getId().hashCode();
        }
        return super.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof final PlayerLog that)) {
            return false; // null or not an AbstractEntity class
        }
        if (getId() != null) {
            return getId().equals(that.getId());
        }
        return super.equals(that);
    }

}
