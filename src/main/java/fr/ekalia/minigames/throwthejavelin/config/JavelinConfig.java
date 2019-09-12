package fr.ekalia.minigames.throwthejavelin.config;

import org.bukkit.Location;

/**
 * @author roro1506_HD
 */
public class JavelinConfig {

    private int roundsCount = -1;

    private Location lobbyLocation = new Location(null, 0, 0, 0);
    private Location launchLocation = new Location(null, 0, 0, 0);
    private Location specLocation = new Location(null, 0, 0, 0);
    private Location finishLocation = new Location(null, 0, 0, 0);

    private Location minTargetLocation = new Location(null, 0, 0, 0);
    private Location maxTargetLocation = new Location(null, 0, 0, 0);

    private Location firstLeaderboardLocation = new Location(null, 0, 0, 0);
    private Location secondLeaderboardLocation = new Location(null, 0, 0, 0);
    private Location thirdLeaderboardLocation = new Location(null, 0, 0, 0);

    private Location playerStatsLocation = new Location(null, 0, 0, 0);
    private Location overallStatsLocation = new Location(null, 0, 0, 0);
    private Location targetStatsLocation = new Location(null, 0, 0, 0);

    /**
     * @return the amount of rounds each game type will run.
     */
    public int getRoundsCount() {
        return this.roundsCount;
    }

    /**
     * @return the lobby location, this is where players are teleported when they join the server
     */
    public Location getLobbyLocation() {
        return this.lobbyLocation;
    }

    /**
     * @return the launch location, this is where players are teleported and sit when they are about to throw the javelin
     */
    public Location getLaunchLocation() {
        return this.launchLocation;
    }

    /**
     * @return the spec location, this is where the spectator corpse is spawned at
     */
    public Location getSpecLocation() {
        return this.specLocation;
    }

    /**
     * @return the finish location, this is where all players are teleported when all the games ended
     */
    public Location getFinishLocation() {
        return this.finishLocation;
    }

    /**
     * This is the lowest target location
     * Target location spawns are as the following : random location in (max target location - min target location)
     *
     * @return the min target location
     */
    public Location getMinTargetLocation() {
        return this.minTargetLocation;
    }

    /**
     * This is the highest target location
     * Target location spawns are as the following : random location in (max target location - min target location)
     *
     * @return the max target location
     */
    public Location getMaxTargetLocation() {
        return this.maxTargetLocation;
    }

    /**
     * @return the first (winner) leaderboard location
     */
    public Location getFirstLeaderboardLocation() {
        return this.firstLeaderboardLocation;
    }

    /**
     * @return the second leader board location
     * @see JavelinConfig#getFirstLeaderboardLocation()
     */
    public Location getSecondLeaderboardLocation() {
        return this.secondLeaderboardLocation;
    }

    /**
     * @return the third leader board location
     * @see JavelinConfig#getFirstLeaderboardLocation()
     */
    public Location getThirdLeaderboardLocation() {
        return this.thirdLeaderboardLocation;
    }

    /**
     * @return the player-relative stats frames top left corner
     */
    public Location getPlayerStatsLocation() {
        return this.playerStatsLocation;
    }

    /**
     * @return the global stats frames top left corner
     */
    public Location getOverallStatsLocation() {
        return this.overallStatsLocation;
    }

    /**
     * @return the target stats frames top left corner
     */
    public Location getTargetStatsLocation() {
        return this.targetStatsLocation;
    }

    /**
     * @return true if the config is all good, false if something is missing or a world is null
     */
    public boolean checkConfig() {
        return this.roundsCount != -1

                && this.lobbyLocation != null
                && this.launchLocation != null
                && this.specLocation != null
                && this.finishLocation != null

                && this.minTargetLocation != null
                && this.maxTargetLocation != null

                && this.firstLeaderboardLocation != null
                && this.secondLeaderboardLocation != null
                && this.thirdLeaderboardLocation != null

                && this.playerStatsLocation != null
                && this.overallStatsLocation != null
                && this.targetStatsLocation != null

                && this.lobbyLocation.getWorld() != null
                && this.launchLocation.getWorld() != null
                && this.specLocation.getWorld() != null
                && this.finishLocation.getWorld() != null

                && this.minTargetLocation.getWorld() != null
                && this.maxTargetLocation.getWorld() != null

                && this.firstLeaderboardLocation.getWorld() != null
                && this.secondLeaderboardLocation.getWorld() != null
                && this.thirdLeaderboardLocation.getWorld() != null

                && this.playerStatsLocation.getWorld() != null
                && this.overallStatsLocation.getWorld() != null
                && this.targetStatsLocation.getWorld() != null;
    }
}
