package io.github.landonjw.pixelmonshowdown.placeholders;

import io.github.landonjw.pixelmonshowdown.PixelmonShowdown;
import io.github.landonjw.pixelmonshowdown.queues.CompetitiveQueue;
import io.github.landonjw.pixelmonshowdown.queues.EloLadder;
import io.github.landonjw.pixelmonshowdown.queues.EloProfile;
import me.rojo8399.placeholderapi.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Arrays;

/**
 * @author happyzleaf
 * @since 06-Aug-19
 */
public class PlaceholderBridge {
	public static void register() {
		Sponge.getServiceManager().provideUnchecked(PlaceholderService.class).loadAll(new PlaceholderBridge(), PixelmonShowdown.getInstance()).stream()
				.map(builder -> builder.tokens("<format>_elo", "<format>_wins", "<format>_losses", "<format>_winrate", "total_wins", "total_losses", "average_winrate", "average_elo").author("happyzleaf, landonjw").plugin(PixelmonShowdown.getInstance()).version(PixelmonShowdown.VERSION))
				.forEach(builder -> {
					try {
						builder.buildAndRegister();
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
	}
	
	@Placeholder(id = "showdown")
	public Object showdown(@Source Player player, @Token String token) throws NoValueException {
		String[] values = token.split("_");
		if (values.length > 0) {
			String format = values[0];

			if(format.equals("total")){
				int size = PixelmonShowdown.getQueueManager().getAllQueues().size();
				String[] queues = PixelmonShowdown.getQueueManager().getAllQueues().keySet().toArray(new String[size]);

				if(values[1].equals("wins")){
					int numWins = 0;
					for(String queue: queues){
						EloLadder ladder = PixelmonShowdown.getQueueManager().findQueue(queue).getLadder();
						if(ladder.getProfile(player.getUniqueId()) != null){
							numWins += ladder.getProfile(player.getUniqueId()).getWins();
						}
					}
					return numWins;
				}
				else if(values[1].equals("losses")){
					int numLosses = 0;
					for(String queue: queues){
						EloLadder ladder = PixelmonShowdown.getQueueManager().findQueue(queue).getLadder();
						if(ladder.getProfile(player.getUniqueId()) != null){
							numLosses += ladder.getProfile(player.getUniqueId()).getLosses();
						}
					}
					return numLosses;
				}
				else{
					throw new NoValueException(String.format("Invalid argument. '%s'", values[1]), Arrays.asList("wins", "losses"));
				}
			}

			if(format.equals("average")){
				int size = PixelmonShowdown.getQueueManager().getAllQueues().size();
				String[] queues = PixelmonShowdown.getQueueManager().getAllQueues().keySet().toArray(new String[size]);
				int numLadders = 0;

				if(values[1].equals("winrate")){
					double winrateSum = 0.0;
					numLadders = 0;
					for(String queue: queues) {
						EloLadder ladder = PixelmonShowdown.getQueueManager().findQueue(queue).getLadder();
						if (ladder.getProfile(player.getUniqueId()) != null) {
							winrateSum += ladder.getProfile(player.getUniqueId()).getWinRate();
							numLadders += 1;
						}
					}
					return winrateSum / numLadders;
				}
				else if(values[1].equals("elo")){
					double eloSum = 0;
					numLadders = 0;
					for(String queue: queues) {
						EloLadder ladder = PixelmonShowdown.getQueueManager().findQueue(queue).getLadder();
						if (ladder.getProfile(player.getUniqueId()) != null) {
							eloSum += ladder.getProfile(player.getUniqueId()).getElo();
							numLadders += 1;
						}
					}
					return Math.round(eloSum / numLadders);
				}
				else{
					throw new NoValueException(String.format("Invalid argument. '%s'", values[1]), Arrays.asList("elo", "winrate"));
				}
			}

			EloLadder ladder = PixelmonShowdown.getQueueManager().getAllQueues().entrySet().stream().filter(e -> e.getKey().toLowerCase().equals(format)).findAny().map(e -> e.getValue().getLadder()).orElse(null);
			if (ladder == null) {
				throw new NoValueException(String.format("The format '%s' cannot be found.", format));
			}

			EloProfile profile = ladder.getProfile(player.getUniqueId());
			if (values.length > 1) {
				switch (values[1]) {
					case "elo":
						return profile == null ? EloProfile.ELO_FLOOR : profile.getElo();
					case "wins":
						return profile == null ? 0 : profile.getWins();
					case "losses":
						return profile == null ? 0 : profile.getLosses();
					case "winrate":
						return profile == null ? 0d : profile.getWinRate();
					default:
						throw new NoValueException(String.format("Invalid argument. '%s'", values[1]), Arrays.asList("elo", "wins", "losses", "winrate"));
				}
			}
			else {
				throw new NoValueException("Not enough arguments. You must specify the information needed.");
			}

		}
		else {
			throw new NoValueException("Not enough arguments. You must specify the format.");
		}
	}
}
