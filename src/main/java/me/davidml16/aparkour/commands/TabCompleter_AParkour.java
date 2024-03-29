package me.davidml16.aparkour.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import me.davidml16.aparkour.Main;

public class TabCompleter_AParkour implements TabCompleter {

	private final Main main;
	public TabCompleter_AParkour(Main main) {
		this.main = main;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if (!(sender instanceof Player)) {
			return null;
		}

		Player p = (Player) sender;

		List<String> list = new ArrayList<String>();
		List<String> auto = new ArrayList<String>();

		if (args.length == 1) {

			list.add("help");

			if(main.isJoinByGUI() && main.getPlayerDataHandler().playerHasPermission(p, "aparkour.tab.play")) {
				list.add("play");
			}

			if (main.getTimerManager().hasPlayerTimer(p)) {
				list.add("leave");
				if (main.getPlayerDataHandler().playerHasPermission(p, "aparkour.tab.checkpoint")) {
					list.add("checkpoint");
				}
			}

			if (main.getPlayerDataHandler().playerHasPermission(p, "aparkour.tab.stats")) {
				list.add("stats");
			}
			if (main.getPlayerDataHandler().playerHasPermission(p, "aparkour.tab.top")) {
				list.add("top");
			}
			if (main.getPlayerDataHandler().playerHasPermission(p, "aparkour.tab.list")) {
				list.add("list");
			}

			if (main.getPlayerDataHandler().playerHasPermission(p, "aparkour.tab.admin")) {
				list.add("start");
				list.add("checkpoint");
				list.add("finish");
				list.add("create");
				list.add("remove");
				list.add("setup");
				list.add("reload");
				list.add("reset");
			}
		} else if (args[0].equalsIgnoreCase("remove")) {
			if (main.getPlayerDataHandler().playerHasPermission(p, "aparkour.admin")) {
				for (File file : Objects.requireNonNull(new File(main.getDataFolder(), "parkours").listFiles())) {
					list.add(file.getName().replace(".yml", ""));
				}
			}
		} else if (args[0].equalsIgnoreCase("setup")) {
			if (main.getPlayerDataHandler().playerHasPermission(p, "aparkour.admin")) {
				for (File file : Objects.requireNonNull(new File(main.getDataFolder(), "parkours").listFiles())) {
					list.add(file.getName().replace(".yml", ""));
				}
			}
		} else if (args[0].equalsIgnoreCase("play") || args[0].equalsIgnoreCase("top")) {
				list.addAll(main.getParkourHandler().getParkours().keySet());
		} else if (args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("start")) {
			if (args.length == 2) {
				if (main.getPlayerDataHandler().playerHasPermission(p, "aparkour.admin")) {
					for (Player target : main.getServer().getOnlinePlayers()) {
						list.add(target.getName());
					}
				}
			} else {
				if (main.getPlayerDataHandler().playerHasPermission(p, "aparkour.admin")) {
					list.addAll(main.getParkourHandler().getParkours().keySet());
				}
			}
		} else if (args[0].equalsIgnoreCase("finish")) {
			if (main.getPlayerDataHandler().playerHasPermission(p, "aparkour.admin")) {
				for (Player target : main.getServer().getOnlinePlayers()) {
					list.add(target.getName());
				}
			}
		} else if (args[0].equalsIgnoreCase("checkpoint")) {
			if (args.length == 3) {
				if (main.getPlayerDataHandler().playerHasPermission(p, "aparkour.admin")) {
					list.add("next");
					list.add("previous");
				}
			}
		}


		for (String s : list) {
			if (s.startsWith(args[args.length - 1])) {
				auto.add(s);
			}
		}

		return auto.isEmpty() ? list : auto;
	}
}