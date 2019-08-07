package io.github.landonjw.pixelmonshowdown.queues;

import io.github.landonjw.pixelmonshowdown.PixelmonShowdown;
import io.github.landonjw.pixelmonshowdown.utilities.DataManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *  Manages all of the competitive queues on the server
 */
public class QueueManager {
    private Map<String, CompetitiveQueue> queuesMap = new HashMap<>();

    //Return all of the queues in the manager
    public Map<String, CompetitiveQueue> getAllQueues(){
        return queuesMap;
    }

    //Load all competitive queues from the Formats.conf config
    public void loadFromConfig(){
        DataManager.getFormatsNode().getNode("Formats").getChildrenMap().forEach((k, v) -> {
            PixelmonShowdown.getLogger().info("Loading Queue: " + k.toString());

            //Load the format
            CompetitiveFormat format = new CompetitiveFormat(k.toString());
            format.loadFormat();

            //Load the ladder
            EloLadder ladder = new EloLadder(k.toString());
            ladder.loadLadder();

            //Create & place queue in hashmap
            CompetitiveQueue queue = new CompetitiveQueue(format, ladder);
            queuesMap.put(format.getFormatName(), queue);
        });
    }

    //Save all the elo profiles to file
    public void saveAllQueueProfiles(){
        queuesMap.forEach((format,queue) ->{
           queue.getLadder().saveAllProfiles();
        });
    }

    //Find queue from hashmap
    public CompetitiveQueue findQueue(String format){
        return queuesMap.get(format);
    }

    //Add queue to manager hashmap
    public void addQueue(String format, CompetitiveQueue queue){
        queuesMap.put(format, queue);
    }

    //Delete queue from manager hashmap
    public void delQueue(String format){
        if(queuesMap.containsKey(format)){
            queuesMap.remove(format);
        }
    }

    //Check if player is in queue in any format
    public boolean isPlayerInQueue(UUID player){
        final boolean[] isInQueue = {false};
        queuesMap.forEach((format,queue) -> {
            if(queue.hasPlayerInQueue(player)){
                isInQueue[0] = true;
            }
        });
        return isInQueue[0];
    }

    //Check if player is in prematch in any format
    public boolean isPlayerInPreMatch(UUID player){
        final boolean[] isInMatch = {false};
        queuesMap.forEach((format,queue) -> {
            if(queue.hasPlayerInPreMatch(player)){
                isInMatch[0] = true;
            }
        });
        return isInMatch[0];
    }

    //Check if player is in match in any format
    public boolean isPlayerInMatch(UUID player){
        final boolean[] isInMatch = {false};
        queuesMap.forEach((format,queue) -> {
            if(queue.hasPlayerInMatch(player)){
                isInMatch[0] = true;
            }
        });
        return isInMatch[0];
    }

    //Check if player is in anything from any format
    public boolean isPlayerInAny(UUID player){
        final boolean[] isInMatch = {false};
        queuesMap.forEach((format,queue) -> {
            if(queue.hasPlayerInAny(player)){
                isInMatch[0] = true;
            }
        });
        return isInMatch[0];
    }

    //Finds the competitive queue a player is in queue for
    public CompetitiveQueue findPlayerInQueue(UUID player){
        final CompetitiveQueue[] queue = new CompetitiveQueue[1];
        queuesMap.forEach((k,v) -> {
            if(v.hasPlayerInQueue(player)){
                queue[0] = v;
            }
        });
        return queue[0];
    }

    //Finds the competitive queue a player is in match for
    public CompetitiveQueue findPlayerInMatch(UUID player){
        final CompetitiveQueue[] queue = new CompetitiveQueue[1];
        queuesMap.forEach((k,v) -> {
            if(v.hasPlayerInMatch(player)){
                queue[0] = v;
            }
        });
        return queue[0];
    }

    //Finds the competitive queue a player is in prematch for
    public CompetitiveQueue findPlayerInPreMatch(UUID player){
        final CompetitiveQueue[] queue = new CompetitiveQueue[1];
        queuesMap.forEach((k,v) -> {
            if(v.hasPlayerInPreMatch(player)){
                queue[0] = v;
            }
        });
        return queue[0];
    }

    //Finds the competitive queue a player is in anything for
    public CompetitiveQueue findPlayerInAny(UUID player){
        final CompetitiveQueue[] queue = new CompetitiveQueue[1];
        queuesMap.forEach((k,v) -> {
            if(v.hasPlayerInQueue(player) || v.hasPlayerInPreMatch(player)
                    || v.hasPlayerInMatch(player)){
                queue[0] = v;
            }
        });
        return queue[0];
    }

}
