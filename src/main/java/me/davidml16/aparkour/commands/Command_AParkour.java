package me.davidml16.aparkour.commands;

import me.davidml16.aparkour.Main;
import me.davidml16.aparkour.api.events.ParkourReturnEvent;
import me.davidml16.aparkour.data.LeaderboardEntry;
import me.davidml16.aparkour.data.Parkour;
import me.davidml16.aparkour.data.ParkourSession;
import me.davidml16.aparkour.handlers.CheckpointsHandler;
import me.davidml16.aparkour.handlers.ParkourHandler;
import me.davidml16.aparkour.managers.ColorManager;
import me.davidml16.aparkour.utils.ActionBar;
import me.davidml16.aparkour.utils.MessageUtils;
import me.davidml16.aparkour.utils.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Command_AParkour implements CommandExecutor {

    private final Main main;
    public Command_AParkour(Main main) {
        this.main = main;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendCommandHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("stats")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ColorManager.translate("&cThis command only can be use by player!"));
                return true;
            }

            Player p = (Player) sender;

            if (!main.getPlayerDataHandler().playerHasPermission(p, "aparkour.commands.stats")) {
                p.sendMessage(ColorManager.translate(main.getLanguageHandler().getMessage("Commands.NoPerms")));
                return true;
            }

            if (main.getParkourHandler().getParkours().size() > 0) {
                main.getStatsGUI().open(p);
            } else {
                String message = main.getLanguageHandler().getMessage("Commands.NoStats");
                if(message.length() > 0)
                    p.sendMessage(message);
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("play")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ColorManager.translate("&cThis command only can be use by player!"));
                return true;
            }

            Player p = (Player) sender;

            if (!main.getPlayerDataHandler().playerHasPermission(p, "aparkour.commands.play")) {
                p.sendMessage(ColorManager.translate(main.getLanguageHandler().getMessage("Commands.NoPerms")));
                return true;
            }

            if(!main.isJoinByGUI()) return false;

            if (args.length == 1) {
                main.getPlayParkourGUI().open(p);
                return true;
            }

            String id = args[1].toLowerCase();
            if (!main.getParkourHandler().getParkourConfigs().containsKey(id)) {
                p.sendMessage(ColorManager.translate(
                        main.getLanguageHandler().getPrefix() + " &cThis parkour doesn't exists!"));
                return true;
            }

            Parkour parkour = main.getParkourHandler().getParkourById(id);
            p.teleport(parkour.getSpawn());

            return true;
        }

        if (args[0].equalsIgnoreCase("top")) {
            if ((sender instanceof Player) && !main.getPlayerDataHandler().playerHasPermission((Player) sender, "aparkour.commands.top")) {
                sender.sendMessage(ColorManager.translate(main.getLanguageHandler().getMessage("Commands.NoPerms")));
                return true;
            }

            if (args.length == 1) {
                sender.sendMessage(ColorManager.translate(
                        main.getLanguageHandler().getPrefix() + " &cUsage: /aparkour top [id]"));
                return true;
            }

            String id = args[1].toLowerCase();
            if (!main.getParkourHandler().getParkourConfigs().containsKey(id)) {
                sender.sendMessage(ColorManager.translate(
                        main.getLanguageHandler().getPrefix() + " &cThis parkour doesn't exists!"));
                return true;
            }

            Parkour parkour = main.getParkourHandler().getParkourById(id);

            List<LeaderboardEntry> leaderboard = main.getLeaderboardHandler().getLeaderboard(id);

            sender.sendMessage("");

            sender.sendMessage(MessageUtils.centeredMessage(main.getLanguageHandler()
                    .getMessage("TopChat.Header.Line1").replaceAll("%parkour%", parkour.getName())));
            sender.sendMessage(MessageUtils.centeredMessage(main.getLanguageHandler()
                    .getMessage("TopChat.Header.Line2").replaceAll("%parkour%", parkour.getName())));

            sender.sendMessage("");

            if(leaderboard != null) {
                int i = 0;
                for (LeaderboardEntry entry : leaderboard) {
                    String line = main.getLanguageHandler()
                            .getMessage("TopChat.Entry.Time")
                            .replaceAll("%player%", main.getPlayerDataHandler().getPlayerName(parkour.getSpawn().getWorld(), entry.getName()))
                            .replaceAll("%position%", Integer.toString(i + 1))
                            .replaceAll("%time%", main.getTimerManager().millisToString(main.getLanguageHandler().getMessage("Timer.Formats.ParkourTimer"), entry.getTime()));

                    sender.sendMessage(MessageUtils.centeredMessage(line));
                    i++;
                }
                for (int j = i; j < 10; j++) {
                    String line = main.getLanguageHandler()
                            .getMessage("TopChat.Entry.NoTime").replaceAll("%position%", Integer.toString(j + 1));
                    sender.sendMessage(MessageUtils.centeredMessage(line));
                }
            } else {
                for (int i = 0; i < 10; i++) {
                    String line = main.getLanguageHandler()
                            .getMessage("TopChat.Entry.NoTime").replaceAll("%position%", Integer.toString(i + 1));
                    sender.sendMessage(MessageUtils.centeredMessage(line));
                }
            }

            sender.sendMessage("");

            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            if ((sender instanceof Player) && !main.getPlayerDataHandler().playerHasPermission((Player) sender, "aparkour.commands.list")) {
                sender.sendMessage(ColorManager.translate(main.getLanguageHandler().getMessage("Commands.NoPerms")));
                return true;
            }

            sendParkourList(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {
            if (args.length > 1) {
                String player = args[1];
                Player onlinePlayer = Bukkit.getPlayer(player);

                if(onlinePlayer == null) {
                    sender.sendMessage(ColorManager.translate(
                            main.getLanguageHandler().getPrefix() + " &cThis player not exists !"));
                    return true;
                }

                if ((sender instanceof Player) && !main.getPlayerDataHandler().playerHasPermission((Player) sender, "aparkour.admin")) {
                    sender.sendMessage(ColorManager.translate(main.getLanguageHandler().getMessage("Commands.NoPerms")));
                    return true;
                }

                if (args.length > 2) {
                    String id = args[2].toLowerCase();
                    if (!main.getParkourHandler().getParkourConfigs().containsKey(id)) {
                        sender.sendMessage(ColorManager.translate(
                                main.getLanguageHandler().getPrefix() + " &cThis parkour doesn't exists!"));
                        return true;
                    }

                    if (args.length > 3) {
                        ParkourHandler.startParkour(sender, onlinePlayer, main.getParkourHandler().getParkourById(id), Boolean.parseBoolean(args[3]));
                        return true;
                    }

                    ParkourHandler.startParkour(sender, onlinePlayer, main.getParkourHandler().getParkourById(id), false);
                    return true;
                }
            }
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!(sender instanceof Player)) {
                main.reloadConfig();
                main.getPluginManager().reloadAll();

                String message = main.getLanguageHandler().getMessage("Commands.Reload");
                if(message.length() > 0)
                    sender.sendMessage(message);

                return true;
            } else {

                Player p = (Player) sender;

                if (!main.getPlayerDataHandler().playerHasPermission(p, "aparkour.admin")) {
                    String message = main.getLanguageHandler().getMessage("Commands.NoPerms");
                    if(message.length() > 0)
                        p.sendMessage(message);
                    return false;
                }

                main.reloadConfig();
                main.getPluginManager().reloadAll();

                String message = main.getLanguageHandler().getMessage("Commands.Reload");
                if(message.length() > 0)
                    sender.sendMessage(message);

            }

            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ColorManager.translate("&cThis command only can be use by player!"));
                return true;
            }

            Player p = (Player) sender;

            if (!main.getPlayerDataHandler().playerHasPermission(p, "aparkour.admin")) {
                String message = main.getLanguageHandler().getMessage("Commands.NoPerms");
                if(message.length() > 0)
                    p.sendMessage(message);
                return false;
            }

            if (args.length == 1 || args.length == 2) {
                p.sendMessage(ColorManager.translate(
                        main.getLanguageHandler().getPrefix() + " &cUsage: /aparkour create [id] [name]"));
                return false;
            }

            String id = args[1].toLowerCase();
            if(!Character.isDigit(id.charAt(0))) {
                if (main.getParkourHandler().parkourExists(id)) {
                    p.sendMessage(ColorManager.translate(
                            main.getLanguageHandler().getPrefix() + " &cThis parkour already exists!"));
                    return true;
                }

                if(main.getParkourHandler().createParkour(id)) {
                    FileConfiguration config = main.getParkourHandler().getConfig(id);
                    config.set("parkour.name", args[2]);
                    config.set("parkour.icon", XMaterial.ITEM_FRAME.name());
                    config.set("parkour.plateHolograms.start.enabled", false);
                    config.set("parkour.plateHolograms.start.distanceBelowPlate", 2.5D);
                    config.set("parkour.plateHolograms.end.enabled", false);
                    config.set("parkour.plateHolograms.end.distanceBelowPlate", 2.5D);
                    config.set("parkour.plateHolograms.checkpoints.enabled", false);
                    config.set("parkour.plateHolograms.checkpoints.distanceBelowPlate", 2.5D);
                    config.set("parkour.titles.start.enabled", false);
                    config.set("parkour.titles.end.enabled", false);
                    config.set("parkour.titles.checkpoint.enabled", false);
                    config.set("parkour.permissionRequired.enabled", false);
                    config.set("parkour.permissionRequired.permission", "aparkour.permission." + id);
                    config.set("parkour.permissionRequired.message", "&cYou dont have permission to start this parkour!");
                    config.set("parkour.rewards.example.firstTime", true);
                    config.set("parkour.rewards.example.permission", "*");
                    config.set("parkour.rewards.example.command", "give %player% diamond 1");
                    config.set("parkour.rewards.example.chance", 100);
                    config.set("parkour.walkableBlocks", new ArrayList<>());
                    config.set("parkour.checkpoints", new ArrayList<>());
                    main.getParkourHandler().saveConfig(id);
                    main.getConfigGUI().loadGUI(id);
                    main.getWalkableBlocksGUI().loadGUI(id);
                    main.getRewardsGUI().loadGUI(id);
                    p.sendMessage(ColorManager.translate(main.getLanguageHandler().getPrefix()
                            + " &aSuccesfully created parkour &e" + id + " &awith the name &e" + args[2]));
                }
                return true;
            } else {
                p.sendMessage(ColorManager.translate(
                        main.getLanguageHandler().getPrefix() + " &cThe parkour id cannot start with a number, use for example 'p1'."));
                return false;
            }
        }

        if (args[0].equalsIgnoreCase("reset")) {
            if (!(sender instanceof Player)) {
                if (args.length == 1 || args.length == 2) {
                    sender.sendMessage(ColorManager.translate(
                            main.getLanguageHandler().getPrefix() + " &cUsage: /aparkour reset [player] [parkourID]"));
                    return false;
                }

                String player = args[1];
                String uuid = "";

                try {
                    uuid = main.getDatabaseHandler().getPlayerUUID(player);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

                if(uuid.equalsIgnoreCase("")) {
                    sender.sendMessage(ColorManager.translate(
                            main.getLanguageHandler().getPrefix() + " &cThis player not exists in the database!"));
                    return false;
                }

                String id = args[2].toLowerCase();
                if (!main.getParkourHandler().getParkourConfigs().containsKey(id)) {
                    sender.sendMessage(ColorManager.translate(
                            main.getLanguageHandler().getPrefix() + " &cThis parkour doesn't exists!"));
                    return true;
                }

                if(Bukkit.getPlayer(player) == null) {

                    try {
                        main.getDatabaseHandler().setTimes(UUID.fromString(uuid), 0L, 0L, id);
                        sender.sendMessage(ColorManager.translate(
                                main.getLanguageHandler().getPrefix() + " &aSuccesfully reseted times of parkour &e" + id + " &ato player &e" + player));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                } else {

                    Player target = Bukkit.getPlayer(player);

                    main.getPlayerDataHandler().getData(target).setLastTime(0L, id);
                    main.getPlayerDataHandler().getData(target).setBestTime(0L, id);
                    try {
                        main.getDatabaseHandler().setTimes(target.getUniqueId(), 0L, 0L, id);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    main.getStatsHologramManager().reloadStatsHologram(target, id);

                    sender.sendMessage(ColorManager.translate(
                            main.getLanguageHandler().getPrefix() + " &aSuccesfully reseted times of parkour &e" + id + " &ato player &e" + target.getName()));

                }

                return true;

            } else {

                Player p = (Player) sender;

                if (!main.getPlayerDataHandler().playerHasPermission(p, "aparkour.admin")) {
                    String message = main.getLanguageHandler().getMessage("Commands.NoPerms");
                    if(message.length() > 0)
                        p.sendMessage(message);
                    return false;
                }

                if (args.length == 1 || args.length == 2) {
                    p.sendMessage(ColorManager.translate(
                            main.getLanguageHandler().getPrefix() + " &cUsage: /aparkour reset [player] [parkourID]"));
                    return false;
                }

                String player = args[1];
                String uuid = "";

                try {
                    uuid = main.getDatabaseHandler().getPlayerUUID(player);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

                if(uuid.equalsIgnoreCase("")) {
                    sender.sendMessage(ColorManager.translate(
                            main.getLanguageHandler().getPrefix() + " &cThis player not exists in the database!"));
                    return false;
                }

                String id = args[2].toLowerCase();
                if (!main.getParkourHandler().getParkourConfigs().containsKey(id)) {
                    p.sendMessage(ColorManager.translate(
                            main.getLanguageHandler().getPrefix() + " &cThis parkour doesn't exists!"));
                    return true;
                }

                if(Bukkit.getPlayer(player) == null) {

                    try {
                        main.getDatabaseHandler().setTimes(UUID.fromString(uuid), 0L, 0L, id);
                        p.sendMessage(ColorManager.translate(
                                main.getLanguageHandler().getPrefix() + " &aSuccesfully reseted times of parkour &e" + id + " &ato player &e" + player));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                } else {

                    Player target = Bukkit.getPlayer(player);

                    main.getPlayerDataHandler().getData(target).setLastTime(0L, id);
                    main.getPlayerDataHandler().getData(target).setBestTime(0L, id);
                    try {
                        main.getDatabaseHandler().setTimes(target.getUniqueId(), 0L, 0L, id);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    main.getStatsHologramManager().reloadStatsHologram(target, id);

                    p.sendMessage(ColorManager.translate(
                            main.getLanguageHandler().getPrefix() + " &aSuccesfully reseted times of parkour &e" + id + " &ato player &e" + target.getName()));

                }

            }

            return true;
        }

        if (args[0].equalsIgnoreCase("setup")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ColorManager.translate("&cThis command only can be use by player!"));
                return true;
            }

            Player p = (Player) sender;

            if (!main.getPlayerDataHandler().playerHasPermission(p, "aparkour.admin")) {
                String message = main.getLanguageHandler().getMessage("Commands.NoPerms");
                if(message.length() > 0)
                    p.sendMessage(message);
                return false;
            }

            if (args.length == 1) {
                p.sendMessage(ColorManager.translate(
                        main.getLanguageHandler().getPrefix() + " &cUsage: /aparkour setup [id]"));
                return true;
            }

            String id = args[1].toLowerCase();
            if (!main.getParkourHandler().getParkourConfigs().containsKey(id)) {
                p.sendMessage(ColorManager.translate(
                        main.getLanguageHandler().getPrefix() + " &cThis parkour doesn't exists!"));
                return true;
            }

            main.getConfigGUI().open(p, id);
            return true;
        }

        if (args[0].equalsIgnoreCase("leave")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ColorManager.translate("&cThis command only can be use by player!"));
                return true;
            }

            Player p = (Player) sender;

            if (main.getTimerManager().hasPlayerTimer(p)) {
                ParkourSession session = main.getSessionHandler().getSession(p);
                p.teleport(session.getParkour().getSpawn());

                String message = main.getLanguageHandler().getMessage("Messages.Leave").replaceAll("%parkourName%", session.getParkour().getName());
                if(message.length() > 0)
                    p.sendMessage(message);

                main.getParkourHandler().resetPlayer(p);

                main.getSoundUtil().playReturn(p);

                Bukkit.getPluginManager().callEvent(new ParkourReturnEvent(p, session.getParkour()));
                return true;
            } else {
                String message = main.getLanguageHandler().getMessage("Messages.NotInParkour");
                if(message.length() > 0)
                    p.sendMessage(message);
                return false;
            }
        }

        if (args[0].equalsIgnoreCase("checkpoint")) {
            if ((sender instanceof Player) && !main.getPlayerDataHandler().playerHasPermission((Player) sender, "aparkour.commands.checkpoint")) {
                sender.sendMessage(ColorManager.translate(main.getLanguageHandler().getMessage("Commands.NoPerms")));
                return true;
            }

            if (args.length > 1) {
                String player = args[1];
                Player onlinePlayer = Bukkit.getPlayer(player);

                if(onlinePlayer == null) {
                    sender.sendMessage(ColorManager.translate(
                            main.getLanguageHandler().getPrefix() + " &cThis player not exists !"));
                    return false;
                }

                if (args.length > 2 && args[2].equalsIgnoreCase("previous")) {
                    return CheckpointsHandler.teleportToPreviousCheckpoint(sender, onlinePlayer);
                }

                if (args.length > 2 && args[2].equalsIgnoreCase("next")) {
                    return CheckpointsHandler.teleportToNextCheckpoint(sender, onlinePlayer);
                }

                sender.sendMessage(ColorManager.translate("&cThis command need a parameter 'next' or 'previous' !"));
                return false;
            }

            Player p = (Player) sender;
            CheckpointsHandler.teleportToPreviousCheckpoint(sender, p);
            return true;
        }

        if (args[0].equalsIgnoreCase("remove")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ColorManager.translate("&cThis command only can be use by player!"));
                return true;
            }

            Player p = (Player) sender;

            if (!main.getPlayerDataHandler().playerHasPermission(p, "aparkour.admin")) {
                String message = main.getLanguageHandler().getMessage("Commands.NoPerms");
                if(message.length() > 0)
                    p.sendMessage(message);
                return false;
            }

            if (args.length == 1) {
                p.sendMessage(ColorManager.translate(
                        main.getLanguageHandler().getPrefix() + " &cUsage: /aparkour remove [id]"));
                return true;
            }
            String id = args[1].toLowerCase();
            if (!main.getParkourHandler().getParkourConfigs().containsKey(id)) {
                p.sendMessage(ColorManager.translate(
                        main.getLanguageHandler().getPrefix() + " &cThis parkour doesn't exists!"));
                return true;
            }

            for (Player pl : Bukkit.getOnlinePlayers()) {
                main.getStatsHologramManager().removeStatsHologram(pl, id);
                if(main.getTimerManager().hasPlayerTimer(pl)) {
                    ParkourSession session = main.getSessionHandler().getSession(pl);
                    if(session.getParkour().getId().equals(id)) {
                        pl.teleport(session.getParkour().getSpawn());

                        if (main.getTimerManager().isActionBarEnabled()) {
                            ActionBar.sendActionBar(pl, " ");
                        }

                        main.getParkourHandler().resetPlayer(pl);

                        main.getSoundUtil().playFall(pl);

                        Bukkit.getPluginManager().callEvent(new ParkourReturnEvent(p, session.getParkour()));
                    }
                }
            }

            main.getTopHologramManager().removeHologram(id);
            main.getParkourHandler().removeHologram(id);

            if(main.getParkourHandler().removeParkour(id)) {
                main.getParkourHandler().getParkours().remove(id);

                if (main.getConfigGUI().getGuis().containsKey(id)) {
                    for (UUID uuid : main.getConfigGUI().getOpened().keySet()) {
                        if (main.getConfigGUI().getOpened().get(uuid).equals(id)) {
                            Bukkit.getPlayer(uuid).closeInventory();
                        }
                    }
                    main.getConfigGUI().getGuis().remove(id);
                }

                if (main.getWalkableBlocksGUI().getGuis().containsKey(id)) {
                    for (UUID uuid : main.getWalkableBlocksGUI().getOpened().keySet()) {
                        if (main.getWalkableBlocksGUI().getOpened().get(uuid).getParkour().equals(id)) {
                            Bukkit.getPlayer(uuid).closeInventory();
                        }
                    }
                    main.getWalkableBlocksGUI().getGuis().remove(id);
                }

                if (main.getRewardsGUI().getGuis().containsKey(id)) {
                    for (UUID uuid : main.getRewardsGUI().getOpened().keySet()) {
                        if (main.getRewardsGUI().getOpened().get(uuid).getParkour().equals(id)) {
                            Bukkit.getPlayer(uuid).closeInventory();
                        }
                    }
                    main.getRewardsGUI().getGuis().remove(id);
                }

                if (main.getCheckpointsGUI().getGuis().containsKey(id)) {
                    for (UUID uuid : main.getCheckpointsGUI().getOpened().keySet()) {
                        if (main.getCheckpointsGUI().getOpened().get(uuid).getParkour().equals(id)) {
                            Bukkit.getPlayer(uuid).closeInventory();
                        }
                    }
                    main.getRewardsGUI().getGuis().remove(id);
                }

                p.sendMessage(ColorManager.translate(
                        main.getLanguageHandler().getPrefix() + " &aSuccesfully deleted parkour &e" + id));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("finish")) {
            String player = args[1];
            Player onlinePlayer = Bukkit.getPlayer(player);

            if(onlinePlayer == null) {
                sender.sendMessage(ColorManager.translate(
                        main.getLanguageHandler().getPrefix() + " &cThis player not exists !"));
                return false;
            }

            if ((sender instanceof Player) && !main.getPlayerDataHandler().playerHasPermission((Player) sender, "aparkour.admin")) {
                sender.sendMessage(ColorManager.translate(main.getLanguageHandler().getMessage("Commands.NoPerms")));
                return true;
            }

            if (!main.getTimerManager().hasPlayerTimer(onlinePlayer)) {
                sender.sendMessage(ColorManager.translate(
                        main.getLanguageHandler().getPrefix() + " &c" + player + " is not in parkour !"));
                return true;
            }

            ParkourHandler.finishParkour(sender, onlinePlayer);
            return true;
        }

        sender.sendMessage(ColorManager.translate(main.getLanguageHandler().getMessage("Commands.Unknown")));
        return false;
    }

    private void sendParkourList(CommandSender p) {
        p.sendMessage("");
        p.sendMessage(ColorManager.translate("  &a&lParkours availables:"));
        p.sendMessage("");

        if (main.getParkourHandler().getParkours().size() == 0) {
            p.sendMessage(ColorManager.translate("    &7- &c&lAny parkour created!"));
        } else {
            for (Parkour parkour : main.getParkourHandler().getParkours().values()) {
                p.sendMessage(ColorManager.translate("    &7- &e&lID: &a&l" + parkour.getId() + " &7&l| &e&lName: &a&l" + parkour.getName()));
            }
        }

        p.sendMessage("");
    }

    private void sendCommandHelp(CommandSender sender) {
        String message;

        message = main.getLanguageHandler().getMessage("Help.Title");
        if (message.length() > 0) {
            sender.sendMessage(ColorManager.translate(message));
        }

        message = main.getLanguageHandler().getMessage("Help.HeadLine");
        if (message.length() > 0) {
            sender.sendMessage(ColorManager.translate(message));
        }

        message = main.getLanguageHandler().getMessage("Help.Help");
        if (message.length() > 0) {
            sender.sendMessage(ColorManager.translate(message));
        }

        if (!(sender instanceof Player) || main.getPlayerDataHandler().playerHasPermission((Player) sender, "aparkour.commands.stats")) {
            message = main.getLanguageHandler().getMessage("Help.Stats");
            if (message.length() > 0) {
                sender.sendMessage(ColorManager.translate(message));
            }
        }

        if (!(sender instanceof Player) || main.getPlayerDataHandler().playerHasPermission((Player) sender, "aparkour.commands.list")) {
            message = main.getLanguageHandler().getMessage("Help.List");
            if (message.length() > 0) {
                sender.sendMessage(ColorManager.translate(message));
            }
        }

        if (!(sender instanceof Player) || main.getPlayerDataHandler().playerHasPermission((Player) sender, "aparkour.commands.top")) {
            message = main.getLanguageHandler().getMessage("Help.Top");
            if (message.length() > 0) {
                sender.sendMessage(ColorManager.translate(message));
            }
        }

        if ((sender instanceof Player) && main.getTimerManager().hasPlayerTimer((Player) sender)) {
            if (main.getPlayerDataHandler().playerHasPermission((Player) sender, "aparkour.commands.checkpoint")) {
                message = main.getLanguageHandler().getMessage("Help.Checkpoint");
                if (message.length() > 0) {
                    sender.sendMessage(ColorManager.translate(message));
                }
            }

            if (main.getPlayerDataHandler().playerHasPermission((Player) sender, "aparkour.commands.leave")) {
                message = main.getLanguageHandler().getMessage("Help.Leave");
                if (message.length() > 0) {
                    sender.sendMessage(ColorManager.translate(message));
                }
            }
        }

        if (main.isJoinByGUI()) {
            if ((sender instanceof Player) && main.getPlayerDataHandler().playerHasPermission((Player) sender, "aparkour.commands.play")){
                message = main.getLanguageHandler().getMessage("Help.Play");
                if (message.length() > 0) {
                    sender.sendMessage(ColorManager.translate(message));
                }
            }
        }

        if (!(sender instanceof Player) || main.getPlayerDataHandler().playerHasPermission((Player) sender, "aparkour.admin")) {
            message = main.getLanguageHandler().getMessage("Help.Create");
            if (message.length() > 0) {
                sender.sendMessage(ColorManager.translate(message));
            }

            message = main.getLanguageHandler().getMessage("Help.Remove");
            if (message.length() > 0) {
                sender.sendMessage(ColorManager.translate(message));
            }

            message = main.getLanguageHandler().getMessage("Help.Start");
            if (message.length() > 0) {
                sender.sendMessage(ColorManager.translate(message));
            }

            message = main.getLanguageHandler().getMessage("Help.CheckpointAdmin");
            if (message.length() > 0) {
                sender.sendMessage(ColorManager.translate(message));
            }

            message = main.getLanguageHandler().getMessage("Help.Finish");
            if (message.length() > 0) {
                sender.sendMessage(ColorManager.translate(message));
            }

            message = main.getLanguageHandler().getMessage("Help.Reset");
            if (message.length() > 0) {
                sender.sendMessage(ColorManager.translate(message));
            }

            message = main.getLanguageHandler().getMessage("Help.Setup");
            if (message.length() > 0) {
                sender.sendMessage(ColorManager.translate(message));
            }

            message = main.getLanguageHandler().getMessage("Help.Reload");
            if (message.length() > 0) {
                sender.sendMessage(ColorManager.translate(message));
            }

        }

        message = main.getLanguageHandler().getMessage("Help.FooterLine");
        if (message.length() > 0) {
            sender.sendMessage(ColorManager.translate(message));
        }

        message = main.getLanguageHandler().getMessage("Help.EndTitle");
        if (message.length() > 0) {
            sender.sendMessage(ColorManager.translate(message));
        }
    }
}
