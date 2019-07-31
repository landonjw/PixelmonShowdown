package io.github.landonjw.pixelmonshowdown.commands;

import io.github.landonjw.pixelmonshowdown.PixelmonShowdown;
import io.github.landonjw.pixelmonshowdown.queues.CompetitiveQueue;
import io.github.landonjw.pixelmonshowdown.queues.EloLadder;
import io.github.landonjw.pixelmonshowdown.queues.EloProfile;
import io.github.landonjw.pixelmonshowdown.queues.QueueManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

public class displayCommand implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) {
        if (src instanceof Player) {
            Player player = (Player) src;

            if(player.hasPermission("pixelmonshowdown.user.command.display")) {
                if(args.<String>getOne("type").isPresent()) {
                    String type = args.<String>getOne("type").get();
                    if(type.equals("profile")){
                        if(args.<String>getOne("format").isPresent()){
                            String format = args.<String>getOne("format").get();

                            QueueManager queueManager = PixelmonShowdown.getInstance().getQueueManager();
                            if(queueManager.findQueue(format) != null){
                                CompetitiveQueue queue = queueManager.findQueue(format);
                                EloLadder ladder = queue.getLadder();

                                if(ladder.getProfile(player.getUniqueId()) != null){
                                    EloProfile profile = ladder.getProfile(player.getUniqueId());

                                    Text hover = Text.of(TextColors.WHITE, "Player: " + profile.getPlayerName() +"\n",
                                            TextColors.GOLD, "Elo: " + profile.getElo() + "\n",
                                            TextColors.GREEN, "Wins: " + profile.getWins() + "\n",
                                            TextColors.RED, "Losses: " + profile.getLosses() + "\n",
                                            TextColors.BLUE, "Winrate: " + profile.getWinRate());

                                    Text body = Text.of(TextColors.WHITE, "[", TextColors.RED, "Pixelmon Showdown", TextColors.WHITE, "] ",
                                            TextColors.GOLD, profile.getPlayerName() + "'s " + queue.getFormat().getFormatName() + " Profile (Hover)");

                                    Text display = Text.builder().append(body).onHover(TextActions.showText(hover)).build();

                                    Sponge.getServer().getBroadcastChannel().send(display);
                                    return CommandResult.success();
                                }
                                else{
                                    player.sendMessage(Text.of(TextColors.RED, "No Player Stats Recorded."));
                                }
                            }
                            else{
                                player.sendMessage(Text.of(TextColors.RED, "Unknown format. Usage: /display <type> <format..>"));
                                return CommandResult.success();
                            }
                        }
                        else{
                            player.sendMessage(Text.of(TextColors.RED, "No format given. Usage: /display <type> <format..>"));
                            return CommandResult.success();
                        }
                    }
                    else if(type.equals("leaderboard")){
                        if(args.<String>getOne("format").isPresent()){
                            String format = args.<String>getOne("format").get();

                            QueueManager queueManager = PixelmonShowdown.getInstance().getQueueManager();
                            if(queueManager.findQueue(format) != null){
                                CompetitiveQueue queue = queueManager.findQueue(format);
                                EloLadder ladder = queue.getLadder();

                                if(ladder.getLadderSize() > 0){

                                    Text hover = Text.of(TextColors.WHITE, "Format: " + queue.getFormat().getFormatName() +"\n");

                                    if(ladder.getLadderSize() >= 5) {
                                        for (int i = 0; i < 5; i++) {
                                            EloProfile profile = ladder.getProfile(i);
                                            hover = hover.concat(Text.of(TextColors.GOLD, (i + 1) + ". " + profile.getPlayerName() + " - ",
                                                    TextColors.GREEN, "(" + profile.getElo() + ")\n"));
                                        }
                                    }
                                    else{
                                        for (int i = 0; i < ladder.getLadderSize(); i++){
                                            EloProfile profile = ladder.getProfile(i);
                                            hover = hover.concat(Text.of(TextColors.GOLD, (i + 1) + ". " + profile.getPlayerName() + " - ",
                                                    TextColors.GREEN, "(" + profile.getElo() + ")\n"));
                                        }
                                    }

                                    Text body = Text.of(TextColors.WHITE, "[", TextColors.RED, "Pixelmon Showdown", TextColors.WHITE, "] ",
                                            TextColors.GOLD, queue.getFormat().getFormatName() + " Leaderboard (Hover)");

                                    Text display = Text.builder().append(body).onHover(TextActions.showText(hover)).build();

                                    Sponge.getServer().getBroadcastChannel().send(display);
                                    return CommandResult.success();
                                }
                                else{
                                    player.sendMessage(Text.of(TextColors.RED, "No Ladder Stats Recorded."));
                                }
                            }
                            else{
                                player.sendMessage(Text.of(TextColors.RED, "Unknown format. Usage: /display <type> <format..>"));
                                return CommandResult.success();
                            }
                        }
                        else{
                            player.sendMessage(Text.of(TextColors.RED, "No format given. Usage: /display <type> <format..>"));
                            return CommandResult.success();
                        }
                    }
                    else{
                        player.sendMessage(Text.of(TextColors.RED, "Unknown display type. Accepted types: leaderboard, profile."));
                        return CommandResult.success();
                    }
                }
            }
        }
        return CommandResult.success();
    }
}