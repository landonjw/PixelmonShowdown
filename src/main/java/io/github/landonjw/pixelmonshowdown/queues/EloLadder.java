package io.github.landonjw.pixelmonshowdown.queues;

import io.github.landonjw.pixelmonshowdown.PixelmonShowdown;
import io.github.landonjw.pixelmonshowdown.queues.EloProfile;
import io.github.landonjw.pixelmonshowdown.utilities.DataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class EloLadder {
    private String formatName;
    private ArrayList<EloProfile> eloProfilesByElo = new ArrayList<>();
    private HashMap<UUID, EloProfile> eloProfilesByUUID = new HashMap<>();
    private ArrayList<EloProfile> activeEloProfiles = new ArrayList<>();

    public EloLadder(String formatName){
        this.formatName = formatName;
    }

    //Load profiles from config
    public void loadLadder(){
        try{
            DataManager.getElosNode().getNode("Player-Elos", formatName).getChildrenMap().forEach((uuid, stats) -> {
                UUID playerUUID = UUID.fromString(uuid.toString());
                EloProfile newProfile = new EloProfile(playerUUID, formatName);
                newProfile.loadProfile();
                eloProfilesByElo.add(newProfile);
                eloProfilesByUUID.put(playerUUID, newProfile);
            });


            sortProfiles();
        }
        catch(Exception e) {
            PixelmonShowdown.getLogger().error("PixelmonShowdown has encountered an error loading elo profiles!");
            e.printStackTrace();
        }
    }

    //Save all profiles to config object
    public void saveAllProfiles(){
        for(int i = 0; i < activeEloProfiles.size(); i++){
            activeEloProfiles.get(i).saveProfile();
        }
        activeEloProfiles.clear();
    }

    //Add profile as active for auto saving
    public void addAsActive(UUID player){
        EloProfile profile = getProfile(player);
        if(profile != null && activeEloProfiles.contains(profile) == false){
            activeEloProfiles.add(profile);
        }
    }

    //Check if ladder has player
    public boolean hasPlayer(UUID player){
        return (eloProfilesByUUID.get(player) != null);
    }

    //Update player on ladder through binary insertion
    public void updatePlayer(UUID player){
        if(hasPlayer(player)) {
            EloProfile profile = getProfile(player);
            eloProfilesByElo.remove(profile);

            int index = binaryInsertionSort(eloProfilesByElo, profile);
            eloProfilesByElo.add(index, profile);
        }
    }

    //Find best place to insert player by elo
    private int binaryInsertionSort(ArrayList<EloProfile> profiles, EloProfile profile){
        int low = 0;
        int high = profiles.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            Integer midVal = profiles.get(mid).getElo();
            int cmp = midVal.compareTo(profile.getElo());

            if (cmp > 0)
                low = mid + 1;
            else if (cmp < 0)
                high = mid - 1;
            else
                return mid;
        }
        return low;
    }

    //Add player to the ladder
    public void addPlayer(UUID player, String playerName){
        if(!hasPlayer(player)){
            EloProfile profile = new EloProfile(player, formatName);
            profile.setPlayerName(playerName);

            //Since player will have 1000 elo, we can simply add it to the end of the array
            eloProfilesByElo.add(profile);
            eloProfilesByUUID.put(player, profile);
            activeEloProfiles.add(profile);
        }
    }

    //Delete player from the ladder
    public void delPlayer(UUID player){
        EloProfile profile = getProfile(player);
        if(profile != null){
            eloProfilesByElo.remove(profile);
            eloProfilesByUUID.remove(profile);
        }
    }

    //Get a profile from the ladder
    public EloProfile getProfile(UUID player){
        return eloProfilesByUUID.get(player);
    }

    //Get a profile from the ladder by index (for leaderboard)
    public EloProfile getProfile(int i){
        if(eloProfilesByElo.size() > i) {
            return eloProfilesByElo.get(i);
        }
        return null;
    }

    //Get size of the ladder
    public int getLadderSize(){
        return eloProfilesByElo.size();
    }

    //Sort profiles from highest elo to lowest elo
    public void sortProfiles(){
        if(eloProfilesByElo.size() == 0){
            return;
        }

        this.eloProfilesByElo = mergeSort(eloProfilesByElo);
    }

    //Sort the list using Merge Sort
    public ArrayList<EloProfile> mergeSort(ArrayList<EloProfile> whole) {

        ArrayList<EloProfile> left = new ArrayList<>();
        ArrayList<EloProfile> right = new ArrayList<>();
        int center;

        if (whole.size() == 1) {
            return whole;
        } else {
            center = whole.size() / 2;
            // copy the left half of whole into the left.
            for (int i = 0; i < center; i++) {
                left.add(whole.get(i));
            }

            //copy the right half of whole into the new arraylist.
            for (int i = center; i < whole.size(); i++) {
                right.add(whole.get(i));
            }

            // Sort the left and right halves of the arraylist.
            left = mergeSort(left);
            right = mergeSort(right);

            // Merge the results back together.
            merge(left, right, whole);
        }
        return whole;
    }

    //Merge indexes in order
    private void merge(ArrayList<EloProfile> left, ArrayList<EloProfile> right, ArrayList<EloProfile> whole) {
        int leftIndex = 0;
        int rightIndex = 0;
        int wholeIndex = 0;

        // As long as neither the left nor the right ArrayList has
        // been used up, keep taking the smaller of left.get(leftIndex)
        // or right.get(rightIndex) and adding it at both.get(bothIndex).
        while (leftIndex < left.size() && rightIndex < right.size()) {
            if (left.get(leftIndex).getElo() > right.get(rightIndex).getElo()) {
                whole.set(wholeIndex, left.get(leftIndex));
                leftIndex++;
            } else {
                whole.set(wholeIndex, right.get(rightIndex));
                rightIndex++;
            }
            wholeIndex++;
        }

        ArrayList<EloProfile> rest;
        int restIndex;
        if (leftIndex >= left.size()) {
            // The left ArrayList has been use up...
            rest = right;
            restIndex = rightIndex;
        } else {
            // The right ArrayList has been used up...
            rest = left;
            restIndex = leftIndex;
        }

        // Copy the rest of whichever ArrayList (left or right) was not used up.
        for (int i = restIndex; i < rest.size(); i++) {
            whole.set(wholeIndex, rest.get(i));
            wholeIndex++;
        }
    }
}
