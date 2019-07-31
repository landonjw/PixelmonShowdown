package io.github.landonjw.pixelmonshowdown.arenas;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.UUID;

public class ArenaLocation {
    private UUID uuid = null;
    private String world = null;
    private double x;
    private double y;
    private double z;
    private double rX;
    private double rY;
    private double rZ;
    private double returnWorld;
    private Location<World> returnLocation;
    private Vector3d returnHeadRotation;

    public ArenaLocation(String world, double x, double y, double z, double rX, double rY, double rZ){
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.rX = rX;
        this.rY = rY;
        this.rZ = rZ;
    }

    public ArenaLocation(){}

    public UUID getUUID(){
        return uuid;
    }

    public void setUUID(UUID uuid){
        this.uuid = uuid;
    }

    public Boolean hasUUID(UUID compUUID){
        if(uuid == null){
            return false;
        }
        else if(uuid.equals(compUUID)){
            return true;
        }
        else{
            return false;
        }
    }

    public void setWorld(String world){
        this.world = world;
    }

    public String getWorld(){
        return world;
    }

    public Location<World> getLocation(){
        Location<World> loc = new Location<>(Sponge.getServer().getWorld(world).get()
                , x, y, z);
        return loc;
    }

    public void setLocation(Location<World> location){
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
    }

    public void setReturnLocation(Location<World> location){
        this.returnLocation = location;
    }

    public void setHeadRotation(Vector3d headRotation){
        this.rX = headRotation.getX();
        this.rY = headRotation.getY();
        this.rZ = headRotation.getZ();
    }

    public void setReturnHeadRotation(Vector3d headRotation){
        this.returnHeadRotation = headRotation;
    }



    public Vector3d getHeadRotation(){
        return new Vector3d(rX, rY, rZ);
    }

    public Location<World> getReturnLocation(){
        return returnLocation;
    }

    public Vector3d getReturnHeadRotation(){
        return returnHeadRotation;
    }
}
