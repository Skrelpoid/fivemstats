package de.skrelpoid.fivemstats.data.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class PlayerLog implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column
	@GeneratedValue
	private Long id;
	
	@Column
	private LocalDateTime logInTime; // NOSONAR this should not be transient because we want it in the database
	
	@Column
	private LocalDateTime logOutTime; // NOSONAR this should not be transient because we want it in the database

}
