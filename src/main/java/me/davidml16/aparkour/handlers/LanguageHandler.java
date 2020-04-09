package me.davidml16.aparkour.handlers;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import me.davidml16.aparkour.managers.ColorManager;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.davidml16.aparkour.Main;

public class LanguageHandler {

	private String language = null;

	private HashMap<String, String> messages;

	private Main main;

	public LanguageHandler(Main main, String language) {
		this.main = main;
		new File(main.getDataFolder().toString() + "/language").mkdirs();
		loadLanguage("en");
		loadLanguage("es");
		this.language = checkLanguage(language);
		this.messages = new HashMap<String, String>();
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getPrefix() {
		return ColorManager.translate(messages.get("Prefix"));
	}

	public String getMessage(String message) {
		return ColorManager.translate(messages.get(message).replaceAll("%prefix%", messages.get("Prefix")));
	}

	public String checkLanguage(String lang) {
		File f = new File("plugins/AParkour/language/messages_" + lang + ".yml");
		if(f.exists())
			return lang;
		return "en";
	}

	public void pushMessages() {
		Main.log.sendMessage(ColorManager.translate(""));
		Main.log.sendMessage(ColorManager.translate("  &eLoading language:"));

		File f = new File("plugins/AParkour/language/messages_" + language + ".yml");
		YamlConfiguration c = YamlConfiguration.loadConfiguration(f);

		for(String key : c.getKeys(true)) {
			if(!(c.get(key) instanceof MemorySection)) {
				messages.put(key, c.getString(key));
			}
		}

		Main.log.sendMessage(ColorManager.translate("    &a'" + language + "' loaded!"));
	}

	public void loadLanguage(String lang) {
		File file = new File(main.getDataFolder() + "/language/messages_" + lang + ".yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

		cfg.options().header("\nThis is the messsages file.\nYou can change any messages that are in this file\n\nIf you want to reset a message back to the default,\ndelete the entire line the message is on and restart the server.\n\t");

		Map<String, String> msgDefaults = new LinkedHashMap<String, String>();

		InputStreamReader input = new InputStreamReader(main.getResource("language/messages_" + lang + ".yml"));
		FileConfiguration data = YamlConfiguration.loadConfiguration(input);


		for(String key : data.getKeys(true)) {
			if(!(data.get(key) instanceof MemorySection)) {
				msgDefaults.put(key, data.getString(key));
			}
		}

		for (String key : msgDefaults.keySet()) {
			if (!cfg.isSet(key)) {
				cfg.set(key, msgDefaults.get(key));
			}
		}

		for(String key : cfg.getKeys(true)) {
			if(!(cfg.get(key) instanceof MemorySection)) {
				if (!data.isSet(key)) {
					cfg.set(key, null);
				}
			}
		}

		try {
			cfg.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}