package io.github.landonjw.pixelmonshowdown.queues;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.rules.clauses.BattleClause;
import com.pixelmonmod.pixelmon.enums.EnumType;
import io.github.landonjw.pixelmonshowdown.PixelmonShowdown;

import java.util.List;

/*
    Creates a monotype clause where certain
 */
public class MonotypeClause extends BattleClause {
    EnumType type;

    public MonotypeClause(String id, EnumType type) {
        super(id);
        this.type = type;
    }

    //Check if team follows the clause type
    public boolean validateTeam(List<Pokemon> team){
        boolean type1Different = false;
        boolean type2Different = false;

        if(type == null) {
            EnumType type1;
            EnumType type2;
            type1 = team.get(0).getSpecies().getBaseStats().getType1();
            type2 = team.get(0).getSpecies().getBaseStats().getType2();

            for (Pokemon pokemon : team) {
                if (pokemon.getSpecies().getBaseStats().getType1() != type1 &&
                        pokemon.getSpecies().getBaseStats().getType1() != type2) {
                    type1Different = true;
                }
                if (pokemon.getSpecies().getBaseStats().getType2() != type1 &&
                        pokemon.getSpecies().getBaseStats().getType2() != type2) {
                    type2Different = true;
                }
            }
            if(type1Different == true && (type2Different == true || type2 == null)){
                return false;
            }
            else{
                return true;
            }
        }
        else{
            for (Pokemon pokemon: team){
                if(pokemon.getSpecies().getBaseStats().getType2() != null) {
                }
                if(pokemon.getSpecies().getBaseStats().getType1() != type &&
                        (pokemon.getSpecies().getBaseStats().getType2() != type ||
                                pokemon.getSpecies().getBaseStats().getType2() == null)){
                    return false;
                }
            }
            return true;
        }
    }
}
