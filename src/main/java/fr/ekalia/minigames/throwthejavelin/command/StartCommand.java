package fr.ekalia.minigames.throwthejavelin.command;

import fr.ekalia.minigames.throwthejavelin.ThrowTheJavelin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.spigotmc.SpigotConfig;

/**
 * @author roro1506_HD
 */
public class StartCommand extends Command {

    private final ThrowTheJavelin plugin;

    public StartCommand(ThrowTheJavelin plugin) {
        super("start");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player && !sender.isOp()) {
            sender.sendMessage(SpigotConfig.unknownCommandMessage);
            return false;
        }

        if (this.plugin.getGameManager().getPlayers().size() <= 1) {
            sender.sendMessage("§cNot enough players!");
            return false;
        }

        sender.sendMessage("§aLancement de la partie...");
        this.plugin.getGameManager().start();
        return true;
    }
}
