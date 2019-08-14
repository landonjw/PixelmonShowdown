package io.github.landonjw.pixelmonshowdown.utilities;

import io.github.landonjw.pixelmonshowdown.PixelmonShowdown;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.scheduler.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Manages the loading and saving of all data within configuration files
 */
public class DataManager {
    private static Path dir, config, elos, formats, arenas;
    private static ConfigurationLoader<CommentedConfigurationNode> configLoad, elosLoad, formatsLoad, arenasLoad;
    private static CommentedConfigurationNode configNode, elosNode, formatsNode, arenasNode;
    private static final String[] FILES = {"Configuration.conf", "Elos.conf", "Formats.conf", "Arenas.conf"};
    private static boolean autoSaveEnabled;
    private static int interval;

    public static void setup(Path folder) {
        dir = folder;
        config = dir.resolve(FILES[0]);
        elos = dir.resolve(FILES[1]);
        formats = dir.resolve(FILES[2]);
        arenas = dir.resolve(FILES[3]);
        load();
        update();
    }

    public static void load() {
        try {
            if(!Files.exists(dir))
                Files.createDirectory(dir);

            PixelmonShowdown.getContainer().getAsset(FILES[0]).get().copyToFile(config, false, true);
            PixelmonShowdown.getContainer().getAsset(FILES[1]).get().copyToFile(elos, false, true);
            PixelmonShowdown.getContainer().getAsset(FILES[2]).get().copyToFile(formats, false, true);
            PixelmonShowdown.getContainer().getAsset(FILES[3]).get().copyToFile(arenas, false, true);

            configLoad = HoconConfigurationLoader.builder().setPath(config).build();
            elosLoad = HoconConfigurationLoader.builder().setPath(elos).build();
            formatsLoad = HoconConfigurationLoader.builder().setPath(formats).build();
            arenasLoad = HoconConfigurationLoader.builder().setPath(arenas).build();

            configNode = configLoad.load();
            elosNode = elosLoad.load();
            formatsNode = formatsLoad.load();
            arenasNode = arenasLoad.load();

            autoSaveEnabled = getConfigNode().getNode("Data-Management", "Automatic-Saving-Enabled").getBoolean();
            interval = getConfigNode().getNode("Data-Management", "Save-Interval").getInt();

        } catch(IOException e) {
            PixelmonShowdown.getLogger().error("Error loading PixelmonShowdown Configurations");
            e.printStackTrace();
        }

        saveAll();
    }

    public static void saveAll() {
        try {
            configLoad.save(configNode);
            elosLoad.save(elosNode);
            formatsLoad.save(formatsNode);
            arenasLoad.save(arenasNode);
        } catch (IOException e) {
            PixelmonShowdown.getLogger().error("Error saving PixelmonShowdown Configuration");
            e.printStackTrace();
        }
    }

    public static void saveElos() {
        try {
            PixelmonShowdown.getQueueManager().saveAllQueueProfiles();
            elosLoad.save(elosNode);
            PixelmonShowdown.getLogger().info("Elos saved.");
        } catch (IOException e) {
            PixelmonShowdown.getLogger().error("Error saving PixelmonShowdown Elos Configuration");
            e.printStackTrace();
        }
    }

    public static void saveArenas() {
        try {
            arenasLoad.save(arenasNode);
        } catch (IOException e) {
            PixelmonShowdown.getLogger().error("Error saving PixelmonShowdown Arenas Configuration");
            e.printStackTrace();
        }
    }

    public static void startAutoSave(){
        if (autoSaveEnabled == true) {
            Task task = Task.builder().execute(() -> saveElos()).interval(
                    interval, TimeUnit.MINUTES).async().submit(PixelmonShowdown.getInstance());
        }
    }

    public static void update() {
        try {
            configNode.mergeValuesFrom(HoconConfigurationLoader.builder()
                    .setURL(PixelmonShowdown.getContainer().getAsset(FILES[0]).get().getUrl())
                    .build()
                    .load(ConfigurationOptions.defaults()));

            elosNode.mergeValuesFrom(HoconConfigurationLoader.builder()
                    .setURL(PixelmonShowdown.getContainer().getAsset(FILES[1]).get().getUrl())
                    .build()
                    .load(ConfigurationOptions.defaults()));

            formatsNode.mergeValuesFrom(HoconConfigurationLoader.builder()
                    .setURL(PixelmonShowdown.getContainer().getAsset(FILES[2]).get().getUrl())
                    .build()
                    .load(ConfigurationOptions.defaults()));

            arenasNode.mergeValuesFrom(HoconConfigurationLoader.builder()
                    .setURL(PixelmonShowdown.getContainer().getAsset(FILES[3]).get().getUrl())
                    .build()
                    .load(ConfigurationOptions.defaults()));

            saveAll();

        } catch (IOException e) {
            PixelmonShowdown.getLogger().error("Error updating PixelmonShowdown Configuration");
            e.printStackTrace();
        }
    }



    public static CommentedConfigurationNode getConfigNode(Object... node) {
        return configNode.getNode(node);
    }

    public static CommentedConfigurationNode getElosNode(Object... node) {
        return elosNode.getNode(node);
    }

    public static CommentedConfigurationNode getFormatsNode(Object... node) {
        return formatsNode.getNode(node);
    }

    public static CommentedConfigurationNode getArenasNode(Object... node) {
        return arenasNode.getNode(node);
    }

    public static ConfigurationLoader<CommentedConfigurationNode> getConfigLoad(){
        return configLoad;
    }

    public static ConfigurationLoader<CommentedConfigurationNode> getElosLoad(){
        return elosLoad;
    }

    public static ConfigurationLoader<CommentedConfigurationNode> getFormatsLoad(){
        return formatsLoad;
    }

    public static ConfigurationLoader<CommentedConfigurationNode> getArenasLoad(){
        return arenasLoad;
    }
}