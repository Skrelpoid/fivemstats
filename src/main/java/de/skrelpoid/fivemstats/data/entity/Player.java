package de.skrelpoid.fivemstats.data.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Player implements Serializable {

	private static final Logger logger = LoggerFactory.getLogger(Player.class);

	private static final long serialVersionUID = 1L;

	@Id
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
	@Column(length = 100)
	private String alias1;
	@Column(length = 100)
	private String alias2;
	@Column(length = 100)
	private String alias3;
	@Column
	private Long longTermSecondsLogged;
	@OneToMany(mappedBy = "player", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private final List<PlayerLog> playerLogs = new ArrayList<>();

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

	public Long getId() {
		return getDiscordId();
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

	public String getAlias1() {
		return alias1;
	}

	public void setAlias1(final String alias1) {
		this.alias1 = alias1;
	}

	public String getAlias2() {
		return alias2;
	}

	public void setAlias2(final String alias2) {
		this.alias2 = alias2;
	}

	public String getAlias3() {
		return alias3;
	}

	public void setAlias3(final String alias3) {
		this.alias3 = alias3;
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
            return false; // null or not an AbstractEntity class
        }
        if (getId() != null) {
            return getId().equals(that.getId());
        }
        return super.equals(that);
    }
}
