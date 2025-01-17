package net.glowstone.scoreboard;

import com.flowpowered.network.Message;
import com.google.common.collect.ImmutableSet;
import net.glowstone.constants.GlowDisplaySlot;
import net.glowstone.entity.GlowPlayer;
import net.glowstone.net.message.play.scoreboard.ScoreboardDisplayMessage;
import net.glowstone.net.message.play.scoreboard.ScoreboardObjectiveMessage;
import net.glowstone.net.message.play.scoreboard.ScoreboardScoreMessage;
import net.glowstone.net.message.play.scoreboard.ScoreboardTeamMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.*;
import java.util.*;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Scoreboard implementation.
 */
public final class GlowScoreboard implements Scoreboard {

    // Objectives
    private final EnumMap<DisplaySlot, GlowObjective> displaySlots = new EnumMap<>(DisplaySlot.class);
    private final HashMap<String, GlowObjective> objectives = new HashMap<>();
    private final HashMap<String, Set<GlowObjective>> criteriaMap = new HashMap<>();

    // Score map - kept up to date by each objective
    private final HashMap<String, Set<GlowScore>> scoreMap = new HashMap<>();

    // Teams
    private final HashMap<String, GlowTeam> teams = new HashMap<>();
    private final HashMap<String, GlowTeam> entryTeams = new HashMap<>();

    // Players who are watching this scoreboard
    private final HashSet<GlowPlayer> players = new HashSet<>();

    ////////////////////////////////////////////////////////////////////////////
    // Internals

    /**
     * Send a player this scoreboard's contents and subscribe them to future
     * changes.
     *
     * @param player The player to subscribe.
     */
    public void subscribe(GlowPlayer player) {
        // send all the setup stuff
        // objectives
        for (GlowObjective objective : objectives.values()) {
            player.getSession().send(ScoreboardObjectiveMessage.create(objective.getName(), objective.getDisplayName()));
        }
        // display slots
        for (DisplaySlot slot : DisplaySlot.values()) {
            GlowObjective objective = displaySlots.get(slot);
            String name = objective != null ? objective.getName() : "";
            player.getSession().send(new ScoreboardDisplayMessage(GlowDisplaySlot.getId(slot), name));
        }
        // scores
        for (Map.Entry<String, Set<GlowScore>> entry : scoreMap.entrySet()) {
            for (GlowScore score : entry.getValue()) {
                player.getSession().send(new ScoreboardScoreMessage(entry.getKey(), score.getObjective().getName(), score.getScore()));
            }
        }
        // teams
        for (GlowTeam team : teams.values()) {
            player.getSession().send(team.getCreateMessage());
        }

        // add to player set
        players.add(player);
    }

    /**
     * Clear the player's scoreboard contents and unsubscribe them from
     * future changes.
     *
     * @param player The player to unsubscribe.
     */
    public void unsubscribe(GlowPlayer player) {
        // remove from player set
        players.remove(player);

        // send all the teardown stuff
        // teams
        for (GlowTeam team : teams.values()) {
            player.getSession().send(ScoreboardTeamMessage.remove(team.getName()));
        }
        // display slots
        for (DisplaySlot slot : DisplaySlot.values()) {
            player.getSession().send(new ScoreboardDisplayMessage(GlowDisplaySlot.getId(slot), ""));
        }
        // objectives
        for (GlowObjective objective : objectives.values()) {
            player.getSession().send(ScoreboardObjectiveMessage.remove(objective.getName()));
        }
    }

    /**
     * Broadcast a scoreboard update to all subscribed players.
     *
     * @param message The message to send.
     */
    void broadcast(Message message) {
        for (GlowPlayer player : players) {
            player.getSession().send(message);
        }
    }

    /**
     * Set the objective displayed in the given slot.
     *
     * @param slot      The display slot.
     * @param objective The objective to display there, possibly null.
     */
    void setDisplaySlot(DisplaySlot slot, GlowObjective objective) {
        GlowObjective previous = displaySlots.put(slot, objective);

        // previous objective is no longer in this display slot
        if (previous != null) {
            previous.displaySlot = null;
        }

        // new objective is now in this display slot
        if (objective != null) {
            // update objective's display slot
            broadcast(new ScoreboardDisplayMessage(GlowDisplaySlot.getId(slot), objective.getName()));
            objective.displaySlot = slot;
        } else {
            // no objective
            broadcast(new ScoreboardDisplayMessage(GlowDisplaySlot.getId(slot), ""));
        }
    }

    /**
     * Unregister an objective from the scoreboard.
     *
     * @param objective The objective to unregister.
     */
    void removeObjective(GlowObjective objective) {
        if (objective.displaySlot != null) {
            setDisplaySlot(objective.displaySlot, null);
        }

        getForCriteria(objective.getCriteria()).remove(objective);
        objectives.remove(objective.getName());
        broadcast(ScoreboardObjectiveMessage.remove(objective.getName()));
    }

    /**
     * Unregister a team from the scoreboard.
     *
     * @param team The team to unregister.
     */
    void removeTeam(GlowTeam team) {
		for(String name : team.getEntries()) entryTeams.remove(name);
        teams.remove(team.getName());
        broadcast(ScoreboardTeamMessage.remove(team.getName()));
    }

    /**
     * Get the internal set of objectives corresponding to a given criteria.
     *
     * @param criteria The criteria to look up.
     * @return The set of objectives.
     */
    Set<GlowObjective> getForCriteria(String criteria) {
        Set<GlowObjective> result = criteriaMap.get(criteria);
		if(result == null) {
			result = new HashSet<>();
			criteriaMap.put(criteria, result);
		}
        return result;
    }

    /**
     * Get the internal set of scores corresponding to a given entry.
     *
     * @param entry The entry to look up.
     * @return The set of scores.
     */
    Set<GlowScore> getScoresForName(String entry) {
        Set<GlowScore> result = scoreMap.get(entry);
		if(result == null) {
			result = new HashSet<>();
			scoreMap.put(entry, result);
		}
        return result;
    }

    /**
     * Update what team a player is associated with.
     *
     * @param player The player.
     * @param team   The team, or null for no team.
     */
    void setPlayerTeam(OfflinePlayer player, GlowTeam team) {
        GlowTeam previous = entryTeams.put(player.getName(), team);
        if (previous != null && previous.hasPlayer(player)) {
            previous.removeEntry(player.getName());
            broadcast(ScoreboardTeamMessage.removePlayers(previous.getName(), Arrays.asList(player.getName())));
        }
        if (team != null) {
            broadcast(ScoreboardTeamMessage.addPlayers(team.getName(), Arrays.asList(player.getName())));
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Objectives

    public Objective registerNewObjective(String name, String criteria) throws IllegalArgumentException {
        checkNotNull(name, "Name cannot be null");
        checkNotNull(criteria, "Criteria cannot be null");
        checkArgument(!objectives.containsKey(name), "Objective \"" + name + "\" already exists");

        GlowObjective objective = new GlowObjective(this, name, criteria);
        objectives.put(name, objective);
        getForCriteria(criteria).add(objective);

        broadcast(ScoreboardObjectiveMessage.create(name, objective.getDisplayName(), RenderType.INTEGER));

        return objective;
    }

    public Objective getObjective(String name) throws IllegalArgumentException {
        return objectives.get(name);
    }

	// javac(1) says 'inconvertible types', converting to raw type instead
	@SuppressWarnings("unchecked")
    public Set<Objective> getObjectivesByCriteria(String criteria) throws IllegalArgumentException {
        return ImmutableSet.copyOf((Set)getForCriteria(criteria));
    }

	@SuppressWarnings("unchecked")
    public Set<Objective> getObjectives() {
        return ImmutableSet.copyOf((Collection)objectives.values());
    }

    public Objective getObjective(DisplaySlot slot) throws IllegalArgumentException {
        checkNotNull(slot, "Slot cannot be null");
        return displaySlots.get(slot);
    }

    public void clearSlot(DisplaySlot slot) throws IllegalArgumentException {
        checkNotNull(slot, "Slot cannot be null");
        setDisplaySlot(slot, null);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Teams

    public Team registerNewTeam(String name) throws IllegalArgumentException {
        checkNotNull(name, "Name cannot be null");
        checkArgument(!teams.containsKey(name), "Team \"" + name + "\" already exists");

        GlowTeam team = new GlowTeam(this, name);
        teams.put(name, team);
        broadcast(team.getCreateMessage());
        return team;
    }

    public Team getPlayerTeam(OfflinePlayer player) throws IllegalArgumentException {
        checkNotNull(player, "Player cannot be null");
        return entryTeams.get(player.getName());
    }

    @Override
    public Team getEntryTeam(String entry) throws IllegalArgumentException {
        checkNotNull(entry, "entry cannot be null");
        return entryTeams.get(entry);
    }

    public Team getTeam(String teamName) throws IllegalArgumentException {
        checkNotNull(teamName, "Team name cannot be null");
        return teams.get(teamName);
    }

	@SuppressWarnings("unchecked")
    public Set<Team> getTeams() {
        return ImmutableSet.copyOf((Collection)teams.values());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Scores

    public Set<String> getEntries() {
        return ImmutableSet.copyOf(scoreMap.keySet());
    }

	@SuppressWarnings("unchecked")
    public Set<Score> getScores(String entry) throws IllegalArgumentException {
        checkNotNull(entry, "Entry cannot be null");

        Set<GlowScore> scoreSet = scoreMap.get(entry);
        if (scoreSet == null) {
            return ImmutableSet.of();
        } else {
            return ImmutableSet.copyOf((Set)scoreSet);
        }
    }

    public void resetScores(String entry) throws IllegalArgumentException {
        checkNotNull(entry, "Entry cannot be null");

        for (GlowObjective objective : objectives.values()) {
            broadcast(ScoreboardScoreMessage.remove(entry, objective.getName()));
            objective.deleteScore(entry);
        }
        scoreMap.remove(entry);
    }

    ////////////////////////////////////////////////////////////////////////////
    // OfflinePlayer methods

    @Deprecated
    public Set<OfflinePlayer> getPlayers() {
        Set<OfflinePlayer> result = new HashSet<>();
		for(String name : getEntries()) {
			result.add(Bukkit.getOfflinePlayer(name));
		}
        return Collections.unmodifiableSet(result);
    }

    @Deprecated
    public Set<Score> getScores(OfflinePlayer player) throws IllegalArgumentException {
        checkNotNull(player, "Player cannot be null");
        return getScores(player.getName());
    }

    @Deprecated
    public void resetScores(OfflinePlayer player) throws IllegalArgumentException {
        checkNotNull(player, "Player cannot be null");
        resetScores(player.getName());
    }
}
