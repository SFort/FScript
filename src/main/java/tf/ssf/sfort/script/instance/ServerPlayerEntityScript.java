package tf.ssf.sfort.script.instance;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;

import java.util.function.Predicate;

public class ServerPlayerEntityScript<T extends ServerPlayerEntity> extends AbstractExtendablePredicateProvider<T> {

	public ServerPlayerEntityScript() {
		help.put("advancement:AdvancementID","Require advancement unlocked");
		help.put("respawn_distance:double","Require player to be nearby their respawn (usually a bed)");
	}

	@Override
	public Predicate<T> getLocalPredicate(String in, String val){
		switch (in){
			case "respawn_distance" :{
				final double arg = Double.parseDouble(val);
				return player -> {
					final BlockPos pos = player.getSpawnPointPosition();
					final ServerWorld world = player.getWorld();
					final RegistryKey<World> dim = player.getSpawnPointDimension();
					if (pos == null || world == null) return false;
					return dim.equals(world.getRegistryKey()) && pos.isWithinDistance(player.getPos(), arg);
				};
			}
			case "advancement" : {
				final Identifier arg = new Identifier(val);
				return player -> {
					final MinecraftServer server = player.getServer();
					if (server == null) return false;
					return player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(arg)).isDone();
				};
			}
			default : return null;
		}
	}

}
