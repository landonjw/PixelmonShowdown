package io.github.landonjw.pixelmonshowdown.arenas;

import com.flowpowered.math.vector.Vector3d;
import io.github.landonjw.pixelmonshowdown.utilities.DataManager;
import org.spongepowered.api.world.Location;

/**
 * Defines an arena with two locations for players
 */
public class Arena{
    private String name;
    private ArenaLocation locationA;
    private ArenaLocation locationB;

    public Arena(String name, ArenaLocation locationA, ArenaLocation locationB){
        this.name = name;
        this.locationA = locationA;
        this.locationB = locationB;
    }

    public Arena(String name){
        this.name = name;
        this.locationA = new ArenaLocation();
        this.locationB = new ArenaLocation();
    }

    //Loads arena from the Arenas.conf configuration
    public void load(){
        String locAWorld = DataManager.getArenasNode().getNode("Arenas", name, "LocationA", "World").getString();
        double locAX = DataManager.getArenasNode().getNode("Arenas", name, "LocationA", "X").getDouble();
        double locAY = DataManager.getArenasNode().getNode("Arenas", name, "LocationA", "Y").getDouble();
        double locAZ = DataManager.getArenasNode().getNode("Arenas", name, "LocationA", "Z").getDouble();
        double locARX = DataManager.getArenasNode().getNode("Arenas", name, "LocationA", "RX").getDouble();
        double locARY = DataManager.getArenasNode().getNode("Arenas", name, "LocationA", "RY").getDouble();
        double locARZ = DataManager.getArenasNode().getNode("Arenas", name, "LocationA", "RZ").getDouble();
        this.locationA = new ArenaLocation(locAWorld, locAX, locAY, locAZ, locARX, locARY, locARZ);

        String locBWorld = DataManager.getArenasNode().getNode("Arenas", name, "LocationB", "World").getString();
        double locBX = DataManager.getArenasNode().getNode("Arenas", name, "LocationB", "X").getDouble();
        double locBY = DataManager.getArenasNode().getNode("Arenas", name, "LocationB", "Y").getDouble();
        double locBZ = DataManager.getArenasNode().getNode("Arenas", name, "LocationB", "Z").getDouble();
        double locBRX = DataManager.getArenasNode().getNode("Arenas", name, "LocationB", "RX").getDouble();
        double locBRY = DataManager.getArenasNode().getNode("Arenas", name, "LocationB", "RY").getDouble();
        double locBRZ = DataManager.getArenasNode().getNode("Arenas", name, "LocationB", "RZ").getDouble();
        this.locationB = new ArenaLocation(locBWorld, locBX, locBY, locBZ, locBRX, locBRY, locBRZ);
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void setLocationA(ArenaLocation location){
        this.locationA = location;
    }

    public ArenaLocation getLocationA(){
        return locationA;
    }

    public void setLocationB(ArenaLocation location){
        this.locationB = location;
    }

    public ArenaLocation getLocationB(){
        return locationB;
    }

    public boolean isLocationsFilled(){
        return (locationA.getUUID() != null && locationB.getUUID() != null);
    }

    //Saves an arena
    public void saveArena(){

        if(locationA.getWorld() != null) {
            Location locALoc = locationA.getLocation();
            Vector3d locAVec = locationA.getHeadRotation();

            DataManager.getArenasNode().getNode("Arenas", name, "LocationA", "World").setValue(locationA.getWorld());
            DataManager.getArenasNode().getNode("Arenas", name, "LocationA", "X").setValue(locALoc.getX());
            DataManager.getArenasNode().getNode("Arenas", name, "LocationA", "Y").setValue(locALoc.getY());
            DataManager.getArenasNode().getNode("Arenas", name, "LocationA", "Z").setValue(locALoc.getZ());
            DataManager.getArenasNode().getNode("Arenas", name, "LocationA", "RX").setValue(locAVec.getX());
            DataManager.getArenasNode().getNode("Arenas", name, "LocationA", "RY").setValue(locAVec.getY());
            DataManager.getArenasNode().getNode("Arenas", name, "LocationA", "RZ").setValue(locAVec.getZ());
        }

        if(locationB.getWorld() != null) {
            Location locBLoc = locationB.getLocation();
            Vector3d locBVec = locationB.getHeadRotation();
            DataManager.getArenasNode().getNode("Arenas", name, "LocationB", "World").setValue(locationB.getWorld());
            DataManager.getArenasNode().getNode("Arenas", name, "LocationB", "X").setValue(locBLoc.getX());
            DataManager.getArenasNode().getNode("Arenas", name, "LocationB", "Y").setValue(locBLoc.getY());
            DataManager.getArenasNode().getNode("Arenas", name, "LocationB", "Z").setValue(locBLoc.getZ());
            DataManager.getArenasNode().getNode("Arenas", name, "LocationB", "RX").setValue(locBVec.getX());
            DataManager.getArenasNode().getNode("Arenas", name, "LocationB", "RY").setValue(locBVec.getY());
            DataManager.getArenasNode().getNode("Arenas", name, "LocationB", "RZ").setValue(locBVec.getZ());
        }
    }
}
