package io.github.landonjw.pixelmonshowdown.commands;

import io.github.landonjw.pixelmonshowdown.utilities.UIManager;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ArenasCommand implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(Text.of(TextColors.RED, "You must be in-game to use this command!"));
        }
        
        Player player = (Player) src;
    
        if(player.hasPermission("pixelmonshowdown.admin.command.arenas")) {
            UIManager manager = new UIManager(player);
            manager.openArenasGUI();
        }
        return CommandResult.success();
    }
}