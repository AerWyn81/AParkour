package me.davidml16.aparkour.data;

public class Reward {

    private final String id;
    private final String permission;
    private final String command;
    private final boolean firstTime;
    private final int chance;

    public Reward(String id, String permission, String command, boolean firstTime, int chance) {
        this.id = id;
        this.permission = permission;
        this.command = command;
        this.firstTime = firstTime;
        this.chance = chance;
    }

    public String getId() {
        return id;
    }

    public String getPermission() {
        return permission;
    }

    public String getCommand() {
        return command;
    }

    public boolean isFirstTime() {
        return firstTime;
    }

    public int getChance() {
        return chance;
    }

    @Override
    public String toString() {
        return "Reward{" +
                "id='" + id + '\'' +
                ", permission='" + permission + '\'' +
                ", command='" + command + '\'' +
                ", firstTime=" + firstTime +
                ", chance=" + chance +
                '}';
    }
}
