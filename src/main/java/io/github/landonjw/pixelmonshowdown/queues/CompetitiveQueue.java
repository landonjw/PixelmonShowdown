package io.github.landonjw.pixelmonshowdown.queues;

import org.spongepowered.api.Sponge;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Creates a queue with a specified format & ladder that allows players to queue for matchmaking
 */
public class CompetitiveQueue {
    private CompetitiveFormat format;
    private EloLadder ladder;
    private ArrayList<UUID> playersInQueue = new ArrayList<>();
    private ArrayList<UUID> playersInPreMatch = new ArrayList<>();
    private ArrayList<UUID> playersInMatch = new ArrayList<>();

    public CompetitiveQueue(CompetitiveFormat format, EloLadder ladder){
        this.format = format;
        this.ladder = ladder;
    }

    //Returns the competitive format
    public CompetitiveFormat getFormat(){
        return format;
    }

    //Returns the elo ladder
    public EloLadder getLadder(){
        return ladder;
    }

    //Checks if player is in queue
    public boolean hasPlayerInQueue(UUID player){
        return playersInQueue.contains(player);
    }

    //Checks if player is in prematch
    public boolean hasPlayerInPreMatch(UUID player){
        return playersInPreMatch.contains(player);
    }

    //Checks if player in in a match
    public boolean hasPlayerInMatch(UUID player){
        return playersInMatch.contains(player);
    }

    //Checks if player in in a match
    public boolean hasPlayerInAny(UUID player){
        if(playersInQueue.contains(player) || playersInPreMatch.contains(player)
                || playersInMatch.contains(player)) {
            return true;
        }
        return false;
    }

    //Adds a player to the queue
    public void addPlayerInQueue(UUID player){
        String playerName = Sponge.getServer().getPlayer(player).get().getName();
        if(hasPlayerInQueue(player) == false && hasPlayerInPreMatch(player) == false
                && hasPlayerInMatch(player) == false){
            //Check if player is in ladder already
            if(ladder.hasPlayer(player) == false){
                //Add player if they aren't
                ladder.addPlayer(player, playerName);
            }
            playersInQueue.add(player);
        }
    }

    //Adds a player to pre-match
    public void addPlayerInPreMatch(UUID player){
        if(hasPlayerInQueue(player) == true && hasPlayerInPreMatch(player) == false
                && hasPlayerInMatch(player) == false){
            ladder.getProfile(player).setTimeVar(0);
            playersInPreMatch.add(player);
            playersInQueue.remove(player);
        }
    }

    //Adds a player to a match
    public void addPlayerInMatch(UUID player){
        if(hasPlayerInQueue(player) == false && hasPlayerInPreMatch(player) == true
                && hasPlayerInMatch(player) == false){
            playersInMatch.add(player);
            playersInPreMatch.remove(player);
        }
    }

    //Removes a player from queue
    public void remPlayerInQueue(UUID player){
        if(hasPlayerInQueue(player)){
            playersInQueue.remove(player);
        }
    }

    public void remPlayerInPreMatch(UUID player){
        if(hasPlayerInPreMatch(player)){
            playersInPreMatch.remove(player);
        }
    }

    //Removes a player from match
    public void remPlayerInMatch(UUID player){
        if(hasPlayerInMatch(player)){
            playersInMatch.remove(player);
        }
    }

    //Removes a player from queue, prematch, or match
    public void remPlayerInAny(UUID player){
        if(hasPlayerInQueue(player)){
            ladder.getProfile(player).setTimeVar(0);
            playersInQueue.remove(player);
        }
        if(hasPlayerInPreMatch(player)){
            playersInPreMatch.remove(player);
        }
        if(hasPlayerInMatch(player)){
            playersInMatch.remove(player);
        }
    }

    public int getQueueSize(){
        return playersInQueue.size();
    }

    public ArrayList<UUID> getPlayersInQueue(){
        return playersInQueue;
    }
}