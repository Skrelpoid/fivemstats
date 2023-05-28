package de.skrelpoid.fivemstats.data.entity;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Player implements Serializable {

	private static final Logger logger = LoggerFactory.getLogger(Player.class);

	private static final long serialVersionUID = 1L;

	@Id
	@Column
	private Long discordId;

	private Long steamId;
	@Column(length = 40)
	private String license;
	@Column(length = 40)
	private String license2;
	private Long xboxLiveId;
	private Long liveId;
	private Long fivemId;

	@Column(length = 100)
	private String name;
	@Column(length = 100)
	private String discordIdentifier;
	@Column(length = 512)
	private String alias1;
	@Column(length = 256)
	private String alias2;
	@Column(length = 128)
	private String alias3;

	@JsonProperty("identifiers")
	private void unpackNameFromNestedObject(final List<String> identifiers) {
		for (final String identifier : identifiers) {
			final String[] keyValue = identifier.split(":");
			switch (keyValue[0]) {
			case "steam":
				steamId = Long.parseLong(keyValue[1]);
				break;
			case "license":
				license = keyValue[1];
				break;
			case "license2":
				license2 = keyValue[1];
				break;
			case "xbl":
				xboxLiveId = Long.parseLong(keyValue[1]);
				break;
			case "live":
				liveId = Long.parseLong(keyValue[1]);
				break;
			case "discord":
				discordId = Long.parseLong(keyValue[1]);
				break;
			case "fivem":
				fivemId = Long.parseLong(keyValue[1]);
				break;
			default:
				logger.warn("Unknown identifier: {}", identifier);
				break;
			}
		}
	}

}
