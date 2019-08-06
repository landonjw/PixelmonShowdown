package io.github.landonjw.pixelmonshowdown.battles;

import io.github.landonjw.pixelmonshowdown.PixelmonShowdown;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import io.github.landonjw.pixelmonshowdown.arenas.Arena;
import io.github.landonjw.pixelmonshowdown.arenas.ArenaLocation;
import io.github.landonjw.pixelmonshowdown.arenas.ArenaManager;
import io.github.landonjw.pixelmonshowdown.queues.*;
import io.github.landonjw.pixelmonshowdown.utilities.DataManager;
import io.github.landonjw.pixelmonshowdown.utilities.UIManager;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/*
    Manages the matchmaking and battle starting of matchmaking queues
 */
public class MatchMakingManager {
    private static QueueManager queueManager = PixelmonShowdown.getQueueManager();
    private static Boolean isRunning = false;
    private static Task matchMake;
    private static final int INTERVAL = DataManager.getConfigNode().getNode("Queue-Management", "Match-Maker-Timer").getInt();
    private static final int WARM_UP = DataManager.getConfigNode().getNode("Queue-Management", "Battle-Preparation-Time").getInt();
    private static final int PREVIEW_TIME = DataManager.getConfigNode().getNode("Queue-Management", "Team-Preview-Time").getInt();
    private static final int BIAS_VALUE = DataManager.getConfigNode().getNode("Queue-Management", "Match-Maker-Bias-Value").getInt();
    private static final int MATCH_THRESHOLD = DataManager.getConfigNode().getNode("Queue-Management", "Match-Threshold-Value").getInt();
    private static final Boolean ARENAS_ENABLED = DataManager.getConfigNode().getNode("Arena-Management", "Arenas-Enabled").getBoolean();

    public static void runTask() {
        if (isRunning == false) {
            isRunning = true;
            matchMake = Task.builder().execute(() -> matchMake()).interval(
                    INTERVAL, TimeUnit.SECONDS).submit(PixelmonShowdown.getInstance());
        }
    }

    private static void matchMake() {
        Boolean[] continueMatching = {false};
        queueManager.getAllQueues().forEach((k, v) -> {
            if (v.getQueueSize() >= 2) {
                findMatch(v);
                continueMatching[0] = true;
            }
        });
        if(continueMatching[0] == false){
            stopTask();
        }
    }

    public static void stopTask() {
        if (isRunning == true) {
            isRunning = false;
            matchMake.cancel();
        }
    }

    private static void startPreBattle(UUID player1UUID, UUID player2UUID, CompetitiveFormat format){
        if(Sponge.getServer().getPlayer(player1UUID).isPresent() && Sponge.getServer().getPlayer(player2UUID).isPresent()){
            Player player1 = Sponge.getServer().getPlayer(player1UUID).get();
            Player player2 = Sponge.getServer().getPlayer(player2UUID).get();

            Text textBattleStarting = Text.of(TextColors.WHITE, "[", TextColors.RED, "Pixelmon Showdown", TextColors.WHITE, "]", TextColors.GREEN, " Battle starting in " + WARM_UP
                    + " seconds. Get ready!");
            player1.sendMessage(textBattleStarting);
            player2.sendMessage(textBattleStarting);

            EntityPlayerMP participant1 = (EntityPlayerMP) player1;
            EntityPlayerMP participant2 = (EntityPlayerMP) player2;
            Pokemon[] player1Party = Pixelmon.storageManager.getParty(participant1).getAll();
            Pokemon[] player2Party = Pixelmon.storageManager.getParty(participant2).getAll();
            ArrayList<Pokemon> player1PokemonList = new ArrayList<>();
            ArrayList<Pokemon> player2PokemonList = new ArrayList<>();

            Boolean player1PartyFainted = true;
            for (int i = 0; i < player1Party.length; i++) {
                if (player1Party[i] == null) {
                    continue;
                }
                player1PokemonList.add(player1Party[i]);
            }

            Boolean player2PartyFainted = true;
            for (int i = 0; i < player2Party.length; i++) {
                if (player2Party[i] == null) {
                    continue;
                }
                player2PokemonList.add(player2Party[i]);
            }

            if(format.isTeamPreview()){
                UIManager player1UI = new UIManager(player1);
                UIManager player2UI = new UIManager(player2);

                Task teamPreview = Task.builder().execute(() -> {
                    player1UI.openTeamPreview(player2UUID);
                    player2UI.openTeamPreview(player1UUID);
                }).delay(WARM_UP - PREVIEW_TIME, TimeUnit.SECONDS).submit(PixelmonShowdown.getInstance());

                Task startBattle = Task.builder().execute(() -> {
                    if(player1.isViewingInventory()) {
                        player1.closeInventory();
                    }
                    if(player2.isViewingInventory()) {
                        player2.closeInventory();
                    }

                    Pokemon player1Starter = player1UI.getStartingPokemon();
                    Pokemon player2Starter = player2UI.getStartingPokemon();

                    MatchMakingManager.startBattle(player1UUID, player1PokemonList, player1Starter, player2UUID, player2PokemonList, player2Starter, format);
                }).delay(WARM_UP, TimeUnit.SECONDS).submit(PixelmonShowdown.getInstance());
            }
            else{
                Task startBattle = Task.builder().execute(() -> {
                    if(player1.isViewingInventory()) {
                        player1.closeInventory();
                    }
                    if(player2.isViewingInventory()) {
                        player2.closeInventory();
                    }
                    MatchMakingManager.startBattle(player1UUID, player1PokemonList, null, player2UUID, player2PokemonList, null, format);
                }).delay(WARM_UP, TimeUnit.SECONDS).submit(PixelmonShowdown.getInstance());
            }
        }
    }

    //Start Battle between two players
    private static void startBattle(UUID player1UUID, ArrayList<Pokemon> player1Pokemon, Pokemon player1Starter,
                                       UUID player2UUID, ArrayList<Pokemon> player2Pokemon, Pokemon player2Starter, CompetitiveFormat format){

        CompetitiveQueue queue = queueManager.findQueue(format.getFormatName());
        queue.addPlayerInMatch(player1UUID);
        queue.addPlayerInMatch(player2UUID);

        if(Sponge.getServer().getPlayer(player1UUID).isPresent() && Sponge.getServer().getPlayer(player2UUID).isPresent()){
            Player player1 = Sponge.getServer().getPlayer(player1UUID).get();
            Player player2 = Sponge.getServer().getPlayer(player2UUID).get();

            EntityPlayerMP participant1 = (EntityPlayerMP) player1;
            EntityPlayerMP participant2 = (EntityPlayerMP) player2;

            //Check players are already in a battle
            if(BattleRegistry.getBattle(participant1) != null || BattleRegistry.getBattle(participant2) != null){
                Text text = Text.of(TextColors.WHITE, "[", TextColors.RED, "Pixelmon Showdown", TextColors.WHITE,
                        "]", TextColors.GOLD, " A participant is already in battle! Battle cancelled.");
                player1.sendMessage(text);
                player2.sendMessage(text);

                queue.remPlayerInMatch(player1UUID);
                queue.remPlayerInMatch(player2UUID);
                return;
            }

            Pokemon[] player1Party = Pixelmon.storageManager.getParty(participant1).getAll();
            Pokemon[] player2Party = Pixelmon.storageManager.getParty(participant2).getAll();
            ArrayList<Pokemon> player1PokemonList = new ArrayList<>();
            ArrayList<Pokemon> player2PokemonList = new ArrayList<>();

            //Check if either player's full party is fainted
            Boolean player1PartyFainted = true;
            for (int i = 0; i < player1Party.length; i++) {
                if (player1Party[i] == null) {
                    continue;
                }
                if(player1Party[i].getHealth() != 0){
                    player1PartyFainted = false;
                }
                player1PokemonList.add(player1Party[i]);
            }

            Boolean player2PartyFainted = true;
            for (int i = 0; i < player2Party.length; i++) {
                if (player2Party[i] == null) {
                    continue;
                }
                if(player2Party[i].getHealth() != 0){
                    player2PartyFainted = false;
                }
                player2PokemonList.add(player2Party[i]);
            }

            //Check if either player's party does not follow formats rules
            Boolean player1Validates = true;
            if(format.getBattleRules().validateTeam(player1PokemonList) != null){
                player1Validates = false;
            }

            Boolean player2Validates = true;
            if(format.getBattleRules().validateTeam(player2PokemonList) != null){
                player2Validates = false;
            }

            //Check that either player's party is the same as what they began match with
            Boolean player1PartySame = isPartySame(player1Pokemon, player1PokemonList);
            Boolean player2PartySame = isPartySame(player2Pokemon, player2PokemonList);

            //Check that either player is not already in battle
            Boolean player1InBattle = BattleRegistry.getBattle(participant1) != null;
            Boolean player2InBattle = BattleRegistry.getBattle(participant2) != null;

            //Check parties are same they matchmade with
            if(player1PartySame == false || player2PartySame == false){
                Text textDifferentParty = Text.of(TextColors.WHITE, "[", TextColors.RED, "Pixelmon Showdown", TextColors.WHITE,
                        "]", TextColors.GOLD, " Your party is not the same as what you queue'd with!");
                if(player1PartySame == false){
                    player1.sendMessage(textDifferentParty);
                }
                if(player2PartySame == false){
                    player2.sendMessage(textDifferentParty);
                }
                Text textBattleCancelled = Text.of(TextColors.WHITE, "[", TextColors.RED, "Pixelmon Showdown", TextColors.WHITE,
                        "]", TextColors.GOLD, " A participant's team was found ineligible! Battle cancelled.");
                player1.sendMessage(textBattleCancelled);
                player2.sendMessage(textBattleCancelled);

                queue.remPlayerInMatch(player1UUID);
                queue.remPlayerInMatch(player2UUID);
                return;
            }

            //Check that either player doesn't have fully fainted party
            if(player1PartyFainted == true || player2PartyFainted == true){
                Text textPartyFainted = Text.of(TextColors.WHITE, "[", TextColors.RED, "Pixelmon Showdown", TextColors.WHITE,
                        "]", TextColors.GOLD, " Your party is all fainted!");
                if(player1PartyFainted == true){
                    player1.sendMessage(textPartyFainted);
                }
                if(player2PartyFainted == true){
                    player2.sendMessage(textPartyFainted);
                }
                Text textBattleCancelled = Text.of(TextColors.WHITE, "[", TextColors.RED, "Pixelmon Showdown", TextColors.WHITE,
                        "]", TextColors.GOLD, " A participant's team was all fainted! Battle cancelled.");
                player1.sendMessage(textBattleCancelled);
                player2.sendMessage(textBattleCancelled);

                queue.remPlayerInMatch(player1UUID);
                queue.remPlayerInMatch(player2UUID);
                return;
            }

            //Check that either player's party doesn't break formats rules
            if(player1Validates == false || player2Validates == false){
                Text textDoesNotValidate = Text.of(TextColors.WHITE, "[", TextColors.RED, "Pixelmon Showdown", TextColors.WHITE,
                        "]", TextColors.GOLD, " Your party does not follow the formats rules!");
                if(player1Validates == false){
                    player1.sendMessage(textDoesNotValidate);
                }
                if(player2Validates == false){
                    player2.sendMessage(textDoesNotValidate);
                }
                Text textBattleCancelled = Text.of(TextColors.WHITE, "[", TextColors.RED, "Pixelmon Showdown", TextColors.WHITE,
                        "]", TextColors.GOLD, " A participant's team did not follow the format's rules! Battle cancelled.");
                player1.sendMessage(textBattleCancelled);
                player2.sendMessage(textBattleCancelled);

                queue.remPlayerInMatch(player1UUID);
                queue.remPlayerInMatch(player2UUID);
                return;
            }

            //Check that either player isnt in battle
            if(player1InBattle == true || player2InBattle == true){
                Text textInBattle = Text.of(TextColors.WHITE, "[", TextColors.RED, "Pixelmon Showdown", TextColors.WHITE,
                        "]", TextColors.GOLD, " You are already in battle!");

                if(player1InBattle == true){
                    player1.sendMessage(textInBattle);
                }

                if(player2InBattle == true){
                    player2.sendMessage(textInBattle);
                }

                Text textBattleCancelled = Text.of(TextColors.WHITE, "[", TextColors.RED, "Pixelmon Showdown", TextColors.WHITE,
                        "]", TextColors.GOLD, " A participant is already in battle! Battle cancelled.");
                player1.sendMessage(textBattleCancelled);
                player2.sendMessage(textBattleCancelled);

                queue.remPlayerInMatch(player1UUID);
                queue.remPlayerInMatch(player2UUID);
                return;
            }

            //Get starting pokemon for battle
            EntityPixelmon participant1Starter;
            EntityPixelmon participant2Starter;
            if(player1Starter == null) {
                participant1Starter = Pixelmon.storageManager.getParty(participant1).getAndSendOutFirstAblePokemon(participant1);
            }
            else{
                participant1Starter = player1Starter.getOrSpawnPixelmon(participant1);
            }

            if(player2Starter == null){
                participant2Starter = Pixelmon.storageManager.getParty(participant2).getAndSendOutFirstAblePokemon(participant2);
            }
            else{
                participant2Starter = player2Starter.getOrSpawnPixelmon(participant2);
            }

            PlayerParticipant[] pp1 = {new PlayerParticipant(participant1, participant1Starter)};
            PlayerParticipant[] pp2 = {new PlayerParticipant(participant2, participant2Starter)};

            //Send player to Arena if enabled
            ArenaManager arenaManager = PixelmonShowdown.getArenaManager();
            if(ARENAS_ENABLED) {
                if (arenaManager.isArenasFull() == false) {
                    Arena arena = arenaManager.addPlayers(player1, player2);
                    ArenaLocation locationA = arena.getLocationA();
                    ArenaLocation locationB = arena.getLocationB();

                    try {
                        Location<World> locA = locationA.getLocation();
                        player1.setLocationAndRotation(locA, locationA.getHeadRotation());

                        Location<World> locB = locationB.getLocation();
                        player2.setLocationAndRotation(locB, locationB.getHeadRotation());
                    }
                    catch(Exception e) {
                        PixelmonShowdown.getLogger().error("Error Teleporting to Arena");
                        e.printStackTrace();
                    }

                    BattleRules rules = format.getBattleRules();
                    BattleRegistry.startBattle(pp1, pp2, rules);
                }
                else{
                    player1.sendMessage(Text.of(TextColors.RED, "Arenas all full, battle commencing at distance."));
                    player2.sendMessage(Text.of(TextColors.RED, "Arenas all full, battle commencing at distance."));
                    BattleRules rules = format.getBattleRules();
                    BattleRegistry.startBattle(pp1, pp2, rules);
                }
            }
            else {
                BattleRules rules = format.getBattleRules();
                BattleRegistry.startBattle(pp1, pp2, rules);
            }
        }
        else{
            Text playerNotFound = Text.of(TextColors.WHITE, "[", TextColors.RED, "Pixelmon Showdown", TextColors.WHITE,
                    "]", TextColors.GOLD, " A player disconnected! Battle cancelled.");
            if(Sponge.getServer().getPlayer(player1UUID).isPresent()){
                Sponge.getServer().getPlayer(player1UUID).get().sendMessage(playerNotFound);
            }
            if(Sponge.getServer().getPlayer(player2UUID).isPresent()){
                Sponge.getServer().getPlayer(player2UUID).get().sendMessage(playerNotFound);
            }
            queue.remPlayerInMatch(player1UUID);
            queue.remPlayerInMatch(player2UUID);
        }
    }

    public static void findMatch(CompetitiveQueue queue){
        //Create arraylist of players to remove from the queue to avoid iteration issues
        ArrayList<UUID> toRemove = new ArrayList<>();
        //Loop through all players in queue
        ArrayList<UUID> playersInQueue = queue.getPlayersInQueue();

        EloLadder ladder = queue.getLadder();

        for(UUID key: playersInQueue){
            //Make sure loop doesnt match players already set for removal
            if(toRemove.contains(key)){
                continue;
            }
            //Create variables for evaluating match quality
            EloProfile playerProfile = ladder.getProfile(key);
            //TimeVar that increases the longer player is in queue
            int timeVar = playerProfile.getTimeVar();
            int lowestMatchValue = -1;
            UUID bestOpponent = null;

            //Loop through players in queue again
            for(UUID secondKey: playersInQueue){
                //Avoid matching players already set for removal
                if(toRemove.contains(secondKey)){
                    continue;
                }

                //Keep player from matching with themselves (lol)
                if(key.equals(secondKey) == false){
                    //Calculate quality of potential match
                    EloProfile oppProfile = ladder.getProfile(secondKey);
                    int matchValue = Math.abs(playerProfile.getElo() - oppProfile.getElo()) - timeVar;
                    //Check match quality is within threshold
                    if(matchValue <= MATCH_THRESHOLD){
                        //Check if match is best value
                        if(matchValue <= lowestMatchValue || lowestMatchValue == -1){
                            lowestMatchValue = matchValue;
                            bestOpponent = secondKey;
                        }
                    }
                }
            }
            //Check if there's an appropriate match
            if(bestOpponent != null){
                toRemove.add(bestOpponent);
                toRemove.add(key);
                EloProfile profile1 = ladder.getProfile(key);
                String player1Name = Sponge.getServer().getPlayer(key).get().getName();
                if(!profile1.getPlayerName().equals(player1Name)){
                    profile1.setPlayerName(player1Name);
                }
                player1Name = profile1.getPlayerName();

                EloProfile profile2 = ladder.getProfile(bestOpponent);
                String player2Name = Sponge.getServer().getPlayer(bestOpponent).get().getName();
                if(!profile2.getPlayerName().equals(player2Name)){
                    profile2.setPlayerName(player2Name);
                }
                player2Name = profile2.getPlayerName();

                MatchMakingManager.startPreBattle(key, bestOpponent, queue.getFormat());
            }
            else{
                //If no good match, increase TimeVar value
                playerProfile.setTimeVar(timeVar + BIAS_VALUE);
            }
        }
        for(UUID uuid: toRemove){
            queue.addPlayerInPreMatch(uuid);
        }
    }

    private static Boolean isPartySame(ArrayList<Pokemon> party1, ArrayList<Pokemon> party2){
        if(party1.size() != party2.size()){
            return false;
        }
        else{
            for(int i = 0; i < party1.size(); i++){
                if(party2.contains(party1.get(i)) == false){
                    return false;
                }
            }
        }
        return true;
    }
}