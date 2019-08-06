package io.github.landonjw.pixelmonshowdown.battles;

import com.flowpowered.math.vector.Vector3d;
import io.github.landonjw.pixelmonshowdown.PixelmonShowdown;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleControllerBase;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.enums.battle.BattleResults;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleEndCause;
import io.github.landonjw.pixelmonshowdown.arenas.Arena;
import io.github.landonjw.pixelmonshowdown.arenas.ArenaLocation;
import io.github.landonjw.pixelmonshowdown.arenas.ArenaManager;
import io.github.landonjw.pixelmonshowdown.queues.CompetitiveQueue;
import io.github.landonjw.pixelmonshowdown.queues.EloLadder;
import io.github.landonjw.pixelmonshowdown.queues.EloProfile;
import io.github.landonjw.pixelmonshowdown.queues.QueueManager;
import io.github.landonjw.pixelmonshowdown.utilities.DataManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Manages the various ways that a battle can end, and how to assign elo to players
 */
public class BattleManager {
    private static final boolean ARENAS_ENABLED = DataManager.getConfigNode().getNode("Arena-Management", "Arenas-Enabled").getBoolean();

    @SubscribeEvent
    public void onBattleEnd(BattleEndEvent event){
        //Get both participants in battle
        BattleParticipant bParticipant1 = event.results.keySet().asList().get(0);
        BattleParticipant bParticipant2 = event.results.keySet().asList().get(1);

        //Check if both participants are players
        if(bParticipant1 instanceof PlayerParticipant && bParticipant2 instanceof PlayerParticipant){
            PlayerParticipant participant1 = (PlayerParticipant) bParticipant1;
            PlayerParticipant participant2 = (PlayerParticipant) bParticipant2;
            UUID player1UUID = participant1.getEntity().getUniqueID();
            UUID player2UUID = participant2.getEntity().getUniqueID();
            Player player1 = Sponge.getServer().getPlayer(player1UUID).get();
            Player player2 = Sponge.getServer().getPlayer(player2UUID).get();
            EntityPlayerMP ePlayer1 = (EntityPlayerMP) player1;
            EntityPlayerMP ePlayer2 = (EntityPlayerMP) player2;

            //Check if players are in battleregistry & deregister it to stop players being stuck in battle
            if(BattleRegistry.getBattle(ePlayer1) != null){
                BattleControllerBase bcb = BattleRegistry.getBattle(ePlayer1);
                BattleRegistry.deRegisterBattle(bcb);
            }

            //Check if both players are online
            if(Sponge.getServer().getPlayer(player1UUID).isPresent() && Sponge.getServer().getPlayer(player2UUID).isPresent()){

                QueueManager queueManager = PixelmonShowdown.getQueueManager();

                //Check if both players are in a match
                if(queueManager.isPlayerInMatch(player1UUID) && queueManager.isPlayerInMatch(player2UUID)){
                    if(queueManager.findPlayerInMatch(player1UUID) != null){
                        CompetitiveQueue queue = queueManager.findPlayerInMatch(player1UUID);
                        EloLadder ladder = queue.getLadder();

                        //Check if both players are in match in the same format
                        if(queue.hasPlayerInMatch(player2UUID)) {
                            //Check if battle end was normal or not
                            if (event.abnormal != true && event.cause != EnumBattleEndCause.FORCE) {
                                //Add players as active for auto saving
                                ladder.addAsActive(player1UUID);
                                ladder.addAsActive(player2UUID);

                                EloProfile eloWinner;
                                EloProfile eloLoser;
                                Player winner;
                                Player loser;

                                //Determine winner and loser
                                if (event.results.get(bParticipant1) == BattleResults.VICTORY) {
                                    winner = player1;
                                    eloWinner = ladder.getProfile(player1UUID);
                                    loser = player2;
                                    eloLoser = ladder.getProfile(player2UUID);
                                } else {
                                    winner = player2;
                                    eloWinner = ladder.getProfile(player2UUID);
                                    loser = player1;
                                    eloLoser = ladder.getProfile(player1UUID);
                                }

                                int winnerElo = eloWinner.getElo();
                                int loserElo = eloLoser.getElo();

                                //Adjust elos for each player
                                eloWinner.addWin(loserElo);
                                eloLoser.addLoss(winnerElo);

                                int newWinnerElo = eloWinner.getElo();
                                int newLoserElo = eloLoser.getElo();

                                //Send player message for with their adjusted elo
                                Text textWin = Text.of(TextColors.WHITE, "[", TextColors.RED, "Pixelmon Showdown", TextColors.WHITE, "]", TextColors.GOLD, " Victory!",
                                        TextColors.GREEN, " [Elo: " + winnerElo + " > " + newWinnerElo + "]");

                                Text textLoss = Text.of(TextColors.WHITE, "[", TextColors.RED, "Pixelmon Showdown", TextColors.WHITE, "]", TextColors.GOLD, " Defeat!",
                                        TextColors.GREEN, " [Elo: " + loserElo + " > " + newLoserElo + "]");

                                winner.sendMessage(textWin);
                                loser.sendMessage(textLoss);

                                //Remove players from match
                                queue.remPlayerInMatch(player1UUID);
                                queue.remPlayerInMatch(player2UUID);
                                //Update ladders for leaderboard
                                ladder.updatePlayer(player1UUID);
                                ladder.updatePlayer(player2UUID);

                                if (ARENAS_ENABLED) {
                                    remFromArena(player1, player1UUID, player2, player2UUID);
                                }
                            }
                            else{
                                //Remove player from arena before task to reduce clunkiness
                                if(ARENAS_ENABLED) {
                                    remFromArena(player1, player1UUID, player2, player2UUID);
                                }
                                //Create task due to some odd battle crashes seemingly not triggering battle end or disconnect event
                                Task task = Task.builder().execute(() -> {
                                    //Check if both players are present
                                    if(Sponge.getServer().getPlayer(player1UUID).isPresent() == false || Sponge.getServer().getPlayer(player2UUID).isPresent() == false){
                                        //If both players aren't present, it will give win to whoever is still connected
                                        //This is to deter players trying to intentionally caused forced battle ends to prevent loss

                                        //Add players as active for auto saving
                                        ladder.addAsActive(player1UUID);
                                        ladder.addAsActive(player2UUID);

                                        EloProfile player1Profile = ladder.getProfile(player1UUID);
                                        EloProfile player2Profile = ladder.getProfile(player2UUID);

                                        //Give win to player 2 if player 1 disconnected, vice versa if player 2 diconnected
                                        if(Sponge.getServer().getPlayer(player1UUID).isPresent() == false){
                                            int player2Elo = player2Profile.getElo();
                                            player2Profile.addWin(player1Profile.getElo());
                                            player1Profile.addLoss(player2Elo);

                                            Text textWin = Text.builder("[").color(TextColors.WHITE).append(
                                                    Text.builder("Pixelmon Showdown").color(TextColors.RED).append(
                                                            Text.builder("] ").color(TextColors.WHITE).append(
                                                                    Text.builder("Victory!").color(TextColors.GOLD).append(
                                                                            Text.builder(" [Elo: " + player2Elo + " > " + player2Profile.getElo() + "]").color(TextColors.GREEN)
                                                                                    .build()).build()).build()).build()).build();
                                            player2.sendMessage(textWin);
                                            ladder.updatePlayer(player1UUID);
                                            ladder.updatePlayer(player2UUID);

                                        }
                                        else if(Sponge.getServer().getPlayer(player2UUID).isPresent() == false){
                                            int player1Elo = player1Profile.getElo();
                                            player1Profile.addWin(player2Profile.getElo());
                                            player2Profile.addLoss(player1Elo);

                                            Text textWin = Text.builder("[").color(TextColors.WHITE).append(
                                                    Text.builder("Pixelmon Showdown").color(TextColors.RED).append(
                                                            Text.builder("] ").color(TextColors.WHITE).append(
                                                                    Text.builder("Victory!").color(TextColors.GOLD).append(
                                                                            Text.builder(" [Elo: " + player1Elo + " > " + player1Profile.getElo() + "]").color(TextColors.GREEN)
                                                                                    .build()).build()).build()).build()).build();
                                            player1.sendMessage(textWin);
                                            ladder.updatePlayer(player1UUID);
                                            ladder.updatePlayer(player2UUID);
                                        }

                                        //Remove both players from match
                                        queue.remPlayerInMatch(player1UUID);
                                        queue.remPlayerInMatch(player2UUID);

                                        //End battle to prevent players being stuck in battle and having to /endbattle
                                        if (BattleRegistry.getBattle(ePlayer1) != null) {
                                            BattleRegistry.getBattle(ePlayer1).endBattle();
                                        }
                                        if (BattleRegistry.getBattle(ePlayer2) != null) {
                                            BattleRegistry.getBattle(ePlayer2).endBattle();
                                        }
                                    }
                                    else {
                                        //If both players are still connected, simply do not assign elo points.
                                        Text textUnexpected = Text.of(TextColors.WHITE, "[", TextColors.RED, "Pixelmon Showdown", TextColors.WHITE, "]", TextColors.GOLD, " Unexpected battle end! No points awarded.");

                                        player1.sendMessage(textUnexpected);
                                        player2.sendMessage(textUnexpected);

                                        queue.remPlayerInMatch(player1UUID);
                                        queue.remPlayerInMatch(player2UUID);

                                        if (BattleRegistry.getBattle(ePlayer1) != null) {
                                            BattleRegistry.getBattle(ePlayer1).endBattle();
                                        }
                                        if (BattleRegistry.getBattle(ePlayer2) != null) {
                                            BattleRegistry.getBattle(ePlayer2).endBattle();
                                        }
                                    }
                                }).delay(500, TimeUnit.MILLISECONDS).submit(PixelmonShowdown.getInstance());
                            }
                        }
                    }
                }
            }
        }
    }

    //If player quits, remove them from any queue they were in
    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event) {
        QueueManager queueManager = PixelmonShowdown.getQueueManager();

        Player player = event.getTargetEntity();
        UUID playerUUID = player.getUniqueId();

        if(queueManager.isPlayerInAny(playerUUID)){
            queueManager.findPlayerInAny(playerUUID).remPlayerInAny(playerUUID);
        }
    }

    //If player uses the command /endbattle, give them the loss in battle
    @Listener
    public void onCommandSend(SendCommandEvent event){
        if(event.getCommand().equals("endbattle")){
            //Check that source is a player
            if(event.getSource() instanceof Player){
                QueueManager queueManager = PixelmonShowdown.getQueueManager();
                Player player = (Player) event.getSource();
                UUID playerUUID = player.getUniqueId();
                Player opponent;
                UUID opponentUUID;

                //Check if player is in match
                if(queueManager.isPlayerInMatch(playerUUID)){
                    CompetitiveQueue queue = queueManager.findPlayerInMatch(playerUUID);

                    EntityPlayer ePlayer = (EntityPlayer) player;
                    BattleControllerBase bcb = BattleRegistry.getBattle(ePlayer);

                    ArrayList<PlayerParticipant> playerParticipants = (ArrayList) bcb.getPlayers();

                    UUID participant1 = playerParticipants.get(0).getEntity().getUniqueID();
                    UUID participant2 = playerParticipants.get(1).getEntity().getUniqueID();

                    if(player.getUniqueId().equals(participant1)){
                        opponent = Sponge.getServer().getPlayer(participant2).get();
                        opponentUUID = opponent.getUniqueId();
                    }
                    else{
                        opponent = Sponge.getServer().getPlayer(participant1).get();
                        opponentUUID = opponent.getUniqueId();
                    }

                    EloLadder ladder = queue.getLadder();
                    ladder.addAsActive(playerUUID);
                    ladder.addAsActive(opponentUUID);

                    EloProfile playerProfile = ladder.getProfile(playerUUID);
                    int playerElo = playerProfile.getElo();

                    EloProfile oppProfile = ladder.getProfile(opponentUUID);
                    int oppElo = oppProfile.getElo();

                    //Add loss to player who /endbattles and win to other participant
                    playerProfile.addLoss(oppElo);
                    oppProfile.addWin(playerElo);

                    Text textWin = Text.builder("[").color(TextColors.WHITE).append(
                            Text.builder("Pixelmon Showdown").color(TextColors.RED).append(
                                    Text.builder("] ").color(TextColors.WHITE).append(
                                            Text.builder("Victory!").color(TextColors.GOLD).append(
                                                    Text.builder(" [Elo: " + oppElo + " > " + oppProfile.getElo() + "]").color(TextColors.GREEN)
                                                            .build()).build()).build()).build()).build();
                    opponent.sendMessage(textWin);

                    Text textLoss = Text.builder("[").color(TextColors.WHITE).append(
                            Text.builder("Pixelmon Showdown").color(TextColors.RED).append(
                                    Text.builder("] ").color(TextColors.WHITE).append(
                                            Text.builder("Defeat!").color(TextColors.GOLD).append(
                                                    Text.builder(" [Elo: " + playerElo + " > " + playerProfile.getElo() + "]").color(TextColors.GREEN)
                                                            .build()).build()).build()).build()).build();
                    player.sendMessage(textLoss);

                    //Remove players from match and update elos
                    queue.remPlayerInMatch(playerUUID);
                    queue.remPlayerInMatch(opponentUUID);
                    ladder.updatePlayer(playerUUID);
                    ladder.updatePlayer(opponentUUID);

                    if(ARENAS_ENABLED){
                        remFromArena(player, playerUUID, opponent, opponentUUID);
                    }
                }
            }
        }
    }

    //Removes players from an arena
    private void remFromArena(Player player1, UUID player1UUID, Player player2, UUID player2UUID){
        if(ARENAS_ENABLED){
            ArenaManager arenaManager = PixelmonShowdown.getArenaManager();
            Arena arena = arenaManager.getArena(player1UUID, player2UUID);

            //Check if arena doesn't exist (arena wasnt found)
            if(arena != null){
                ArenaLocation locA = arena.getLocationA();
                ArenaLocation locB = arena.getLocationB();

                //Return players to their original location
                if(locA.hasUUID(player1UUID)){
                    Location<World> player1ReturnLocation = locA.getReturnLocation();
                    Vector3d player1ReturnRotation = locA.getReturnHeadRotation();
                    if(Sponge.getServer().getWorld(locA.getWorld()).isPresent()) {
                        player1.setLocationAndRotation(player1ReturnLocation, player1ReturnRotation);
                    }
                }
                else if(locA.hasUUID(player2UUID)){
                    Location<World> player2ReturnLocation = locA.getReturnLocation();
                    Vector3d player2ReturnRotation = locA.getReturnHeadRotation();
                    if(Sponge.getServer().getWorld(locA.getWorld()).isPresent()) {
                        player2.setLocationAndRotation(player2ReturnLocation, player2ReturnRotation);
                    }
                }

                if(locB.hasUUID(player1UUID)){
                    Location<World> player1ReturnLocation = locB.getReturnLocation();
                    Vector3d player1ReturnRotation = locB.getReturnHeadRotation();
                    if(Sponge.getServer().getWorld(locB.getWorld()).isPresent()) {
                        player1.setLocationAndRotation(player1ReturnLocation, player1ReturnRotation);
                    }
                }
                else if(locB.hasUUID(player2UUID)){
                    Location<World> player2ReturnLocation = locB.getReturnLocation();
                    Vector3d player2ReturnRotation = locB.getReturnHeadRotation();
                    if(Sponge.getServer().getWorld(locB.getWorld()).isPresent()) {
                        player2.setLocationAndRotation(player2ReturnLocation, player2ReturnRotation);
                    }
                }

                //Clear arena
                arenaManager.remPlayers(player1UUID, player2UUID);
            }
        }
    }
}