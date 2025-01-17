package net.glowstone.scoreboard;

import net.glowstone.util.nbt.CompoundTag;
import net.glowstone.util.nbt.NBTInputStream;
import net.glowstone.util.nbt.TagType;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;

import java.io.*;
import java.util.List;

public class NbtScoreboardIoReader {

    public static GlowScoreboard readMainScoreboard(File path) throws IOException {
        CompoundTag root;

        try (NBTInputStream nbt = new NBTInputStream(getDataInputStream(path), true)) {
            root = nbt.readCompound().getCompound("data");
        }

        GlowScoreboard scoreboard = new GlowScoreboard();

        registerObjectives(root, scoreboard);
        registerScores(root, scoreboard);
        registerTeams(root, scoreboard);
        registerDisplaySlots(root, scoreboard);

        return scoreboard;
    }

    private static DataInputStream getDataInputStream(File path) throws FileNotFoundException {
        return new DataInputStream(new FileInputStream(path));
    }

    private static void registerObjectives(CompoundTag root, GlowScoreboard scoreboard) {
        if (root.containsKey("Objectives")) {
            List<CompoundTag> objectives = root.getCompoundList("Objectives");
            for (CompoundTag objective : objectives) {
                registerObjective(objective, scoreboard);
            }
        }
    }

    private static void registerObjective(CompoundTag data, GlowScoreboard scoreboard) {
        String criteria = data.getString("CriteriaName");
        String displayName = data.getString("DisplayName");
        String name = data.getString("Name");
        String renderType = data.getString("RenderType");

        GlowObjective objective = (GlowObjective) scoreboard.registerNewObjective(name, criteria);
        objective.setDisplayName(displayName);
        objective.setRenderType(renderType);
    }


    private static void registerScores(CompoundTag root, GlowScoreboard scoreboard) {
        if (root.containsKey("PlayerScores")) {
            List<CompoundTag> scores = root.getCompoundList("PlayerScores");
            for (CompoundTag score : scores) {
                registerScore(score, scoreboard);
            }
        }
    }

    private static void registerScore(CompoundTag data, GlowScoreboard scoreboard) {
        int scoreNum = data.getInt("Score");
        String name = data.getString("Name");
        String objective = data.getString("Objective");
        boolean locked = data.getByte("Locked") == 1;

        Score score = scoreboard.getObjective(objective).getScore(name);
        score.setScore(scoreNum);
        score.setLocked(locked);
    }

    private static void registerTeams(CompoundTag root, GlowScoreboard scoreboard) {
        if (root.containsKey("Teams")) {
            List<CompoundTag> teams = root.getCompoundList("Teams");
            for (CompoundTag team : teams) {
                registerTeam(team, scoreboard);
            }
        }
    }

    private static void registerTeam(CompoundTag data, GlowScoreboard scoreboard) {
        boolean allowFriendlyFire = data.getByte("AllowFriendlyFire") == 1;
        boolean seeFriendlyInvisibles = data.getByte("SeeFriendlyInvisibles") == 1;
        Team.OptionStatus nameTagVisibility = Team.OptionStatus.valueOf(data.getString("NameTagVisibility").toUpperCase());
        Team.OptionStatus deathMessageVisibility = Team.OptionStatus.ALWAYS;
        switch (data.getString("DeathMessageVisibility")) {
            case "never":
                deathMessageVisibility = Team.OptionStatus.NEVER;
                break;
            case "hideForOtherTeams":
                deathMessageVisibility = Team.OptionStatus.FOR_OTHER_TEAMS;
                break;
            case "hideForOwnTeam":
                deathMessageVisibility = Team.OptionStatus.FOR_OWN_TEAM;
                break;
        }
        Team.OptionStatus collisionRule = Team.OptionStatus.ALWAYS;
        switch (data.getString("CollisionRule")) {
            case "never":
                collisionRule = Team.OptionStatus.NEVER;
                break;
            case "pushOtherTeams":
                collisionRule = Team.OptionStatus.FOR_OTHER_TEAMS;
                break;
            case "pushOwnTeam":
                collisionRule = Team.OptionStatus.FOR_OWN_TEAM;
                break;
        }
        String displayName = data.getString("DisplayName");
        String name = data.getString("Name");
        String prefix = data.getString("Prefix");
        String suffix = data.getString("Suffix");
        ChatColor teamColor = null;
        if (data.containsKey("TeamColor")) {
            teamColor = ChatColor.valueOf(data.getString("TeamColor").toUpperCase());
        }

        List<String> players = data.getList("Players", TagType.STRING);

        GlowTeam team = (GlowTeam) scoreboard.registerNewTeam(name);
        team.setDisplayName(displayName);
        team.setPrefix(prefix);
        team.setSuffix(suffix);
        team.setAllowFriendlyFire(allowFriendlyFire);
        team.setCanSeeFriendlyInvisibles(seeFriendlyInvisibles);
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, nameTagVisibility);
        team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, deathMessageVisibility);
        team.setOption(Team.Option.COLLISION_RULE, collisionRule);
        if (teamColor != null) {
            team.setColor(teamColor);
        }

		for(String p : players) team.addEntry(p);
    }

    private static String getOrNull(String key, CompoundTag tag) {
        if (tag.isString(key)) {
            return tag.getString(key);
        }
        return null;
    }

    private static void registerDisplaySlots(CompoundTag root, GlowScoreboard scoreboard) {
        if (root.containsKey("DisplaySlots")) {
            CompoundTag data = root.getCompound("DisplaySlots");

            String list = getOrNull("slot_0", data);
            String sidebar = getOrNull("slot_1", data);
            String belowName = getOrNull("slot_2", data);

            if (list != null) {
                scoreboard.getObjective(list).setDisplaySlot(DisplaySlot.PLAYER_LIST);
            }

            if (sidebar != null) {
                scoreboard.getObjective(sidebar).setDisplaySlot(DisplaySlot.SIDEBAR);
            }

            if (belowName != null) {
                scoreboard.getObjective(belowName).setDisplaySlot(DisplaySlot.BELOW_NAME);
            }

            /* TODO: anything need to be done with team slots?
            String teamBlack = getOrNull("slot_3", data);
            String teamDarkBlue = getOrNull("slot_4", data);
            String teamDarkGreen = getOrNull("slot_5", data);
            String teamDarkAqua = getOrNull("slot_6", data);
            String teamDarkRed = getOrNull("slot_7", data);
            String teamDarkPurple = getOrNull("slot_8", data);
            String teamGold = getOrNull("slot_9", data);
            String teamGray = getOrNull("slot_10", data);
            String teamDarkGray = getOrNull("slot_11", data);
            String teamBlue = getOrNull("slot_12", data);
            String teamGreen = getOrNull("slot_13", data);
            String teamAqua = getOrNull("slot_14", data);
            String teamRed = getOrNull("slot_15", data);
            String teamLightPurple = getOrNull("slot_16", data);
            String teamYellow = getOrNull("slot_17", data);
            String teamWhite = getOrNull("slot_18", data);
            */
        }
    }
}
