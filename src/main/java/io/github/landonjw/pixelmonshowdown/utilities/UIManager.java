package io.github.landonjw.pixelmonshowdown.utilities;

import io.github.landonjw.pixelmonshowdown.PixelmonShowdown;
import com.mcsimonflash.sponge.teslalibs.inventory.Action;
import com.mcsimonflash.sponge.teslalibs.inventory.Element;
import com.mcsimonflash.sponge.teslalibs.inventory.Layout;
import com.mcsimonflash.sponge.teslalibs.inventory.View;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import com.pixelmonmod.pixelmon.config.*;
import io.github.landonjw.pixelmonshowdown.arenas.Arena;
import io.github.landonjw.pixelmonshowdown.arenas.ArenaLocation;
import io.github.landonjw.pixelmonshowdown.arenas.ArenaManager;
import io.github.landonjw.pixelmonshowdown.battles.MatchMakingManager;
import io.github.landonjw.pixelmonshowdown.queues.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.*;
import java.util.function.Consumer;

/*
    Manages all user interfaces for the Pixelmon Showdown plugin
 */
public class UIManager {

    private QueueManager manager = PixelmonShowdown.getQueueManager();
    private String activeQueueFormat = null;
    private ItemType activeQueueBall = (ItemType) PixelmonItemsPokeballs.pokeBall;
    private String activeArena = null;
    private ItemType activeArenaBall = (ItemType) PixelmonItemsPokeballs.pokeBall;
    private PluginContainer container = PixelmonShowdown.getInstance().getContainer();
    private Player player;
    private UUID playerUUID;
    private int arenasPageNum = 1;
    private int leaderboardPageNum = 1;
    private int formatsPageNum = 1;
    private final int ELO_FLOOR = DataManager.getConfigNode().getNode("Elo-Management", "Elo-Range", "Elo-Floor").getInt();
    private Pokemon startingPokemon = null;

    public UIManager(Player player){
        this.player = player;
        this.playerUUID = player.getUniqueId();
    }

    public void openMainGUI(){
        Element queue;
        activeQueueFormat = null;
        activeQueueBall = (ItemType) PixelmonItemsPokeballs.pokeBall;
        leaderboardPageNum = 1;

        if(manager.isPlayerInQueue(playerUUID)) {
            //Leave Queue
            ItemStack itemLeaveQueue = ItemStack.of(ItemTypes.BOAT, 1);
            itemLeaveQueue.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Leave Queue"));
            Consumer<Action.Click> consEnterQueue = action -> {
                manager.findPlayerInQueue(playerUUID).remPlayerInQueue(playerUUID);
                player.sendMessage(Text.of(TextColors.WHITE, "[", TextColors.RED, "Pixelmon Showdown", TextColors.WHITE, "]",
                        TextColors.GOLD, " You have left queue!"));
                player.closeInventory();

            };
            queue = Element.of(itemLeaveQueue, consEnterQueue);
        }
        else if(manager.isPlayerInPreMatch(playerUUID) || manager.isPlayerInMatch(playerUUID)){
            //Leave Queue
            ItemStack itemInMatch = ItemStack.of(ItemTypes.WOOL, 1);
            itemInMatch.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "You are already in a match!"));
            itemInMatch.offer(Keys.DYE_COLOR, DyeColors.RED);
            queue = Element.of(itemInMatch);
        }
        else {
            //Enter Queue
            ItemStack itemEnterQueue = ItemStack.of((ItemType) PixelmonItemsPokeballs.pokeBall, 1);
            itemEnterQueue.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Enter Queue"));
            Consumer<Action.Click> consEnterQueue = action -> {
                if(player.hasPermission("pixelmonshowdown.user.action.openqueue")) {
                    openQueueGUI();
                }
                else{
                    player.sendMessage(Text.of(TextColors.RED, "You do not have permission to do this!"));
                    player.closeInventory();
                }
            };
            queue = Element.of(itemEnterQueue, consEnterQueue);
        }

        //Stats
        ItemStack itemStats = ItemStack.of(ItemTypes.PAPER, 1);
        itemStats.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Open Stats"));
        Consumer<Action.Click> consStats = action -> {
            if(player.hasPermission("pixelmonshowdown.user.action.openstats")) {
                openStatsGUI();
            }else{
                player.sendMessage(Text.of(TextColors.RED, "You do not have permission to do this!"));
                player.closeInventory();
            }
        };
        Element stats = Element.of(itemStats, consStats);

        //Leaderboard
        ItemStack itemLeaderboard = ItemStack.of(ItemTypes.MAP, 1);
        itemLeaderboard.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Open Leaderboard"));
        Consumer<Action.Click> consLeaderboard = action -> {
            if(player.hasPermission("pixelmonshowdown.user.action.openleaderboard")) {
                openLeaderboardGUI();
            }
            else{
                player.sendMessage(Text.of(TextColors.RED, "You do not have permission to do this!"));
                player.closeInventory();
            }
        };
        Element leaderboard = Element.of(itemLeaderboard, consLeaderboard);

        //Rules
        ItemStack itemRules = ItemStack.of(ItemTypes.BOOK, 1);
        itemRules.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Open Rules"));
        Consumer<Action.Click> consRules = action -> {
            if(player.hasPermission("pixelmonshowdown.user.action.openrules")) {
                openRulesGUI();
            }
            else{
                player.sendMessage(Text.of(TextColors.RED, "You do not have permission to do this!"));
                player.closeInventory();
            }
        };
        Element rules = Element.of(itemRules, consRules);

        ItemStack itemBorder = ItemStack.of(ItemTypes.STAINED_GLASS_PANE,1);
        itemBorder.offer(Keys.DYE_COLOR, DyeColors.RED);
        itemBorder.offer(Keys.DISPLAY_NAME, Text.of(""));
        Element border = Element.of(itemBorder);

        Layout newLayout = Layout.builder()
                .set(queue,10).set(stats,12).set(leaderboard, 14).set(rules, 16).border(border).build();

        View view = View.builder().archetype(InventoryArchetypes.CHEST)
                .property(InventoryTitle.of(Text.of(TextColors.RED, TextStyles.BOLD, "Pixelmon Showdown"))).build(container);

        view.define(newLayout);
        view.open(player);
    }

    private void openQueueGUI(){
        formatsPageNum = 1;

        ItemStack itemQueueType = ItemStack.of(activeQueueBall, 1);
        if(activeQueueFormat == null){
            itemQueueType.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Format: Not Chosen"));
        }
        else {
            itemQueueType.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Format: " + activeQueueFormat));
        }
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of(TextColors.GREEN, "Click to see available formats!"));
        itemQueueType.offer(Keys.ITEM_LORE, lore);
        Consumer<Action.Click> consEnterQueue = action -> {
            openFormatList("QueueGUI");
        };
        Element queueType = Element.of(itemQueueType, consEnterQueue);

        ItemStack itemValidateTeam = ItemStack.of(ItemTypes.WRITABLE_BOOK, 1);
        EntityPlayerMP participant = (EntityPlayerMP) player;
        Pokemon[] party = Pixelmon.storageManager.getParty(participant).getAll();
        ArrayList<Pokemon> pokemonList = new ArrayList<>();
        for (int i = 0; i < party.length; ++i) {
            if (party[i] == null) {
                continue;
            }
            pokemonList.add(party[i]);
        }

        Boolean doesValidate = false;

        if(activeQueueFormat == null){
            itemValidateTeam.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Team Eligible: ", TextColors.RED, "Format Not Chosen"));
        }
        else{
            BattleRules rules = manager.findQueue(activeQueueFormat).getFormat().getBattleRules();

            if(rules.validateTeam(pokemonList) == null){
                doesValidate = true;
            }

            if(doesValidate == false) {
                itemValidateTeam.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Team Eligible: ", TextColors.RED, "False"));
            }
            else{
                itemValidateTeam.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Team Eligible: ", TextColors.GREEN, "True"));
            }
        }
        Element validateTeam = Element.of(itemValidateTeam);

        Element startQueue;
        if(doesValidate) {
            ItemStack itemStartQueue = ItemStack.of(ItemTypes.WOOL, 1);
            itemStartQueue.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Confirm"));
            itemStartQueue.offer(Keys.DYE_COLOR, DyeColors.LIME);
            Consumer<Action.Click> consStartQueue = action -> {
                CompetitiveQueue queue = manager.findQueue(activeQueueFormat);
                if(queue != null){
                    queue.addPlayerInQueue(playerUUID);
                    MatchMakingManager.runTask();
                    player.sendMessage(Text.of(TextColors.WHITE, "[", TextColors.RED, "Pixelmon Showdown", TextColors.WHITE, "]",
                            TextColors.GOLD, " You have entered queue!"));
                    player.closeInventory();
                }
            };
            startQueue = Element.of(itemStartQueue, consStartQueue);
        }
        else{
            ItemStack itemStartQueueBlocked = ItemStack.of(ItemTypes.WOOL, 1);
            itemStartQueueBlocked.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Can't Confirm! Select Format or Eligible Team."));
            itemStartQueueBlocked.offer(Keys.DYE_COLOR, DyeColors.RED);
            startQueue = Element.of(itemStartQueueBlocked);
        }

        ItemStack itemBack = ItemStack.of((ItemType) PixelmonItemsHeld.redCard, 1);
        itemBack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Go Back"));
        Consumer<Action.Click> consBack = action -> {
            openMainGUI();
        };
        Element back = Element.of(itemBack, consBack);

        ItemStack itemBorder = ItemStack.of(ItemTypes.STAINED_GLASS_PANE,1);
        itemBorder.offer(Keys.DYE_COLOR, DyeColors.RED);
        itemBorder.offer(Keys.DISPLAY_NAME, Text.of(""));
        Element border = Element.of(itemBorder);

        Layout newLayout = Layout.builder().set(queueType, 11).set(validateTeam, 13).set(startQueue, 15).set(back, 25).border(border).build();

        View view = View.builder().archetype(InventoryArchetypes.CHEST)
                .property(InventoryTitle.of(Text.of(TextColors.RED, TextStyles.BOLD, "Queues"))).build(container);

        view.define(newLayout);
        view.open(player);
    }

    public void openStatsGUI(){
        formatsPageNum = 1;

        ItemStack itemQueueType = ItemStack.of(activeQueueBall, 1);
        if(activeQueueFormat == null){
            itemQueueType.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Format: Not Chosen"));
        }
        else {
            itemQueueType.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Format: " + activeQueueFormat));
        }
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of(TextColors.GREEN, "Click to see available formats!"));
        itemQueueType.offer(Keys.ITEM_LORE, lore);
        Consumer<Action.Click> consEnterQueue = action -> {
            openFormatList("StatsGUI");
        };
        Element queueType = Element.of(itemQueueType, consEnterQueue);

        ItemStack itemElo = ItemStack.of(ItemTypes.WOOL, 1);
        if(activeQueueFormat == null){
            itemElo.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Elo: Format Not Chosen"));
        }
        else {
            CompetitiveQueue queue = manager.findQueue(activeQueueFormat);
            if(queue != null) {
                if(queue.getLadder().hasPlayer(playerUUID) == true){
                    EloProfile playerProfile = queue.getLadder().getProfile(playerUUID);
                    if (playerProfile.getWins() == 0 && playerProfile.getLosses() == 0 && playerProfile.getElo() == ELO_FLOOR) {
                        itemElo.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Elo: No games recorded!"));
                    } else {
                        itemElo.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Elo: " + playerProfile.getElo()));
                    }
                }
                else{
                    itemElo.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Elo: No games recorded!"));
                }
            }
        }
        itemElo.offer(Keys.DYE_COLOR, DyeColors.CYAN);
        Element elo = Element.of(itemElo);

        ItemStack itemWins = ItemStack.of(ItemTypes.WOOL, 1);
        if(activeQueueFormat == null){
            itemWins.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Wins: Format Not Chosen"));
        }
        else {
            CompetitiveQueue queue = manager.findQueue(activeQueueFormat);
            if(queue != null) {
                if(queue.getLadder().hasPlayer(playerUUID) == true){
                    EloProfile playerProfile = queue.getLadder().getProfile(playerUUID);
                    if (playerProfile.getWins() == 0 && playerProfile.getLosses() == 0 && playerProfile.getElo() == ELO_FLOOR) {
                        itemWins.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Wins: No games recorded!"));
                    } else {
                        itemWins.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Wins: " + playerProfile.getWins()));
                    }
                }
                else{
                    itemWins.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Wins: No games recorded!"));
                }
            }
        }
        itemWins.offer(Keys.DYE_COLOR, DyeColors.LIME);
        Element wins = Element.of(itemWins);

        ItemStack itemLosses = ItemStack.of(ItemTypes.WOOL, 1);
        if(activeQueueFormat == null){
            itemLosses.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Losses: Format Not Chosen"));
        }
        else {
            CompetitiveQueue queue = manager.findQueue(activeQueueFormat);
            if(queue != null) {
                if(queue.getLadder().hasPlayer(playerUUID) == true){
                    EloProfile playerProfile = queue.getLadder().getProfile(playerUUID);
                    if (playerProfile.getWins() == 0 && playerProfile.getLosses() == 0 && playerProfile.getElo() == ELO_FLOOR) {
                        itemLosses.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Losses: No games recorded!"));
                    } else {
                        itemLosses.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Losses: " + playerProfile.getLosses()));
                    }
                }
                else{
                    itemLosses.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Losses: No games recorded!"));
                }
            }
        }
        itemLosses.offer(Keys.DYE_COLOR, DyeColors.RED);
        Element losses = Element.of(itemLosses);

        ItemStack itemWinrate = ItemStack.of(ItemTypes.WOOL, 1);
        if(activeQueueFormat == null){
            itemWinrate.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Winrate: Format Not Chosen"));
        }
        else {
            CompetitiveQueue queue = manager.findQueue(activeQueueFormat);
            if(queue != null) {
                if(queue.getLadder().hasPlayer(playerUUID) == true){
                    EloProfile playerProfile = queue.getLadder().getProfile(playerUUID);
                    if (playerProfile.getWins() == 0 && playerProfile.getLosses() == 0 && playerProfile.getElo() == ELO_FLOOR) {
                        itemWinrate.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Winrate: No games recorded!"));
                    } else {
                        itemWinrate.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Winrate: " + playerProfile.getWinRate()));
                    }
                }
                else{
                    itemWinrate.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Winrate: No games recorded!"));
                }
            }
        }
        itemWinrate.offer(Keys.DYE_COLOR, DyeColors.WHITE);
        Element winrate = Element.of(itemWinrate);

        ItemStack itemReset = ItemStack.of(ItemTypes.WRITABLE_BOOK, 1);
        itemReset.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Reset Stats"));
        Consumer<Action.Click> consReset = action -> {
            if(player.hasPermission("pixelmonshowdown.user.action.resetwl")) {
                CompetitiveQueue queue = manager.findQueue(activeQueueFormat);
                if(queue != null){
                    if(queue.getLadder().hasPlayer(playerUUID) == true){
                        EloLadder ladder = queue.getLadder();
                        EloProfile playerProfile = ladder.getProfile(playerUUID);
                        playerProfile.resetWL();
                        ladder.addAsActive(playerUUID);
                        openStatsGUI();
                    }
                }
            }
            else{
                player.sendMessage(Text.of(TextColors.RED, "You do not have permission to do this!"));
                player.closeInventory();
            }
        };
        Element reset = Element.of(itemReset, consReset);

        ItemStack itemBack = ItemStack.of((ItemType) PixelmonItemsHeld.redCard, 1);
        itemBack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Go Back"));
        Consumer<Action.Click> consBack = action -> {
            openMainGUI();
        };
        Element back = Element.of(itemBack, consBack);

        ItemStack itemBorder = ItemStack.of(ItemTypes.STAINED_GLASS_PANE,1);
        itemBorder.offer(Keys.DYE_COLOR, DyeColors.RED);
        itemBorder.offer(Keys.DISPLAY_NAME, Text.of(""));
        Element border = Element.of(itemBorder);

        Layout newLayout = Layout.builder().set(queueType, 10).set(elo, 12)
                .set(wins, 13).set(losses, 14).set(winrate, 15).set(reset, 16).set(back, 25).border(border).build();

        View view = View.builder().archetype(InventoryArchetypes.CHEST)
                .property(InventoryTitle.of(Text.of(TextColors.RED, TextStyles.BOLD, "Stats"))).build(container);

        view.define(newLayout);
        view.open(player);
    }

    public void openRulesGUI(){
        formatsPageNum = 1;

        ItemStack itemQueueType = ItemStack.of(activeQueueBall, 1);
        if(activeQueueFormat == null){
            itemQueueType.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Format: Not Chosen"));
        }
        else {
            itemQueueType.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Format: " + activeQueueFormat));
        }
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of(TextColors.GREEN, "Click to see available formats!"));
        itemQueueType.offer(Keys.ITEM_LORE, lore);
        Consumer<Action.Click> consEnterQueue = action -> {
            openFormatList("RulesGUI");
        };
        Element queueType = Element.of(itemQueueType, consEnterQueue);

        ItemStack itemRulesClauses = ItemStack.of(ItemTypes.KNOWLEDGE_BOOK, 1);
        if(activeQueueFormat == null){
            itemRulesClauses.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Format Not Chosen"));
        }
        else {
            itemRulesClauses.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Rules Clauses:"));
            List<String> clauses = manager.findQueue(activeQueueFormat).getFormat().getStrBattleRules();
            List<Text> clausesDisplay = new ArrayList<>();
            for(int i = 0; i < clauses.size(); i++){
                clausesDisplay.add(Text.of(TextColors.GRAY, clauses.get(i)));
            }
            itemRulesClauses.offer(Keys.ITEM_LORE, clausesDisplay);
        }
        Element ruleClauses = Element.of(itemRulesClauses);

        ItemStack itemPokemonClauses = ItemStack.of(ItemTypes.BOOK, 1);
        if(activeQueueFormat == null){
            itemPokemonClauses.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Format Not Chosen"));
        }
        else {
            itemPokemonClauses.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Pokemon Clauses:"));
            List<String> clauses = manager.findQueue(activeQueueFormat).getFormat().getStrPokemonClauses();
            List<Text> clausesDisplay = new ArrayList<>();
            for(int i = 0; i < clauses.size(); i++){
                clausesDisplay.add(Text.of(TextColors.GRAY, clauses.get(i)));
            }
            itemPokemonClauses.offer(Keys.ITEM_LORE, clausesDisplay);
        }
        Element pokemonClauses = Element.of(itemPokemonClauses);

        ItemStack itemAbilityClauses = ItemStack.of(ItemTypes.BOOK, 1);
        if(activeQueueFormat == null){
            itemAbilityClauses.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Format Not Chosen"));
        }
        else {
            itemAbilityClauses.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Ability Clauses:"));
            List<String> clauses = manager.findQueue(activeQueueFormat).getFormat().getStrAbilityClauses();
            List<Text> clausesDisplay = new ArrayList<>();
            for(int i = 0; i < clauses.size(); i++){
                clausesDisplay.add(Text.of(TextColors.GRAY, clauses.get(i)));
            }
            itemAbilityClauses.offer(Keys.ITEM_LORE, clausesDisplay);
        }
        Element abilityClauses = Element.of(itemAbilityClauses);

        ItemStack itemItemClauses = ItemStack.of(ItemTypes.BOOK, 1);
        if(activeQueueFormat == null){
            itemItemClauses.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Format Not Chosen"));
        }
        else {
            itemItemClauses.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Item Clauses:"));
            List<String> clauses = manager.findQueue(activeQueueFormat).getFormat().getStrItemClauses();
            List<Text> clausesDisplay = new ArrayList<>();
            for(int i = 0; i < clauses.size(); i++){
                clausesDisplay.add(Text.of(TextColors.GRAY, clauses.get(i)));
            }
            itemItemClauses.offer(Keys.ITEM_LORE, clausesDisplay);
        }
        Element itemClauses = Element.of(itemItemClauses);

        ItemStack itemMoveClauses = ItemStack.of(ItemTypes.BOOK, 1);
        if(activeQueueFormat == null){
            itemMoveClauses.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Format Not Chosen"));
        }
        else {
            itemMoveClauses.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Move Clauses:"));
            List<String> clauses = manager.findQueue(activeQueueFormat).getFormat().getStrMoveClauses();
            List<Text> clausesDisplay = new ArrayList<>();
            for(int i = 0; i < clauses.size(); i++){
                clausesDisplay.add(Text.of(TextColors.GRAY, clauses.get(i)));
            }
            itemMoveClauses.offer(Keys.ITEM_LORE, clausesDisplay);
        }
        Element moveClauses = Element.of(itemMoveClauses);

        ItemStack itemBack = ItemStack.of((ItemType) PixelmonItemsHeld.redCard, 1);
        itemBack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Go Back"));
        Consumer<Action.Click> consBack = action -> {
            openMainGUI();
        };
        Element back = Element.of(itemBack, consBack);

        ItemStack itemBorder = ItemStack.of(ItemTypes.STAINED_GLASS_PANE,1);
        itemBorder.offer(Keys.DYE_COLOR, DyeColors.RED);
        itemBorder.offer(Keys.DISPLAY_NAME, Text.of(""));
        Element border = Element.of(itemBorder);

        Layout newLayout = Layout.builder().set(queueType, 10).set(ruleClauses, 12).set(pokemonClauses, 13)
                .set(abilityClauses, 14).set(itemClauses, 15).set(moveClauses, 16).set(back, 25).border(border).build();

        View view = View.builder().archetype(InventoryArchetypes.CHEST)
                .property(InventoryTitle.of(Text.of(TextColors.RED, TextStyles.BOLD, "Rules"))).build(container);

        view.define(newLayout);
        view.open(player);
    }

    public void openLeaderboardGUI(){
        formatsPageNum = 1;

        ItemStack itemQueueType = ItemStack.of(activeQueueBall, 1);
        HashMap<Integer, Element> elements = new HashMap<>();

        if(activeQueueFormat == null){
            itemQueueType.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Format: Not Chosen"));
        }
        else {
            itemQueueType.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Format: " + activeQueueFormat));
        }
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of(TextColors.GREEN, "Click to see available formats!"));
        itemQueueType.offer(Keys.ITEM_LORE, lore);
        Consumer<Action.Click> consEnterQueue = action -> {
            openFormatList("LeaderboardGUI");
        };
        Element queueType = Element.of(itemQueueType, consEnterQueue);
        elements.put(10, queueType);


        if(activeQueueFormat != null) {
            ItemStack itemPrevious = ItemStack.of((ItemType) PixelmonItems.LtradeHolderLeft, 1);
            itemPrevious.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Previous"));
            Element previous;
            if(leaderboardPageNum > 1){
                Consumer<Action.Click> consGoPrevious = action -> {
                    this.leaderboardPageNum--;
                    openLeaderboardGUI();
                };

                previous = Element.of(itemPrevious, consGoPrevious);
            }
            else{
                previous = Element.of(itemPrevious);
            }
            elements.put(12, previous);

            CompetitiveQueue queue = manager.findQueue(activeQueueFormat);
            if (queue != null) {
                EloLadder ladder = queue.getLadder();

                EloProfile profile1 = ladder.getProfile(3 * (leaderboardPageNum - 1));
                if (profile1 != null) {
                    ItemStack itemPlayer1 = ItemStack.of(ItemTypes.SKULL, 1);
                    itemPlayer1.offer(Keys.SKULL_TYPE, SkullTypes.PLAYER);
                    String displayName = profile1.getPlayerName();

                    itemPlayer1.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, (3 * (leaderboardPageNum - 1) + 1) + ". " + displayName));
                    List<Text> player1Lore = new ArrayList<>();
                    player1Lore.add(Text.of(TextColors.GREEN, "Elo: " + profile1.getElo()));
                    itemPlayer1.offer(Keys.ITEM_LORE, player1Lore);
                    Element player1 = Element.of(itemPlayer1);
                    elements.put(13, player1);
                }

                EloProfile profile2 = ladder.getProfile(3 * (leaderboardPageNum - 1) + 1);

                if (profile2 != null) {
                    ItemStack itemPlayer2 = ItemStack.of(ItemTypes.SKULL, 1);
                    itemPlayer2.offer(Keys.SKULL_TYPE, SkullTypes.PLAYER);
                    String displayName = profile2.getPlayerName();

                    itemPlayer2.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, (3 * (leaderboardPageNum - 1) + 2) + ". " + displayName));
                    List<Text> player2Lore = new ArrayList<>();
                    player2Lore.add(Text.of(TextColors.GREEN, "Elo: " + profile2.getElo()));
                    itemPlayer2.offer(Keys.ITEM_LORE, player2Lore);
                    Element player2 = Element.of(itemPlayer2);
                    elements.put(14, player2);
                }

                EloProfile profile3 = ladder.getProfile(3 * (leaderboardPageNum - 1) + 2);
                if (profile3 != null) {
                    ItemStack itemPlayer3 = ItemStack.of(ItemTypes.SKULL, 1);
                    itemPlayer3.offer(Keys.SKULL_TYPE, SkullTypes.PLAYER);
                    String displayName = profile3.getPlayerName();

                    itemPlayer3.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, (3 * (leaderboardPageNum - 1) + 3) + ". " + displayName));
                    List<Text> player3Lore = new ArrayList<>();
                    player3Lore.add(Text.of(TextColors.GREEN, "Elo: " + profile3.getElo()));
                    itemPlayer3.offer(Keys.ITEM_LORE, player3Lore);
                    Element player3 = Element.of(itemPlayer3);
                    elements.put(15, player3);
                }

                ItemStack itemNext = ItemStack.of((ItemType) PixelmonItems.tradeHolderRight, 1);
                itemNext.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Next"));
                Element next;
                if (ladder.getLadderSize() > 3 * (leaderboardPageNum)) {
                    Consumer<Action.Click> consGoNext = action -> {
                        this.leaderboardPageNum++;
                        openLeaderboardGUI();
                    };
                    next = Element.of(itemNext, consGoNext);
                }
                else{
                    next = Element.of(itemNext);
                }
                elements.put(16, next);
            }
        }

        ItemStack itemBack = ItemStack.of((ItemType) PixelmonItemsHeld.redCard, 1);
        itemBack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Go Back"));
        Consumer<Action.Click> consBack = action -> {
            openMainGUI();
        };
        Element back = Element.of(itemBack, consBack);
        elements.put(25, back);

        ItemStack itemBorder = ItemStack.of(ItemTypes.STAINED_GLASS_PANE,1);
        itemBorder.offer(Keys.DYE_COLOR, DyeColors.RED);
        itemBorder.offer(Keys.DISPLAY_NAME, Text.of(""));
        Element border = Element.of(itemBorder);

        Layout newLayout = Layout.builder().setAll(elements).border(border).build();

        View view = View.builder().archetype(InventoryArchetypes.CHEST)
                .property(InventoryTitle.of(Text.of(TextColors.RED, TextStyles.BOLD, "Leaderboards"))).build(container);

        view.define(newLayout);
        view.open(player);
    }

    public void openArenasGUI(){
        arenasPageNum = 1;

        ItemStack itemArenas = ItemStack.of(activeArenaBall, 1);
        if(activeArena == null){
            itemArenas.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Arena: Not Chosen"));

        }
        else{
            itemArenas.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Arena: " + activeArena));
        }

        List<Text> lore = new ArrayList<>();
        lore.add(Text.of(TextColors.GREEN, "Click to see available arenas!"));
        itemArenas.offer(Keys.ITEM_LORE, lore);
        Consumer<Action.Click> consArenaList = action -> {
            openArenaList();
        };
        Element arenas = Element.of(itemArenas, consArenaList);

        ItemStack itemLocations = ItemStack.of(ItemTypes.FILLED_MAP, 1);
        itemLocations.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Location Management"));
        Consumer<Action.Click> consLocations = action -> {
            if(activeArena != null){
                openLocationsGUI();
            }
        };
        Element locations = Element.of(itemLocations, consLocations);

        ItemStack itemBorder = ItemStack.of(ItemTypes.STAINED_GLASS_PANE,1);
        itemBorder.offer(Keys.DYE_COLOR, DyeColors.RED);
        itemBorder.offer(Keys.DISPLAY_NAME, Text.of(""));
        Element border = Element.of(itemBorder);

        Layout newLayout = Layout.builder().set(arenas, 11).set(locations, 15)
                .border(border).border(border).build();

        View view = View.builder().archetype(InventoryArchetypes.CHEST)
                .property(InventoryTitle.of(Text.of(TextColors.RED, TextStyles.BOLD, "Arena Management"))).build(container);

        view.define(newLayout);
        view.open(player);
    }

    public void openLocationsGUI(){
        ItemStack setLocA = ItemStack.of((ItemType) PixelmonItemsTools.galacticBoots, 1);
        setLocA.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Set Location A"));
        Consumer<Action.Click> consSetLocA = action -> {
            ArenaManager arenaManager = PixelmonShowdown.getArenaManager();
            Arena arena = arenaManager.getArena(activeArena);
            if(arena != null){
                ArenaLocation locationA = arena.getLocationA();
                locationA.setWorld(player.getWorld().getName());
                locationA.setLocation(player.getLocation());
                locationA.setHeadRotation(player.getHeadRotation());
                arena.saveArena();
                player.sendMessage(Text.of(TextColors.GREEN, "Location A updated."));
            }
        };
        Element elementSetLocA = Element.of(setLocA, consSetLocA);

        ItemStack setLocB = ItemStack.of((ItemType) PixelmonItemsTools.neoBoots, 1);
        setLocB.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Set Location B"));
        Consumer<Action.Click> consSetLocB = action -> {
            ArenaManager arenaManager = PixelmonShowdown.getArenaManager();
            Arena arena = arenaManager.getArena(activeArena);
            if(arena != null){
                ArenaLocation locationB = arena.getLocationB();
                locationB.setWorld(player.getWorld().getName());
                locationB.setLocation(player.getLocation());
                locationB.setHeadRotation(player.getHeadRotation());
                arena.saveArena();
                player.sendMessage(Text.of(TextColors.GREEN, "Location B updated."));
            }
        };
        Element elementSetLocB = Element.of(setLocB, consSetLocB);

        ItemStack itemBorder = ItemStack.of(ItemTypes.STAINED_GLASS_PANE,1);
        itemBorder.offer(Keys.DYE_COLOR, DyeColors.RED);
        itemBorder.offer(Keys.DISPLAY_NAME, Text.of(""));
        Element border = Element.of(itemBorder);

        ItemStack itemBack = ItemStack.of((ItemType) PixelmonItemsHeld.redCard, 1);
        itemBack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Go Back"));
        Consumer<Action.Click> consBack = action -> {
            openArenasGUI();
        };
        Element back = Element.of(itemBack, consBack);


        Layout newLayout = Layout.builder().set(elementSetLocA, 11).set(elementSetLocB, 15)
                .set(back, 25).border(border).border(border).build();

        View view = View.builder().archetype(InventoryArchetypes.CHEST)
                .property(InventoryTitle.of(Text.of(TextColors.RED, TextStyles.BOLD, "Location Management"))).build(container);

        view.define(newLayout);
        view.open(player);
    }

    public void openArenaList(){
        ArenaManager arenaManager = PixelmonShowdown.getArenaManager();
        arenaManager.sortArenas();
        ArrayList<Arena> arenas = arenaManager.getArenas();
        HashMap<Integer, Element> elements = new HashMap<>();

        ItemStack itemPrevious = ItemStack.of((ItemType) PixelmonItems.LtradeHolderLeft, 1);
        itemPrevious.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Previous"));
        Element previous;
        if(arenasPageNum > 1){
            Consumer<Action.Click> previousArenas = action -> {
                arenasPageNum--;
                openArenaList();
            };
            previous = Element.of(itemPrevious, previousArenas);
        }
        else{
            previous = Element.of(itemPrevious);
        }
        elements.put(10, previous);

        for(int i = 0; i < 5; i++){
            if(arenas.size() > ((arenasPageNum - 1) * 5 + i)){
                Arena arena = arenas.get((arenasPageNum - 1) * 5 + i);
                if(arena != null){
                    int pokeballIndex = ((arenasPageNum - 1) * 5 + i) % PixelmonItemsPokeballs.getPokeballListWithMaster().size();
                    ItemType pokeball = (ItemType) PixelmonItemsPokeballs.getPokeballListWithMaster().get(pokeballIndex);
                    ItemStack itemArena = ItemStack.of(pokeball, 1);
                    itemArena.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, arena.getName()));
                    Consumer<Action.Click> selectArena = action -> {
                        activeArena = arena.getName();
                        activeArenaBall = pokeball;
                        openArenasGUI();
                    };
                    Element elementArena = Element.of(itemArena, selectArena);
                    elements.put(11 + i, elementArena);
                }
            }
        }

        ItemStack itemNext = ItemStack.of((ItemType) PixelmonItems.tradeHolderRight, 1);
        itemNext.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Next"));
        Element next;
        if(arenasPageNum * 5 < arenas.size()){
            Consumer<Action.Click> previousArenas = action -> {
                arenasPageNum++;
                openArenaList();
            };
            next = Element.of(itemNext, previousArenas);
        }
        else{
            next = Element.of(itemNext);
        }
        elements.put(16, next);

        ItemStack itemCreate = ItemStack.of((ItemType) PixelmonItems.pokemonEditor, 1);
        itemCreate.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Create Arena"));
        Consumer<Action.Click> consCreate = action -> {
            arenaManager.addArena();
            openArenaList();
        };
        Element create = Element.of(itemCreate, consCreate);
        elements.put(22, create);

        ItemStack itemBorder = ItemStack.of(ItemTypes.STAINED_GLASS_PANE,1);
        itemBorder.offer(Keys.DYE_COLOR, DyeColors.RED);
        itemBorder.offer(Keys.DISPLAY_NAME, Text.of(""));
        Element border = Element.of(itemBorder);

        Layout newLayout = Layout.builder().setAll(elements).border(border).build();

        View view = View.builder().archetype(InventoryArchetypes.CHEST)
                .property(InventoryTitle.of(Text.of(TextColors.RED, TextStyles.BOLD, "Arenas"))).build(container);

        view.define(newLayout);
        view.open(player);
    }

    public void openFormatList(String fromGUI){
        Object[] formats = manager.getAllQueues().keySet().toArray();

        if(DataManager.getConfigNode().getNode("GUI-Management", "Custom-Listing-Enabled").getBoolean() == true){
            Object[] newFormats = new Object[formats.length];
            QueueManager queueManager = PixelmonShowdown.getQueueManager();
            for(int i = 0; i < newFormats.length; i++){
                for(int k = 0; k < newFormats.length; k++){
                    String strFormatName = (String) formats[k];
                    if(queueManager.findQueue(strFormatName).getFormat().getPositionNum() == i){
                        newFormats[i] = formats[k];
                    }
                }
            }
            formats = newFormats;
        }

        HashMap<Integer, Element> elements = new HashMap<>();

        ItemStack itemPrevious = ItemStack.of((ItemType) PixelmonItems.LtradeHolderLeft, 1);
        itemPrevious.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Previous"));
        Element previous;
        if(formatsPageNum > 1){
            Consumer<Action.Click> previousFormats = action -> {
                formatsPageNum--;
                openFormatList(fromGUI);
            };
            previous = Element.of(itemPrevious, previousFormats);
        }
        else{
            previous = Element.of(itemPrevious);
        }
        elements.put(10, previous);

        for(int i = 0; i < 5; i++){
            if(formats.length > (formatsPageNum - 1) * 5 + i){
                String format = (String) formats[(formatsPageNum - 1) * 5 + i];
                if(format != null){
                    int pokeBallIndex = ((formatsPageNum - 1) * 5 + i) % PixelmonItemsPokeballs.getPokeballListWithMaster().size();
                    ItemType pokeBall = (ItemType) PixelmonItemsPokeballs.getPokeballListWithMaster().get(pokeBallIndex);
                    ItemStack itemFormat = ItemStack.of(pokeBall, 1);
                    itemFormat.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, format));
                    Consumer<Action.Click> selectArena = action -> {
                        activeQueueFormat = format;
                        activeQueueBall = pokeBall;
                        if(fromGUI.equals("QueueGUI")) {
                            openQueueGUI();
                        }
                        else if(fromGUI.equals("StatsGUI")){
                            openStatsGUI();
                        }
                        else if(fromGUI.equals("RulesGUI")){
                            openRulesGUI();
                        }
                        else if(fromGUI.equals("LeaderboardGUI")){
                            openLeaderboardGUI();
                        }
                    };
                    Element elementArena = Element.of(itemFormat, selectArena);
                    elements.put(11 + i, elementArena);
                }
            }

            ItemStack itemNext = ItemStack.of((ItemType) PixelmonItems.tradeHolderRight, 1);
            itemNext.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Next"));
            Element next;
            if(formatsPageNum * 5 < formats.length){
                Consumer<Action.Click> nextFormats = action -> {
                    formatsPageNum++;
                    openFormatList(fromGUI);
                };
                next = Element.of(itemNext, nextFormats);
            }
            else{
                next = Element.of(itemNext);
            }
            elements.put(16, next);
        }

        ItemStack itemBorder = ItemStack.of(ItemTypes.STAINED_GLASS_PANE,1);
        itemBorder.offer(Keys.DYE_COLOR, DyeColors.RED);
        itemBorder.offer(Keys.DISPLAY_NAME, Text.of(""));
        Element border = Element.of(itemBorder);

        Layout newLayout = Layout.builder().setAll(elements).border(border).build();

        View view = View.builder().archetype(InventoryArchetypes.CHEST)
                .property(InventoryTitle.of(Text.of(TextColors.RED, TextStyles.BOLD, "Formats"))).build(container);

        view.define(newLayout);
        view.open(player);
    }

    public void openTeamPreview(UUID opponentUUID){
        HashMap<Integer, Element> elements = new HashMap<>();

        ItemStack itemOpponent = ItemStack.of((ItemType) PixelmonItems.trainerEditor,1);

        String displayName = "null";
        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        if(userStorage.get().get(opponentUUID).isPresent()) {
            User user = userStorage.get().get(opponentUUID).get();
            displayName = user.getName();
        }
        itemOpponent.offer(Keys.DISPLAY_NAME, Text.of(TextColors.RED, displayName));
        Element elementOpponent = Element.of(itemOpponent);
        elements.put(1, elementOpponent);

        if(Sponge.getServer().getPlayer(opponentUUID).isPresent()) {
            Player opponent = Sponge.getServer().getPlayer(opponentUUID).get();
            EntityPlayerMP oppParticipant = (EntityPlayerMP) opponent;
            Pokemon[] oppParty = Pixelmon.storageManager.getParty(oppParticipant).getAll();
            ArrayList<Pokemon> oppPokemonList = new ArrayList<>();
            for (int i = 0; i < oppParty.length; ++i) {
                if (oppParty[i] == null) {
                    continue;
                }
                oppPokemonList.add(oppParty[i]);
            }

            for (int i = 0; i < oppPokemonList.size(); i++) {
                Pokemon pokemon = oppPokemonList.get(i);
                ItemStack itemPokemon = getPokemonPhoto(pokemon, pokemon.getForm());
                itemPokemon.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, pokemon.getSpecies().name));
                Element elementPokemon = Element.of(itemPokemon);
                elements.put(2 + i, elementPokemon);
            }
        }

        EntityPlayerMP playerParticipant = (EntityPlayerMP) player;
        Pokemon[] playerParty = Pixelmon.storageManager.getParty(playerParticipant).getAll();
        ArrayList<Pokemon> playerPokemonList = new ArrayList<>();
        for(int i = 0; i < playerParty.length; i++){
            if(playerParty[i] == null) {
                continue;
            }
            playerPokemonList.add(playerParty[i]);
        }

        for (int i = 0; i < playerPokemonList.size(); i++) {
            Pokemon pokemon = playerPokemonList.get(i);
            ItemStack itemPokemon = getPokemonPhoto(pokemon, pokemon.getForm());
            itemPokemon.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, pokemon.getSpecies().name));
            int index = i;
            Consumer<Action.Click> consChangeStarter = action -> {
                this.startingPokemon = playerPokemonList.get(index);
                openTeamPreview(opponentUUID);
            };
            Element elementPokemon = Element.of(itemPokemon, consChangeStarter);
            elements.put(20 + i, elementPokemon);
        }
        if(startingPokemon == null) {
            ItemStack itemStarter = ItemStack.of((ItemType) PixelmonItemsPokeballs.pokeBall, 1);
            itemStarter.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Starting Pokemon: " + playerPokemonList.get(0).getSpecies().name));
            Element starter = Element.of(itemStarter);
            elements.put(19, starter);
        }
        else{
            ItemStack itemStarter = ItemStack.of((ItemType) PixelmonItemsPokeballs.pokeBall, 1);
            itemStarter.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Starting Pokemon: " + startingPokemon.getSpecies().name));
            Element starter = Element.of(itemStarter);
            elements.put(19, starter);
        }

        ItemStack itemBorder = ItemStack.of(ItemTypes.STAINED_GLASS_PANE,1);
        itemBorder.offer(Keys.DYE_COLOR, DyeColors.RED);
        itemBorder.offer(Keys.DISPLAY_NAME, Text.of(""));

        Element border = Element.of(itemBorder);

        Layout newLayout = Layout.builder().setAll(elements).row(border, 1).set(border, 0, 8, 18, 26).build();

        View view = View.builder().archetype(InventoryArchetypes.CHEST)
                .property(InventoryTitle.of(Text.of(TextColors.RED, TextStyles.BOLD, "Team Preview"))).build(container);

        view.define(newLayout);
        view.open(player);
    }

    public ItemStack getPokemonPhoto(Pokemon pokemon, int form){
        net.minecraft.item.ItemStack itemStack = new net.minecraft.item.ItemStack(PixelmonItems.itemPixelmonSprite);
        NBTTagCompound tagCompound = new NBTTagCompound();
        itemStack.setTagCompound(tagCompound);
        tagCompound.setShort("ndex", (short) pokemon.getSpecies().getNationalPokedexInteger());
        tagCompound.setByte("form", (byte) pokemon.getForm());
        tagCompound.setByte("gender", pokemon.getGender().getForm());

        return (ItemStack) (Object) itemStack;
    }

    public Pokemon getStartingPokemon(){
        return startingPokemon;
    }
}
