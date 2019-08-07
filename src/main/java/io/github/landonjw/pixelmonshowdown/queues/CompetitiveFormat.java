package io.github.landonjw.pixelmonshowdown.queues;

import com.pixelmonmod.pixelmon.enums.EnumType;
import com.pixelmonmod.pixelmon.enums.forms.EnumDeoxys;
import io.github.landonjw.pixelmonshowdown.PixelmonShowdown;
import io.github.landonjw.pixelmonshowdown.queues.MonotypeClause;
import io.github.landonjw.pixelmonshowdown.utilities.DataManager;
import com.google.common.reflect.TypeToken;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import com.pixelmonmod.pixelmon.battles.rules.clauses.*;
import com.pixelmonmod.pixelmon.entities.npcs.registry.PokemonForm;
import com.pixelmonmod.pixelmon.entities.pixelmon.abilities.AbilityBase;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleType;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CompetitiveFormat {
    private String formatName;
    private int positionNum;

    private BattleRules battleRules = new BattleRules();
    private List<PokemonClause> pokemonClauses = new ArrayList<>();
    private List<ItemPreventClause> itemClauses = new ArrayList<>();
    private List<AbilityClause> abilityClauses = new ArrayList<>();
    private List<MoveClause> moveClauses = new ArrayList<>();
    private List<BattleClauseAll> complexClauses = new ArrayList<>();
    private List<String> strBattleRules = new ArrayList<>();
    private List<String> strPokemonClauses = new ArrayList<>();
    private List<String> strItemClauses = new ArrayList<>();
    private List<String> strAbilityClauses = new ArrayList<>();
    private List<String> strMoveClauses = new ArrayList<>();
    private int complexNum = 0;

    private boolean teamPreview = false;

    public CompetitiveFormat(String formatName){
        this.formatName = formatName;
    }

    //Get Pokemon clause
    public PokemonClause getPokemonClause(String pokemonClause){
        //Check if pokemon is a form
        if(pokemonClause.contains("-")){
            //Check if form exists
            if(PokemonForm.getFromName(pokemonClause).isPresent()){
                PokemonForm form = PokemonForm.getFromName(pokemonClause).get();

                String[] splitString = pokemonClause.split("-");

                if(splitString.length == 2) {

                    //Get pokemon name & form from string
                    String pokemon = splitString[0];
                    String suffix = "-" + splitString[1].toLowerCase();

                    //Check if pokemon exists
                    if(EnumSpecies.getFromName(pokemon).isPresent()) {

                        //Find object for suffix & return it
                        Object[] forms = EnumSpecies.formList.get(EnumSpecies.getFromName(pokemon).get()).toArray();
                        for (Object formsList : forms) {
                            IEnumForm castF = (IEnumForm) formsList;
                            if (castF.getFormSuffix().equals(suffix)) {
                                form.form = castF.getForm();
                                PokemonClause clause = new PokemonClause(pokemonClause, form);
                                return clause;
                            }
                        }
                    }
                }
            }
        }
        //If form isn't found or no suffix, try to find species & return it
        if (EnumSpecies.getFromNameAnyCase(pokemonClause) != null) {
            PokemonClause clause = new PokemonClause(pokemonClause, EnumSpecies.getFromNameAnyCase(pokemonClause));
            return clause;
        }
        //Throw error if pokemon not found
        PixelmonShowdown.getLogger().error("Error Getting Pokemon Clause: " + pokemonClause + ". Please check format config for errors.");
        return null;
    }

    //Adds Pokemon clause to rules
    public void addPokemonClause(String pokemonClause){

        if(pokemonClause.equals("Legendaries")) { //Ban all legends if Legendaries is in list
            strPokemonClauses.add("Legendaries");
            for (int k = 0; k < EnumSpecies.LEGENDARY_ENUMS.length; k++) {
                PokemonClause clause = getPokemonClause(EnumSpecies.LEGENDARY_ENUMS[k].getPokemonName());
                pokemonClauses.add(clause);
            }
        }
        else if(pokemonClause.equals("Ultrabeasts")){ //Ban all ultrabeasts if Ultrabeasts is in list
            strPokemonClauses.add("Ultrabeasts");
            for (int k = 0; k < EnumSpecies.ultrabeasts.size(); k++){
                PokemonClause clause = getPokemonClause(EnumSpecies.ultrabeasts.get(k));
                pokemonClauses.add(clause);
            }
        }
        else if(EnumSpecies.getFromNameAnyCase(pokemonClause) != null || PokemonForm.getFromName(pokemonClause).isPresent()) {
            PokemonClause clause = getPokemonClause(pokemonClause);
            if(clause != null){
                strPokemonClauses.add(pokemonClause);
                pokemonClauses.add(clause);
            }
        }
        else{
            PixelmonShowdown.getLogger().error("Error Adding Pokemon Clause: " + pokemonClause + ". Please check format config for errors.");
        }
    }

    //Gets item clause from String
    public ItemPreventClause getItemClause(String itemClause){
        //Pixelmon's item enums dont follow consistent convention, so have to check which one the item is...

        //Converts string to lowercase notation
        String itemLowerCase = itemClause.replace(" ", "").toLowerCase();

        //Converts string to camelCase notation
        String itemCamelCase = itemClause.trim();
        if(!itemCamelCase.substring(0,1).equals(itemCamelCase.substring(0,1).toLowerCase())){
            itemCamelCase = itemCamelCase.substring(0,1).toLowerCase() + itemCamelCase.substring(1);
        }
        while(itemCamelCase.indexOf(" ") != -1){
            int spaceIndex = itemCamelCase.indexOf(" ");
            String upperCase = String.valueOf(itemCamelCase.charAt(spaceIndex + 1)).toUpperCase();
            itemCamelCase = itemCamelCase.substring(0, spaceIndex) + upperCase + itemCamelCase.substring(spaceIndex + 2);
        }

        boolean errorCaught1 = false;
        boolean errorCaught2 = false;
        //Check if item exists
        try{
            if(itemLowerCase.equals("megastones")){
                return new ItemPreventClause(itemClause, EnumHeldItems.megaStone);
            }
            else if(itemLowerCase.equals("z crystals") || itemLowerCase.equals("z-crystals")){
                return new ItemPreventClause(itemClause, EnumHeldItems.zCrystal);
            }
            else {
                ItemPreventClause clause = new ItemPreventClause(itemClause, EnumHeldItems.valueOf(itemLowerCase));
                return clause;
            }
        }
        catch(Exception e){
            errorCaught1 = true;
        }

        try{
            ItemPreventClause clause = new ItemPreventClause(itemClause, EnumHeldItems.valueOf(itemCamelCase));
            return clause;
        }
        catch(Exception e){
            errorCaught2 = true;
        }

        if(errorCaught1 == true && errorCaught2 == true){
            PixelmonShowdown.getLogger().error("Error Adding Ability Clause: " + itemClause + ". Please check format config for errors.");
        }
        return null;
    }

    //Adds item clause to rules
    public void addItemClause(String itemClause){
        ItemPreventClause clause = getItemClause(itemClause);
        if(clause != null){
            strItemClauses.add(itemClause);
            itemClauses.add(clause);
        }
    }

    //Gets ability clause from String
    public AbilityClause getAbilityClause(String abilityClause){
        if(AbilityBase.getAbility(abilityClause).isPresent()) {
            Class<? extends AbilityBase> ability = AbilityBase.getAbility(abilityClause).get().getClass();
            AbilityClause clause = new AbilityClause(abilityClause, ability);
            return clause;
        }
        else{
            PixelmonShowdown.getLogger().error("Error Adding Ability Clause: " + abilityClause + ". Please check format config for errors.");
        }
        return null;
    }

    //Adds ability clause to rules
    public void addAbilityClause(String abilityClause){
        AbilityClause clause = getAbilityClause(abilityClause);
        if(!clause.equals(null)){
            strAbilityClauses.add(abilityClause);
            abilityClauses.add(clause);
        }
    }

    //Gets move clause from String
    public MoveClause getMoveClause(String moveClause){
        try{
            if(moveClause.equals("OHKO Moves")){
                MoveClause clause = new MoveClause("OHKO", "Fissure", "Guillotine", "Horn Drill", "Sheer Cold");
                return clause;
            }
            else {
                MoveClause clause = new MoveClause(moveClause, moveClause);
                return clause;
            }
        }
        catch (Exception e){
            PixelmonShowdown.getLogger().error("Error Adding Ability Clause: " + moveClause + ". Please check format config for errors.");
        }
        return null;
    }

    //Adds move clause to rules
    public void addMoveClause(String moveClause){
        MoveClause clause = getMoveClause(moveClause);
        if(clause != null){
            strMoveClauses.add(moveClause);
            moveClauses.add(clause);
        }
    }

    //Adds a complex clause (when all rules are present, pokemon is not allowed)
    public void addComplexClause(List<String> clauses){
        ArrayList<BattleClause> builtClauses = new ArrayList<>();
        for(int i = 0; i < clauses.size(); i++){
            String strClause = clauses.get(i);
            if(strClause.startsWith("P:")){
                String pokemonClause = strClause.substring(2);
                PokemonClause clause = getPokemonClause(pokemonClause);
                if(clause != null){
                    builtClauses.add(clause);
                }
            }
            else if(strClause.startsWith("A:")){
                String abilityClause = strClause.substring(2);
                AbilityClause clause = getAbilityClause(abilityClause);
                if(clause != null){
                    builtClauses.add(clause);
                }
            }
            else if(strClause.startsWith("I:")){
                String itemClause = strClause.substring(2);
                ItemPreventClause clause = getItemClause(itemClause);
                if(clause != null){
                    builtClauses.add(clause);
                }
            }
            else if(strClause.startsWith("M:")){
                String moveClause = strClause.substring(2);
                MoveClause clause = getMoveClause(moveClause);
                if(clause != null){
                    builtClauses.add(clause);
                }
            }
        }

        BattleClause[] arrClauses = new BattleClause[builtClauses.size()];
        arrClauses = builtClauses.toArray(arrClauses);

        BattleClauseAll comboClause = new BattleClauseAll("ComplexClause" + complexNum, arrClauses);

        complexNum++;
        complexClauses.add(comboClause);
    }

    //Build format from config
    public void buildFormat(){
        BattleRules newRules = new BattleRules();
        ArrayList<BattleClause> allClauses = new ArrayList<>();
        allClauses.addAll(pokemonClauses);
        allClauses.addAll(itemClauses);
        allClauses.addAll(abilityClauses);
        allClauses.addAll(moveClauses);
        allClauses.addAll(complexClauses);

        if(DataManager.getFormatsNode().getNode("Formats", formatName, "Battle-Rules", "Sleep-Clause").getBoolean()){
            strBattleRules.add("Sleep Clause");
            allClauses.add(new BattleClause("sleep"));
        }
        if(DataManager.getFormatsNode().getNode("Formats", formatName, "Battle-Rules", "Bag-Clause").getBoolean()){
            strBattleRules.add("Bag Clause");
            allClauses.add(new BattleClause("bag"));
        }
        if(DataManager.getFormatsNode().getNode("Formats", formatName, "Battle-Rules", "Inverse-Clause").getBoolean()){
            strBattleRules.add("Inverse Battle Clause");
            allClauses.add(new BattleClause("inverse"));
        }
        if(DataManager.getFormatsNode().getNode("Formats", formatName, "Battle-Rules", "Species-Clause").getBoolean()){
            strBattleRules.add("Species Clause");
            BattleClause clause = BattleClauseRegistry.getClauseRegistry().getClause("pokemon");
            allClauses.add(clause);
        }
        if(DataManager.getFormatsNode().getNode("Formats", formatName, "Battle-Rules", "Team-Preview").getBoolean()){
            this.teamPreview = true;
            strBattleRules.add("Team Preview");
        }

        String monotype = DataManager.getFormatsNode().getNode("Formats", formatName, "Battle-Rules", "Monotype").getString();
        if(monotype != null) {
            if (!monotype.equals("None")){
                if (monotype.equals("Any")) {
                    BattleClause clause = new MonotypeClause("Monotype", null);
                    allClauses.add(clause);
                    strBattleRules.add("Monotype");
                }
                else {
                    EnumType type = EnumType.parseType(monotype);
                    if (type != null) {
                        BattleClause clause = new MonotypeClause("Monotype", type);
                        allClauses.add(clause);
                        strBattleRules.add("Monotype " + type);
                    } else {
                        PixelmonShowdown.getLogger().error("Error Adding Monotype Clause: " + monotype + ". Please check format config for errors.");
                    }
                }
            }
        }

        newRules.setNewClauses(allClauses);

        newRules.battleType = EnumBattleType.Single;
        newRules.teamPreview = false;
        newRules.levelCap = DataManager.getFormatsNode().getNode("Formats", formatName, "Battle-Rules", "Level-Cap").getInt();
        newRules.fullHeal = DataManager.getFormatsNode().getNode("Formats", formatName, "Battle-Rules", "Full-Heal").getBoolean();
        newRules.numPokemon = DataManager.getFormatsNode().getNode("Formats", formatName, "Battle-Rules", "Num-Pokemon").getInt();
        newRules.raiseToCap = DataManager.getFormatsNode().getNode("Formats", formatName, "Battle-Rules", "Raise-To-Cap").getBoolean();
        newRules.turnTime = DataManager.getFormatsNode().getNode("Formats", formatName, "Battle-Rules", "Turn-Time").getInt();


        this.battleRules = newRules;
    }

    public String getFormatName(){
        return formatName;
    }

    public BattleRules getBattleRules(){
        return battleRules;
    }

    //Get str lists for adding to rules UI
    public List<String> getStrBattleRules(){
        return strBattleRules;
    }

    public List<String> getStrItemClauses(){
        return strItemClauses;
    }

    public List<String> getStrPokemonClauses(){
        return strPokemonClauses;
    }

    public List<String> getStrAbilityClauses(){
        return strAbilityClauses;
    }

    public List<String> getStrMoveClauses(){
        return strMoveClauses;
    }

    public int getPositionNum(){
        return positionNum;
    }

    public boolean isTeamPreview(){
        return teamPreview;
    }

    public void loadFormat(){
        try {
            this.positionNum = DataManager.getFormatsNode().getNode("Formats", formatName, "Listing-Number").getInt() - 1;

            List<String> strPokemonClauses = DataManager.getFormatsNode().getNode("Formats", formatName, "Pokemon-Clauses").getList(TypeToken.of(String.class));
            List<String> strItemClauses = DataManager.getFormatsNode().getNode("Formats", formatName, "Item-Clauses").getList(TypeToken.of(String.class));
            List<String> strAbilityClauses = DataManager.getFormatsNode().getNode("Formats", formatName, "Ability-Clauses").getList(TypeToken.of(String.class));
            List<String> strMoveClauses = DataManager.getFormatsNode().getNode("Formats", formatName, "Move-Clauses").getList(TypeToken.of(String.class));

            for (int i = 0; i < strPokemonClauses.size(); i++) {
                addPokemonClause(strPokemonClauses.get(i));
            }

            for (int i = 0; i < strItemClauses.size(); i++) {
                addItemClause(strItemClauses.get(i));
            }

            for (int i = 0; i < strAbilityClauses.size(); i++) {
                addAbilityClause(strAbilityClauses.get(i));
            }

            for(int i = 0; i < strMoveClauses.size(); i++){
                addMoveClause(strMoveClauses.get(i));
            }

            Iterator itr = DataManager.getFormatsNode().getNode("Formats", formatName, "Complex-Clauses").getChildrenList().iterator();

            while(itr.hasNext()){
                try {
                    SimpleCommentedConfigurationNode node = (SimpleCommentedConfigurationNode) itr.next();
                    Iterator strItr = node.getChildrenList().iterator();
                    ArrayList<String> strList = new ArrayList<>();
                    while(strItr.hasNext()){
                        try{
                            String clause = (String) ((SimpleCommentedConfigurationNode) strItr.next()).getValue();
                            strList.add(clause);
                        }
                        catch(Exception e){
                            PixelmonShowdown.getLogger().error("PixelmonShowdown has encountered an error loading complex causes! Check configuration for errors!");
                        }
                    }

                    addComplexClause(strList);
                }
                catch(Exception e){
                    PixelmonShowdown.getLogger().error("PixelmonShowdown has encountered an error loading complex causes! Check configuration for errors!");
                    PixelmonShowdown.getLogger().error(e.getMessage());
                }

            }

            buildFormat();

        }
        catch(ObjectMappingException e){
            PixelmonShowdown.getLogger().error("PixelmonShowdown has encountered an error loading format! Check configuration for errors!");
        }
    }

    public List<BattleClause> validateTeamList(List<Pokemon> team){
        List<BattleClause> clauseList = battleRules.getClauseList();
        List<BattleClause> caughtClauses = new ArrayList<>();
        for(BattleClause clause: clauseList){
            if(clause.validateTeam(team)){
                caughtClauses.add(clause);
            }
        }
        return caughtClauses;
    }
}