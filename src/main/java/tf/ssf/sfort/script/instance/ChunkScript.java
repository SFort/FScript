package tf.ssf.sfort.script.instance;

import net.minecraft.world.chunk.Chunk;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;

import java.util.function.Predicate;

public class ChunkScript extends AbstractExtendablePredicateProvider<Chunk> {

	public ChunkScript() {
		help.put("inhabited:long","Minimum time players have loaded the chunk in ticks");
	}

	@Override
	public Predicate<Chunk> getLocalPredicate(String in, String val){
		switch (in){
			case "inhabited":
				final long arg = Long.parseLong(val);
				return chunk -> chunk.getInhabitedTime()>arg;
			default: return null;
		}
	}
}
