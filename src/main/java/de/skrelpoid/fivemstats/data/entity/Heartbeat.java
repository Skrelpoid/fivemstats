package de.skrelpoid.fivemstats.data.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Save the last active datetime of the service
 * 
 * @author dustinkristen
 *
 */
@Entity
public class Heartbeat implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final long STATIC_ID = 1;
	
	@Id
    private final Long id;

	@Column
	private LocalDateTime lastActiveTime;
	
	Heartbeat() {
		this.id = STATIC_ID;
	}

	public Heartbeat(final LocalDateTime lastActiveTime) {
		this.id = STATIC_ID;
		this.lastActiveTime = lastActiveTime;
	}

	public LocalDateTime getLastActiveTime() {
		return lastActiveTime;
	}

	public void setLastActiveTime(final LocalDateTime lastActiveTime) {
		this.lastActiveTime = lastActiveTime;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof final Heartbeat that)) {
            return false; // null or not an Heartbeat class
        }
        if (id != null) {
            return id.equals(that.id);
        }
        return super.equals(that);
	}

	@Override
	public int hashCode() {
		if (id != null) {
            return id.hashCode();
        }
        return super.hashCode();
	}

}
