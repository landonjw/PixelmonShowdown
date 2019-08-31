package io.github.landonjw.pixelmonshowdown.queues;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.rules.clauses.BattleClause;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.EnumType;
import com.pixelmonmod.pixelmon.enums.forms.EnumSilvally;
import io.github.landonjw.pixelmonshowdown.PixelmonShowdown;

import java.util.List;

/**
 * Creates a monotype clause where certain
 */
public class MonotypeClause extends BattleClause {
    EnumType type;

    public MonotypeClause(String id, EnumType type) {
        super(id);
        this.type = type;
    }

    //Check if team follows the clause type
    public boolean validateTeam(List<Pokemon> team){

        if(type == null) {
            EnumType type1;
            EnumType type2;
            type1 = team.get(0).getSpecies().getBaseStats(team.get(0).getFormEnum()).getType1();
            type2 = team.get(0).getSpecies().getBaseStats(team.get(0).getFormEnum()).getType2();
            PixelmonShowdown.getLogger().info(type1.getName());

            for (Pokemon pokemon : team) {
                boolean type1Different = false;
                boolean type2Different = false;
                PixelmonShowdown.getLogger().info(pokemon.getSpecies().name + pokemon.getFormEnum().getFormSuffix());
                PixelmonShowdown.getLogger().info(pokemon.getSpecies().getBaseStats(pokemon.getFormEnum()).getType1().getName());

                if (pokemon.getSpecies().getBaseStats(pokemon.getFormEnum()).getType1() != type1 &&
                        pokemon.getSpecies().getBaseStats(pokemon.getFormEnum()).getType1() != type2) {
                    type1Different = true;

                }
                if (pokemon.getSpecies().getBaseStats(pokemon.getFormEnum()).getType2() != type1 &&
                        pokemon.getSpecies().getBaseStats(pokemon.getFormEnum()).getType2() != type2) {
                    type2Different = true;
                }

                if(type1Different == true && type2Different == true){
                    return false;
                }
            }
            return true;
        }
        else{
            for (Pokemon pokemon: team){
                if(pokemon.getSpecies().getBaseStats(pokemon.getFormEnum()).getType1() != type &&
                        (pokemon.getSpecies().getBaseStats(pokemon.getFormEnum()).getType2() != type ||
                                pokemon.getSpecies().getBaseStats(pokemon.getFormEnum()).getType2() == null)){
                    return false;
                }
            }
            return true;
        }
    }
}
