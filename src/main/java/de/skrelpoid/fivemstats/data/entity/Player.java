package de.skrelpoid.fivemstats.data.entity;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "license" }))
public class Player implements Serializable {

	private static final Logger logger = LoggerFactory.getLogger(Player.class);

	private static final long serialVersionUID = 1L;

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column
	private Long discordId;

	@Column(length = 40)
	private String steamId;
	
	@Column(length = 40)
	private String license;
	@Column(length = 40)
	private String license2;
	@Column
	private Long xboxLiveId;
	@Column
	private Long liveId;
	@Column
	private Long fivemId;

	@Column(length = 100)
	private String name;
	@Column(length = 100)
	private String discordIdentifier;
	@Column(length = 500)
	private String aliases;
	@Column
	private Long longTermSecondsLogged;
	@OneToMany(mappedBy = "player", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<PlayerLog> playerLogs;
	@ManyToMany(mappedBy = "players")
	private List<Group> groups;

	@JsonProperty("identifiers")
	private void unpackNameFromNestedObject(final List<String> identifiers) {
		for (final String identifier : identifiers) {
			final String[] keyValue = identifier.split(":");
			switch (keyValue[0]) {
			case "steam":
				steamId = keyValue[1];
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

	public void setId(final Long id) {
		this.id = id;
	}
	
	public Long getId() {
		return id;
	}

	public Long getDiscordId() {
		return discordId;
	}

	public void setDiscordId(final Long discordId) {
		this.discordId = discordId;
	}

	public String getSteamId() {
		return steamId;
	}

	public void setSteamId(final String steamId) {
		this.steamId = steamId;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(final String license) {
		this.license = license;
	}

	public String getLicense2() {
		return license2;
	}

	public void setLicense2(final String license2) {
		this.license2 = license2;
	}

	public Long getXboxLiveId() {
		return xboxLiveId;
	}

	public void setXboxLiveId(final Long xboxLiveId) {
		this.xboxLiveId = xboxLiveId;
	}

	public Long getLiveId() {
		return liveId;
	}

	public void setLiveId(final Long liveId) {
		this.liveId = liveId;
	}

	public Long getFivemId() {
		return fivemId;
	}

	public void setFivemId(final Long fivemId) {
		this.fivemId = fivemId;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getDiscordIdentifier() {
		return discordIdentifier;
	}

	public void setDiscordIdentifier(final String discordIdentifier) {
		this.discordIdentifier = discordIdentifier;
	}

	public String getAliases() {
		return aliases;
	}

	public void setAliases(final String aliases) {
		this.aliases = aliases;
	}

	public Long getLongTermSecondsLogged() {
		return longTermSecondsLogged;
	}

	public void setLongTermSecondsLogged(final Long longTermSecondsLogged) {
		this.longTermSecondsLogged = longTermSecondsLogged;
	}

	public List<PlayerLog> getPlayerLogs() {
		return playerLogs;

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
        if (!(obj instanceof final Player that)) {
            return false;
        }
        if (getId() != null) {
            return getId().equals(that.getId());
        }
        return super.equals(that);
    }

	public List<Group> getGroups() {
		return groups;
	}
	
	public void setGroups(final List<Group> groups) {
		this.groups = groups;
	}
	
	@Override
	public String toString() {
		if (StringUtils.isNotBlank(aliases)) {
			return aliases;
		}
		if (StringUtils.isNotBlank(discordIdentifier)) {
			return discordIdentifier;
		}
		return name;
	}
}
