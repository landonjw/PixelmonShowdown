package io.github.landonjw.pixelmonshowdown.arenas;

import io.github.landonjw.pixelmonshowdown.arenas.Arena;
import io.github.landonjw.pixelmonshowdown.arenas.ArenaLocation;
import io.github.landonjw.pixelmonshowdown.utilities.DataManager;
import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.UUID;

/*
    Manages all of the Arena objects for the matchmaking system
 */
public class ArenaManager {
    private ArrayList<Arena> arenas = new ArrayList<>();

    public ArrayList<Arena> getArenas(){
        return arenas;
    }

    //Load arenas from Arenas.conf configuration
    public void loadArenas(){
        if(DataManager.getConfigNode().getNode("Arena-Management", "Arenas-Enabled").getBoolean() == true){
            DataManager.getArenasNode().getNode("Arenas").getChildrenMap().forEach((k,v) -> {
                Arena arena = new Arena(k.toString());
                arena.load();
                arenas.add(arena);
            });
        }
    }

    //Check if all arenas are full
    public Boolean isArenasFull(){
        for(int i = 0; i < arenas.size(); i++){
            if(arenas.get(i).isLocationsFilled() != true){
                return false;
            }
        }
        return true;
    }

    //Add players to arena
    public Arena addPlayers(Player player1, Player player2){
        for(int i = 0; i < arenas.size(); i++){
            if(arenas.get(i).isLocationsFilled() == false){
                ArenaLocation locA = arenas.get(i).getLocationA();
                ArenaLocation locB = arenas.get(i).getLocationB();
                locA.setUUID(player1.getUniqueId());
                locA.setReturnLocation(player1.getLocation());
                locA.setReturnHeadRotation(player1.getHeadRotation());

                locB.setUUID(player2.getUniqueId());
                locB.setReturnLocation(player2.getLocation());
                locB.setReturnHeadRotation(player2.getHeadRotation());
                return arenas.get(i);
            }
        }
        return null;
    }

    //Get arena from arena name
    public Arena getArena(String arenaName){
        for(int i = 0; i < arenas.size(); i++){
            if(arenas.get(i).getName().equals(arenaName)){
                return arenas.get(i);
            }
        }
        return null;
    }

    //Remove players from arena
    public void remPlayers(UUID player1, UUID player2){
        for(int i = 0; i < arenas.size(); i++){
            //Check if arena is filled
            if(arenas.get(i).isLocationsFilled()){
                ArenaLocation locA = arenas.get(i).getLocationA();
                //Clear location if location has player uuid
                if(locA.getUUID().equals(player1) || locA.getUUID().equals(player2)){
                    locA.setUUID(null);
                }

                //Clear location if location has player uuid
                ArenaLocation locB = arenas.get(i).getLocationB();
                if(locB.getUUID().equals(player1) || locB.getUUID().equals(player2)){
                    locB.setUUID(null);
                }
            }
        }
    }

    //Get Arena players are in
    public Arena getArena(UUID player1, UUID player2){
        for(int i = 0; i < arenas.size(); i++){
            Boolean matchA = false;
            Boolean matchB = false;
            if(arenas.get(i).isLocationsFilled()){
                ArenaLocation locA = arenas.get(i).getLocationA();
                if(locA.getUUID().equals(player1) || locA.getUUID().equals(player2)){
                    matchA = true;
                }

                ArenaLocation locB = arenas.get(i).getLocationB();
                if(locB.getUUID().equals(player1) || locB.getUUID().equals(player2)){
                    matchB = true;
                }
                if(matchA == true && matchB == true){
                    return arenas.get(i);
                }
            }
        }
        return null;
    }

    //Add arena from Arenas.conf configuration
    public void addArena(){
        String arenaName = "Arena " + (arenas.size() + 1);
        Arena newArena = new Arena(arenaName);
        DataManager.getArenasNode().getNode("Arenas", arenaName, "LocationA", "X").setValue(0);
        DataManager.getArenasNode().getNode("Arenas", arenaName, "LocationA", "Y").setValue(0);
        DataManager.getArenasNode().getNode("Arenas", arenaName, "LocationA", "Z").setValue(0);
        DataManager.getArenasNode().getNode("Arenas", arenaName, "LocationA", "RX").setValue(0);
        DataManager.getArenasNode().getNode("Arenas", arenaName, "LocationA", "RY").setValue(0);
        DataManager.getArenasNode().getNode("Arenas", arenaName, "LocationA", "RZ").setValue(0);

        DataManager.getArenasNode().getNode("Arenas", arenaName, "LocationB", "X").setValue(0);
        DataManager.getArenasNode().getNode("Arenas", arenaName, "LocationB", "Y").setValue(0);
        DataManager.getArenasNode().getNode("Arenas", arenaName, "LocationB", "Z").setValue(0);
        DataManager.getArenasNode().getNode("Arenas", arenaName, "LocationB", "RX").setValue(0);
        DataManager.getArenasNode().getNode("Arenas", arenaName, "LocationB", "RY").setValue(0);
        DataManager.getArenasNode().getNode("Arenas", arenaName, "LocationB", "RZ").setValue(0);
        arenas.add(newArena);
    }

    //Sort arenas by arena number
    public void sortArenas(){
        ArrayList<Arena> newArenas = new ArrayList<>();
        ArrayList<Arena> ignoreArenas = new ArrayList<>();
        for(int i = 0; i < arenas.size(); i++){
            Arena lowestArena = null;
            int lowestNum = -1;
            for(int k = 0; k < arenas.size(); k++){
                String arenaName = arenas.get(k).getName();
                String[] splitName = arenaName.split("Arena ");
                if((Integer.parseInt(splitName[1]) < lowestNum || lowestNum == -1)
                        && ignoreArenas.contains(arenas.get(k)) == false){
                    lowestNum = Integer.parseInt(splitName[1]);
                    lowestArena = arenas.get(k);
                }
            }
            if(lowestArena != null){
                newArenas.add(lowestArena);
                ignoreArenas.add(lowestArena);
            }
        }
        arenas = newArenas;
    }
}
