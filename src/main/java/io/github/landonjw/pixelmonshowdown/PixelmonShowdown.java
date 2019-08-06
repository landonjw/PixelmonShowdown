package io.github.landonjw.pixelmonshowdown;

import io.github.landonjw.pixelmonshowdown.arenas.ArenaManager;
import io.github.landonjw.pixelmonshowdown.commands.ArenasCommand;
import io.github.landonjw.pixelmonshowdown.commands.DisplayCommand;
import io.github.landonjw.pixelmonshowdown.commands.ShowdownCommand;
import io.github.landonjw.pixelmonshowdown.battles.BattleManager;
import io.github.landonjw.pixelmonshowdown.placeholders.PlaceholderBridge;
import io.github.landonjw.pixelmonshowdown.queues.QueueManager;
import io.github.landonjw.pixelmonshowdown.utilities.*;
import com.google.inject.Inject;
import com.pixelmonmod.pixelmon.Pixelmon;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Plugin(id = PixelmonShowdown.PLUGIN_ID, name = PixelmonShowdown.PLUGIN_NAME, version = PixelmonShowdown.VERSION,
        description = "Competitive ELO System for Pixelmon Reforged.",
        url = "https://github.com/landonjw", authors = {"landonjw", "happyzleaf"},
        dependencies={
                @Dependency(id="pixelmon"),
                @Dependency(id="teslalibs"),
                @Dependency(id = "placeholderapi", optional = true)
        })
public class PixelmonShowdown {
    public static final String PLUGIN_ID = "pixelmonshowdown";
    public static final String PLUGIN_NAME = "PixelmonShowdown";
    public static final String VERSION = "1.2.6";
    
    private static Logger logger = LoggerFactory.getLogger(PLUGIN_NAME);

    @Inject
    @ConfigDir(sharedRoot=false)
    private Path dir;

    private static PixelmonShowdown instance;
    private static PluginContainer container;
    
    private static QueueManager queueManager = new QueueManager();
    private static ArenaManager arenaManager = new ArenaManager();

    public static PixelmonShowdown getInstance() {
        return instance;
    }

    public static PluginContainer getContainer() {
        return container;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static QueueManager getQueueManager() {
        return queueManager;
    }

    public static ArenaManager getArenaManager() {
        return arenaManager;
    }

    @Listener
    public void preInit(GamePreInitializationEvent event) {
        instance = this;
        container = Sponge.getPluginManager().getPlugin(PLUGIN_ID).get();
        
        DataManager.setup(dir);
    }

    @Listener
    public void init(GameInitializationEvent event) {
        CommandSpec showdown = CommandSpec.builder()
                .description(Text.of("Opens Showdown GUI"))
                .permission("pixelmonshowdown.user.command.pixelmonshowdown")
                .executor(new ShowdownCommand())
                .build();
        CommandSpec arenas = CommandSpec.builder()
                .description(Text.of("Opens Arena Management GUI"))
                .permission("pixelmonshowdown.admin.command.arenas")
                .executor(new ArenasCommand())
                .build();
        CommandSpec display = CommandSpec.builder()
                .description(Text.of("Displays profile or leaderboard in chat"))
                .permission("pixelmonshowdown.user.command.display")
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("type"))),
                        GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of("format"))))
                .executor(new DisplayCommand())
                .build();
        
        Sponge.getCommandManager().register(this, showdown, "showdown", "pixelmonshowdown", "sd", "psd");
        Sponge.getCommandManager().register(this, arenas, "psarenas", "arenas");
        Sponge.getCommandManager().register(this, display, "display", "psdisplay");
        Pixelmon.EVENT_BUS.register(new BattleManager());
        Sponge.getEventManager().registerListeners(this, new BattleManager());
    }

    @Listener
    public void postInit(GamePostInitializationEvent event) {
        queueManager.loadFromConfig();
        arenaManager.loadArenas();
        DataManager.startAutoSave();
        
        if (Sponge.getPluginManager().isLoaded("placeholderapi")) {
            PlaceholderBridge.register();
        }
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
