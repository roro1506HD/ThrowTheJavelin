package fr.ekalia.minigames.throwthejavelin.scoreboard;

import fr.ekalia.minigames.throwthejavelin.ThrowTheJavelin;
import fr.ekalia.minigames.throwthejavelin.game.player.GamePlayer;
import fr.ekalia.minigames.throwthejavelin.util.GameScoreboard;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author roro1506_HD
 */
public class ScoreboardManager {

    private final ThrowTheJavelin plugin;
    private final DecimalFormat formatter;

    public ScoreboardManager(ThrowTheJavelin plugin) {
        this.plugin = plugin;

        this.formatter = new DecimalFormat("0.00");
        this.formatter.setRoundingMode(RoundingMode.FLOOR);
    }

    /**
     * Initiate a new scoreboard to the provided player
     *
     * @param player The player to init the scoreboard to
     */
    public void initScoreboard(GamePlayer player) {
        GameScoreboard scoreboard = player.getScoreboard();

        scoreboard.setLine(0, "");
        this.updatePlayers(player);
        scoreboard.setLine(2, "");
        scoreboard.setLine(3, "Manche : §cX/X");
        scoreboard.setLine(4, "Lancer : §cX/X");
        scoreboard.setLine(5, "");
        this.updateScore(player);
        scoreboard.setLine(8, "");
        this.updateLeaderBoard(player);
        scoreboard.setLine(14, "");
        scoreboard.create();
    }

    /**
     * Updates the player count to the provided player
     *
     * @param player the player to update the player count to
     */
    public void updatePlayers(GamePlayer player) {
        player.getScoreboard().setLine(1, "Joueurs : §a" + this.plugin.getGameManager().getPlayers().size());
    }

    /**
     * Update the round and throws infos on the scoreboard to the provided player
     *
     * @param player the player to update the scoreboard to
     */
    public void updateRound(GamePlayer player) {
        player.getScoreboard().setLine(3, "Manche : §e" + (this.plugin.getGameManager().getRound() + 1) + "/" + (this.plugin.getGameConfig().getRoundsCount() * 2));
        player.getScoreboard().setLine(4, "Lancer : §e" + (this.plugin.getGameManager().getCurrentPlayerIndex() + 1) + "/" + this.plugin.getGameManager().getMaxPlayerIndex());
    }

    /**
     * Update the score and the total score for the provided player
     *
     * @param player the player to update the scoreboard to
     */
    public void updateScore(GamePlayer player) {
        player.getScoreboard().setLine(6, "Dernier score : §3" + this.formatter.format(player.getLastScore()));
        player.getScoreboard().setLine(7, "Score total : §3" + this.formatter.format(player.getTotalScore()));
    }

    /**
     * Update the leaderboard on the provided player's leaderboard
     *
     * @param player the player to update the scoreboard to
     */
    public void updateLeaderBoard(GamePlayer player) {
        GameScoreboard scoreboard = player.getScoreboard();

        List<GamePlayer> players = this.plugin.getGameManager().getPlayers().stream()
                .sorted((player1, player2) ->
                {
                    int compareResult = -Double.compare(player1.getTotalScore(), player2.getTotalScore());

                    if (compareResult != 0)
                        return compareResult;

                    return player1.getName().compareToIgnoreCase(player2.getName());
                })
                .collect(Collectors.toList());

        scoreboard.setLine(9, "Classement :");

        boolean playerPlaced = player.isSpectator();
        for (int i = 0; i < Math.max(players.size(), 4); i++) {
            GamePlayer leaderBoardPlayer = players.size() <= i ? null : players.get(i);

            if (player.equals(leaderBoardPlayer))
                playerPlaced = true;

            if (i < 4 || playerPlaced)
                scoreboard.setLine(10 + Math.min(3, i), (i == 0 ? "§61er" : i == 1 ? "§e2ème" : (i + 1) + "ème") + " §7- " + (leaderBoardPlayer == null ? "§cPersonne" : leaderBoardPlayer.getName()));

            if (playerPlaced && i >= 3)
                break;
        }
    }
}
