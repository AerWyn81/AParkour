package me.davidml16.aparkour.database;

import me.davidml16.aparkour.database.types.Database;

import me.davidml16.aparkour.Main;
import me.davidml16.aparkour.database.types.MariaDB;
import me.davidml16.aparkour.database.types.MySQL;
import me.davidml16.aparkour.database.types.SQLite;
import me.davidml16.aparkour.managers.ColorManager;

public class DatabaseHandler {

	private Database database;

	private Main main;

	public DatabaseHandler(Main main) {
		this.main = main;

		DatabaseType databaseType = DatabaseType.valueOf(main.getConfig().getString("MySQL.Type", "NONE"));

		switch (databaseType) {
			case MYSQL:
				database = new MySQL(main);
				break;
			case MARIADB:
				database = new MariaDB(main);
				break;
			default:
				database = new SQLite(main);
				break;
		}
	}

	public void openConnection() {
		Main.log.sendMessage(ColorManager.translate("  &eLoading database:"));
		database.open();
	}

	public void changeToSQLite() {
		database = new SQLite(main);
		database.open();
	}

	public Database getDatabase() { return database; }

	private enum DatabaseType {
		SQLITE, MYSQL, MARIADB
	}
}
