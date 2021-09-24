package me.davidml16.aparkour.handlers;

import me.davidml16.aparkour.data.LeaderboardEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LeaderboardHandler {

    private final Map<String, List<LeaderboardEntry>> leaderboards;

    private Map<UUID, String> playerNames;

    public LeaderboardHandler() {
        this.leaderboards = new HashMap<>();
        this.playerNames = new HashMap<>();
    }

    public List<LeaderboardEntry> getLeaderboard(String parkour) {
        return leaderboards.get(parkour);
    }

    public void addLeaderboard(String id, List<LeaderboardEntry> times) {
        leaderboards.put(id, times);
    }

    public Map<UUID, String> getPlayerNames() {
        return playerNames;
    }

    public void setPlayerNames(Map<UUID, String> playerNames) {
        this.playerNames = playerNames;
    }
}
