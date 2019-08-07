package io.github.landonjw.pixelmonshowdown.placeholders;

import io.github.landonjw.pixelmonshowdown.PixelmonShowdown;
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
				.map(builder -> builder.tokens("<format>_elo", "<format>_wins", "<format>_losses", "<format>_winrate").author("happyzleaf").plugin(PixelmonShowdown.getInstance()).version(PixelmonShowdown.VERSION))
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
			EloLadder ladder = PixelmonShowdown.getQueueManager().getAllQueues().entrySet().stream().filter(e -> e.getKey().toLowerCase().equals(format)).findAny().map(e -> e.getValue().getLadder()).orElse(null);
			if (ladder == null) {
				throw new NoValueException(String.format("The format '%s' cannot be found.", format));
			}
			
//			if (!ladder.hasPlayer(player.getUniqueId())) {
//				ladder.addPlayer(player.getUniqueId(), player.getName());
//			}
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
			} else {
				throw new NoValueException("Not enough arguments. You must specify the information needed.");
			}
		} else {
			throw new NoValueException("Not enough arguments. You must specify the format.");
		}
	}
}
