package me.davidml16.aparkour.database.types;

import me.davidml16.aparkour.Main;
import me.davidml16.aparkour.data.LeaderboardEntry;
import me.davidml16.aparkour.managers.ColorManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SQLite implements Database {

    private Connection connection;

    private Main main;

    public SQLite(Main main) {
        this.main = main;
    }

    @Override
    public void close() {
        if(connection != null) {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    @Override
    public void open() {
        if (connection != null)  return;

        File file = new File(main.getDataFolder(), "playerData.db");
        String URL = "jdbc:sqlite:" + file;

        synchronized (this) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(URL);
                Main.log.sendMessage(ColorManager.translate("    &aSQLite has been enabled!"));
            } catch (SQLException | ClassNotFoundException e) {
                Main.log.sendMessage(ColorManager.translate("    &cSQLite has an error on the conection! Plugin disabled : Database needed"));
                Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin("AParkour"));
            }
        }
    }

    @Override
    public void loadTables() {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS ap_times (`UUID` varchar(40) NOT NULL, `parkourID` varchar(25) NOT NULL, `lastTime` bigint NOT NULL, `bestTime` bigint NOT NULL, PRIMARY KEY (`UUID`, `parkourID`));");
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        PreparedStatement statement2 = null;
        try {
            statement2 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS ap_playernames (`UUID` varchar(40) NOT NULL, `NAME` varchar(40), PRIMARY KEY (`UUID`));");
            statement2.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(statement2 != null) {
                try {
                    statement2.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void deleteParkourRows(String parkour) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            PreparedStatement ps = null;
            try {
                ps = connection.prepareStatement("DELETE FROM ap_times WHERE parkourID = '" + parkour + "'");
                ps.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public boolean hasData(UUID uuid, String parkour) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement("SELECT * FROM ap_times WHERE UUID = '" + uuid.toString() + "' AND parkourID = '" + parkour + "';");
            rs = ps.executeQuery();

            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(ps != null) ps.close();
            if(rs != null) rs.close();
        }

        return false;
    }

    @Override
    public void createData(UUID uuid, String parkour) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("REPLACE INTO ap_times (UUID,parkourID,lastTime,bestTime) VALUES(?,?,0,0)");
            ps.setString(1, uuid.toString());
            ps.setString(2, parkour);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(ps != null) ps.close();
        }
    }

    @Override
    public boolean hasName(Player p) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement("SELECT * FROM ap_playernames WHERE UUID = '" + p.getUniqueId().toString() + "';");
            rs = ps.executeQuery();

            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(ps != null) ps.close();
            if(rs != null) rs.close();
        }

        return false;
    }

    @Override
    public void updatePlayerName(Player p) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            PreparedStatement ps = null;
            try {
                ps = connection.prepareStatement("REPLACE INTO ap_playernames (UUID,NAME) VALUES(?,?)");
                ps.setString(1, p.getUniqueId().toString());
                ps.setString(2, p.getName());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public String getPlayerName(String uuid) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = connection.prepareStatement("SELECT * FROM ap_playernames WHERE UUID = '" + uuid + "';");
        rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getString("NAME");
        }

        return "";
    }

    @Override
    public String getPlayerUUID(String name) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT * FROM ap_playernames WHERE NAME = '" + name + "';");
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("UUID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(ps != null) ps.close();
            if(rs != null) rs.close();
        }

        return "";
    }

    @Override
    public Long getLastTime(UUID uuid, String parkour) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement("SELECT * FROM ap_times WHERE UUID = '" + uuid.toString() + "' AND parkourID = '" + parkour + "';");

            rs = ps.executeQuery();
            while (rs.next()) {
                return rs.getLong("lastTime");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(ps != null) ps.close();
            if(rs != null) rs.close();
        }

        return 0L;
    }

    @Override
    public Long getBestTime(UUID uuid, String parkour) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement("SELECT * FROM ap_times WHERE UUID = '" + uuid.toString() + "' AND parkourID = '" + parkour + "';");

            rs = ps.executeQuery();
            while (rs.next()) {
                return rs.getLong("bestTime");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(ps != null) ps.close();
            if(rs != null) rs.close();
        }

        return 0L;
    }

    @Override
    public void setTimes(UUID uuid, Long lastTime, Long bestTime, String parkour) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            PreparedStatement ps = null;
            try {
                ps = connection.prepareStatement("REPLACE INTO ap_times (UUID,parkourID,lastTime,bestTime) VALUES(?,?,?,?)");
                ps.setString(1, uuid.toString());
                ps.setString(2, parkour);
                ps.setLong(3, lastTime);
                ps.setLong(4, bestTime);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public CompletableFuture<Map<String, Long>> getPlayerLastTimes(UUID uuid) {
        CompletableFuture<Map<String, Long>> result = new CompletableFuture<>();
        HashMap<String, Long> times = new HashMap<String, Long>();
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            for (File file : Objects.requireNonNull(new File(main.getDataFolder(), "parkours").listFiles())) {
                String parkour = file.getName().replace(".yml", "");
                try {
                    if (hasData(uuid, parkour)) {
                        times.put(parkour, getLastTime(uuid, parkour));
                    } else {
                        createData(uuid, parkour);
                        times.put(parkour, 0L);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            result.complete(times);
        });
        return result;
    }

    @Override
    public CompletableFuture<Map<String, Long>> getPlayerBestTimes(UUID uuid) {
        CompletableFuture<Map<String, Long>> result = new CompletableFuture<>();
        HashMap<String, Long> times = new HashMap<String, Long>();
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            for (File file : Objects.requireNonNull(new File(main.getDataFolder(), "parkours").listFiles())) {
                String parkour = file.getName().replace(".yml", "");
                try {
                    if (hasData(uuid, parkour)) {
                        times.put(parkour, getBestTime(uuid, parkour));
                    } else {
                        createData(uuid, parkour);
                        times.put(parkour, 0L);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            result.complete(times);
        });
        return result;
    }

    @Override
    public CompletableFuture<List<LeaderboardEntry>> getParkourBestTimes(String id, int amount) {
        CompletableFuture<List<LeaderboardEntry>> result = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            PreparedStatement ps = null;
            ResultSet rs = null;
            List<LeaderboardEntry> times = new ArrayList<>();
            try {
                ps = connection.prepareStatement("SELECT * FROM ap_times WHERE bestTime != 0 AND parkourID = '" + id + "' ORDER BY bestTime ASC LIMIT " + amount + ";");

                rs = ps.executeQuery();
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("UUID"));
                    if(main.getLeaderboardHandler().getPlayerNames().containsKey(uuid)) {
                        times.add(new LeaderboardEntry(main.getLeaderboardHandler().getPlayerNames().get(uuid), rs.getLong("bestTime")));
                    } else {
                        String playerName = getPlayerName(uuid.toString());
                        times.add(new LeaderboardEntry(playerName, rs.getLong("bestTime")));
                        main.getLeaderboardHandler().getPlayerNames().put(uuid, playerName);
                    }
                }

                Bukkit.getScheduler().runTask(main, () -> result.complete(times));
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if(ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if(rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return result;
    }

}
