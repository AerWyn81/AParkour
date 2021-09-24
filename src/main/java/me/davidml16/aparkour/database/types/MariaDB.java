package me.davidml16.aparkour.database.types;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import me.davidml16.aparkour.Main;
import me.davidml16.aparkour.data.LeaderboardEntry;
import me.davidml16.aparkour.managers.ColorManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MariaDB implements Database {

    private HikariDataSource hikari;

    private final String host, user, password, database;
    private final int port;

    private final Main main;

    public MariaDB(Main main) {
        this.main = main;
        this.host = main.getConfig().getString("MySQL.Host");
        this.user = main.getConfig().getString("MySQL.User");
        this.password = main.getConfig().getString("MySQL.Password");
        this.database = main.getConfig().getString("MySQL.Database");
        this.port = main.getConfig().getInt("MySQL.Port");
    }

    @Override
    public void close() {
        if(hikari != null) {
            hikari.close();
        }
    }

    @Override
    public void open() {
        if (hikari != null)  return;

        try {
            HikariConfig config = new HikariConfig();
            config.setPoolName("    AParkour Pool MariaDB");
            config.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
            config.addDataSourceProperty("serverName", this.host);
            config.addDataSourceProperty("port", port);
            config.addDataSourceProperty("databaseName", this.database);
            config.setUsername(this.user);
            config.setPassword(this.password);

            hikari = new HikariDataSource(config);

            Main.log.sendMessage(ColorManager.translate("    &aMariaDB has been enabled!"));
        } catch (HikariPool.PoolInitializationException e) {
            Main.log.sendMessage(ColorManager.translate("    &cMariaDB has an error on the conection! Now trying with SQLite..."));
            main.getDatabase().changeToSQLite();
        }
    }

    @Override
    public void loadTables() {
        PreparedStatement statement = null;
        Connection connection = null;
        try {
            connection = hikari.getConnection();
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
            if(connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        PreparedStatement statement2 = null;
        Connection connection2 = null;
        try {
            connection2 = hikari.getConnection();
            statement2 = connection2.prepareStatement("CREATE TABLE IF NOT EXISTS ap_playernames (`UUID` varchar(40) NOT NULL, `NAME` varchar(40), PRIMARY KEY (`UUID`));");
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
            if(connection2 != null) {
                try {
                    connection2.close();
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
            Connection connection = null;
            try {
                connection = hikari.getConnection();
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
                if (connection != null) {
                    try {
                        connection.close();
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
        Connection connection = null;
        try {
            connection = hikari.getConnection();
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
            if(connection != null) connection.close();
        }

        return false;
    }

    @Override
    public void createData(UUID uuid, String parkour) throws SQLException {
        PreparedStatement ps = null;
        Connection connection = null;
        try {
            connection = hikari.getConnection();
            ps = connection.prepareStatement("REPLACE INTO ap_times (UUID,parkourID,lastTime,bestTime) VALUES(?,?,0,0)");
            ps.setString(1, uuid.toString());
            ps.setString(2, parkour);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(ps != null) ps.close();
            if(connection != null) connection.close();
        }
    }

    @Override
    public boolean hasName(Player p) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection connection = null;
        try {
            connection = hikari.getConnection();
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
            if(connection != null) connection.close();
        }

        return false;
    }

    @Override
    public void updatePlayerName(Player p) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            PreparedStatement ps = null;
            Connection connection = null;
            try {
                connection = hikari.getConnection();
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
                if (connection != null) {
                    try {
                        connection.close();
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
        Connection connection = null;
        try {
            connection =  hikari.getConnection();
            ps = connection.prepareStatement("SELECT * FROM ap_playernames WHERE UUID = '" + uuid + "';");
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("NAME");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(ps != null) ps.close();
            if(rs != null) rs.close();
            if(connection != null) connection.close();
        }


        return "";
    }

    @Override
    public String getPlayerUUID(String name) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection connection = null;
        try {
            connection =  hikari.getConnection();
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
            if(connection != null) connection.close();
        }

        return "";
    }

    @Override
    public Long getLastTime(UUID uuid, String parkour) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection connection = null;
        try {
            connection = hikari.getConnection();
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
            if(connection != null) connection.close();
        }

        return 0L;
    }

    @Override
    public Long getBestTime(UUID uuid, String parkour) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection connection = null;
        try {
            connection = hikari.getConnection();
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
            if(connection != null) connection.close();
        }

        return 0L;
    }

    @Override
    public void setTimes(UUID uuid, Long lastTime, Long bestTime, String parkour) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            PreparedStatement ps = null;
            Connection connection = null;
            try {
                connection = hikari.getConnection();
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
                if (connection != null) {
                    try {
                        connection.close();
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
            Connection connection = null;
            try {
                connection = hikari.getConnection();
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
                if(connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return result;
    }
}
