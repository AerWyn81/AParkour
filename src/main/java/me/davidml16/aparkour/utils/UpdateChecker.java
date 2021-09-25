package me.davidml16.aparkour.utils;

import me.davidml16.aparkour.Main;
import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class UpdateChecker {

    private final Main plugin;
    private final String versionUrl = "https://api.github.com/repos/AerWyn81/AParkour/releases/latest";

    public UpdateChecker(Main plugin) {
        this.plugin = plugin;
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
                InputStream is = new URL(versionUrl).openStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                JSONObject json = (JSONObject) new JSONParser().parse(readAll(rd));

                is.close();
                consumer.accept((String) json.get("tag_name"));
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        });
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}