package io.github.landonjw.pixelmonshowdown;

import io.github.landonjw.pixelmonshowdown.arenas.ArenaManager;
import io.github.landonjw.pixelmonshowdown.commands.arenasCommand;
import io.github.landonjw.pixelmonshowdown.commands.displayCommand;
import io.github.landonjw.pixelmonshowdown.commands.showdownCommand;
import io.github.landonjw.pixelmonshowdown.battles.BattleManager;
import io.github.landonjw.pixelmonshowdown.queues.QueueManager;
import io.github.landonjw.pixelmonshowdown.utilities.*;
import com.google.inject.Inject;
import com.pixelmonmod.pixelmon.Pixelmon;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

@Plugin(
        id="pixelmonshowdown",
        name="PixelmonShowdown",
        description="Competitive ELO System for Pixelmon 7.0.6",
        url="https://github.com/landonjw", authors={"landonjw"},
        version="1.2.5",
        dependencies={
                @Dependency(id=Pixelmon.MODID),
                @Dependency(id="teslalibs")
        })

public class PixelmonShowdown {

    @Inject
    private Logger logger;

    @Inject
    private PluginContainer container;

    @Inject
    @ConfigDir(sharedRoot=false)
    private Path dir;

    @Inject
    private static PixelmonShowdown instance;

    @Inject
    private QueueManager queueManager = new QueueManager();

    @Inject
    private ArenaManager arenaManager = new ArenaManager();

    private final String VERSION = "1.2.5";

    CommandSpec showdown = CommandSpec.builder()
            .description(Text.of("Opens Showdown GUI"))
            .permission("pixelmonshowdown.user.command.pixelmonshowdown")
            .executor(new showdownCommand())
            .build();
    CommandSpec arenas = CommandSpec.builder()
            .description(Text.of("Opens Arena Management GUI"))
            .permission("pixelmonshowdown.admin.command.arenas")
            .executor(new arenasCommand())
            .build();
    CommandSpec display = CommandSpec.builder()
            .description(Text.of("Displays profile or leaderboard in chat"))
            .permission("pixelmonshowdown.user.command.display")
            .arguments(
                    GenericArguments.onlyOne(GenericArguments.string(Text.of("type"))),
                    GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of("format"))))
            .executor(new displayCommand())
            .build();

    public static PixelmonShowdown getInstance() {
        return instance;
    }

    public PluginContainer getContainer() {
        return this.container;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public QueueManager getQueueManager() {
        return this.queueManager;
    }

    public ArenaManager getArenaManager() {
        return this.arenaManager;
    }

    @Listener
    public void onPreInitialization(GamePreInitializationEvent event){
        instance = this;
        DataManager.setup(dir);
    }

    @Listener
    public void onInitialization(GameInitializationEvent event){
        Sponge.getCommandManager().register(this, showdown, "showdown", "pixelmonshowdown", "sd", "psd");
        Sponge.getCommandManager().register(this, arenas, "psarenas", "arenas");
        Sponge.getCommandManager().register(this, display, "display", "psdisplay");
        Pixelmon.EVENT_BUS.register(new BattleManager());
        Sponge.getEventManager().registerListeners(this, new BattleManager());
    }

    @Listener
    public void onPostInitialization(GamePostInitializationEvent event){
        queueManager.loadFromConfig();
        arenaManager.loadArenas();
        DataManager.startAutoSave();
    }



    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        logger.info("PixelmonShowdown " + VERSION + " Successfully Launched");
    }

    @Listener
    public void onServerStop(GameStoppingServerEvent event) {
        queueManager.saveAllQueueProfiles();
        DataManager.saveElos();
        DataManager.saveArenas();
    }
}