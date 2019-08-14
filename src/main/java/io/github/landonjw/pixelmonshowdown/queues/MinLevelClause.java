package io.github.landonjw.pixelmonshowdown.queues;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.rules.clauses.BattleClause;

import java.util.List;

/**
 * Creates a monotype clause where certain
 */
public class MinLevelClause extends BattleClause {
    int level;

    public MinLevelClause(String id, int level) {
        super(id);
        this.level = level;
    }

    //Check if team follows the clause type
    public boolean validateTeam(List<Pokemon> team){
        for(Pokemon pokemon: team){
            if(pokemon.getLevel() < level){
                return false;
            }
        }
        return true;
    }
}