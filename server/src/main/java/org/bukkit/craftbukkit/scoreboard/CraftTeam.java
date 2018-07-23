package org.bukkit.craftbukkit.scoreboard;

import java.util.Set;

import net.minecraft.server.ScoreboardTeamBase.EnumNameTagVisibility;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Team;

import com.google.common.collect.ImmutableSet;

import net.minecraft.server.ScoreboardTeam;
import net.minecraft.server.ScoreboardTeamBase;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.util.CraftChatMessage;

final class CraftTeam extends CraftScoreboardComponent implements Team {
    private final ScoreboardTeam team;

    CraftTeam(CraftScoreboard scoreboard, ScoreboardTeam team) {
        super(scoreboard);
        this.team = team;
    }

    public String getName() throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        return team.getName();
    }

    public String getDisplayName() throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        return CraftChatMessage.fromComponent(team.getDisplayName());
    }

    public void setDisplayName(String displayName) throws IllegalStateException {
        Validate.notNull(displayName, "Display name cannot be null");
        Validate.isTrue(displayName.length() <= 128, "Display name '" + displayName + "' is longer than the limit of 128 characters");
        CraftScoreboard scoreboard = checkState();

        team.setDisplayName(CraftChatMessage.fromString(displayName)[0]); // SPIGOT-4112: not nullable
    }

    public String getPrefix() throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        return CraftChatMessage.fromComponent(team.getPrefix());
    }

    public void setPrefix(String prefix) throws IllegalStateException, IllegalArgumentException {
        Validate.notNull(prefix, "Prefix cannot be null");
        Validate.isTrue(prefix.length() <= 64, "Prefix '" + prefix + "' is longer than the limit of 64 characters");
        CraftScoreboard scoreboard = checkState();

        team.setPrefix(CraftChatMessage.fromStringOrNull(prefix));
    }

    public String getSuffix() throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        return CraftChatMessage.fromComponent(team.getPrefix());
    }

    public void setSuffix(String suffix) throws IllegalStateException, IllegalArgumentException {
        Validate.notNull(suffix, "Suffix cannot be null");
        Validate.isTrue(suffix.length() <= 64, "Suffix '" + suffix + "' is longer than the limit of 64 characters");
        CraftScoreboard scoreboard = checkState();

        team.setSuffix(CraftChatMessage.fromStringOrNull(suffix));
    }

    @Override
    public ChatColor getColor() throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        return CraftChatMessage.getColor(team.getColor());
    }

    @Override
    public void setColor(ChatColor color) {
        Validate.notNull(color, "Color cannot be null");
        CraftScoreboard scoreboard = checkState();

        team.setColor(CraftChatMessage.getColor(color));
    }

    public boolean allowFriendlyFire() throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        return team.allowFriendlyFire();
    }

    public void setAllowFriendlyFire(boolean enabled) throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        team.setAllowFriendlyFire(enabled);
    }

    public boolean canSeeFriendlyInvisibles() throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        return team.canSeeFriendlyInvisibles();
    }

    public void setCanSeeFriendlyInvisibles(boolean enabled) throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        team.setCanSeeFriendlyInvisibles(enabled);
    }

    public NameTagVisibility getNameTagVisibility() throws IllegalArgumentException {
        CraftScoreboard scoreboard = checkState();

        return notchToBukkit(team.getNameTagVisibility());
    }

    public void setNameTagVisibility(NameTagVisibility visibility) throws IllegalArgumentException {
        CraftScoreboard scoreboard = checkState();

        team.setNameTagVisibility(bukkitToNotch(visibility));
    }

    public Set<OfflinePlayer> getPlayers() throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        ImmutableSet.Builder<OfflinePlayer> players = ImmutableSet.builder();
        for (String playerName : team.getPlayerNameSet()) {
            players.add(Bukkit.getOfflinePlayer(playerName));
        }
        return players.build();
    }

    @Override
    public Set<String> getEntries() throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        ImmutableSet.Builder<String> entries = ImmutableSet.builder();
        for (String playerName: team.getPlayerNameSet()){
            entries.add(playerName);
        }
        return entries.build();
    }

    public int getSize() throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        return team.getPlayerNameSet().size();
    }

    public void addPlayer(OfflinePlayer player) throws IllegalStateException, IllegalArgumentException {
        Validate.notNull(player, "OfflinePlayer cannot be null");
        addEntry(player.getName());
    }

    public void addEntry(String entry) throws IllegalStateException, IllegalArgumentException {
        Validate.notNull(entry, "Entry cannot be null");
        CraftScoreboard scoreboard = checkState();

        scoreboard.board.addPlayerToTeam(entry, team);
    }

    public boolean removePlayer(OfflinePlayer player) throws IllegalStateException, IllegalArgumentException {
        Validate.notNull(player, "OfflinePlayer cannot be null");
        return removeEntry(player.getName());
    }

    public boolean removeEntry(String entry) throws IllegalStateException, IllegalArgumentException {
        Validate.notNull(entry, "Entry cannot be null");
        CraftScoreboard scoreboard = checkState();

        if (!team.getPlayerNameSet().contains(entry)) {
            return false;
        }

        scoreboard.board.removePlayerFromTeam(entry, team);
        return true;
    }

    public boolean hasPlayer(OfflinePlayer player) throws IllegalArgumentException, IllegalStateException {
        Validate.notNull(player, "OfflinePlayer cannot be null");
        return hasEntry(player.getName());
    }

    public boolean hasEntry(String entry) throws IllegalArgumentException, IllegalStateException {
        Validate.notNull("Entry cannot be null");

        CraftScoreboard scoreboard = checkState();

        return team.getPlayerNameSet().contains(entry);
    }

    @Override
    public void unregister() throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        scoreboard.board.removeTeam(team);
    }

    @Override
    public OptionStatus getOption(Option option) throws IllegalStateException {
        checkState();

        switch (option) {
            case NAME_TAG_VISIBILITY:
                return OptionStatus.values()[team.getNameTagVisibility().ordinal()];
            case DEATH_MESSAGE_VISIBILITY:
                return OptionStatus.values()[team.getDeathMessageVisibility().ordinal()];
            case COLLISION_RULE:
                return OptionStatus.values()[team.getCollisionRule().ordinal()];
            default:
                throw new IllegalArgumentException("Unrecognised option " + option);
        }
    }

    @Override
    public void setOption(Option option, OptionStatus status) throws IllegalStateException {
        checkState();

        switch (option) {
            case NAME_TAG_VISIBILITY:
                team.setNameTagVisibility(EnumNameTagVisibility.values()[status.ordinal()]);
                break;
            case DEATH_MESSAGE_VISIBILITY:
                team.setDeathMessageVisibility(EnumNameTagVisibility.values()[status.ordinal()]);
                break;
            case COLLISION_RULE:
                team.setCollisionRule(ScoreboardTeamBase.EnumTeamPush.values()[status.ordinal()]);
                break;
            default:
                throw new IllegalArgumentException("Unrecognised option " + option);
        }
    }

    public static EnumNameTagVisibility bukkitToNotch(NameTagVisibility visibility) {
        switch (visibility) {
            case ALWAYS:
                return EnumNameTagVisibility.ALWAYS;
            case NEVER:
                return EnumNameTagVisibility.NEVER;
            case HIDE_FOR_OTHER_TEAMS:
                return EnumNameTagVisibility.HIDE_FOR_OTHER_TEAMS;
            case HIDE_FOR_OWN_TEAM:
                return EnumNameTagVisibility.HIDE_FOR_OWN_TEAM;
            default:
                throw new IllegalArgumentException("Unknown visibility level " + visibility);
        }
    }

    public static NameTagVisibility notchToBukkit(EnumNameTagVisibility visibility) {
        switch (visibility) {
            case ALWAYS:
                return NameTagVisibility.ALWAYS;
            case NEVER:
                return NameTagVisibility.NEVER;
            case HIDE_FOR_OTHER_TEAMS:
                return NameTagVisibility.HIDE_FOR_OTHER_TEAMS;
            case HIDE_FOR_OWN_TEAM:
                return NameTagVisibility.HIDE_FOR_OWN_TEAM;
            default:
                throw new IllegalArgumentException("Unknown visibility level " + visibility);
        }
    }

    @Override
    CraftScoreboard checkState() throws IllegalStateException {
        if (getScoreboard().board.getTeam(team.getName()) == null) {
            throw new IllegalStateException("Unregistered scoreboard component");
        }

        return getScoreboard();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (this.team != null ? this.team.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CraftTeam other = (CraftTeam) obj;
        return !(this.team != other.team && (this.team == null || !this.team.equals(other.team)));
    }

}
