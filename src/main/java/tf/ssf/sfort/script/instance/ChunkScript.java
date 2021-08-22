package tf.ssf.sfort.script.instance;

import net.minecraft.world.chunk.Chunk;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.Help;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class ChunkScript implements PredicateProvider<Chunk>, Help {
	@Override
	public Predicate<Chunk> getPredicate(String in, String val, Set<Class<?>> dejavu){
		return getLP(in,val);
	}

	public Predicate<Chunk> getLP(String in, String val){
		return switch (in){
			case "inhabited" -> {
				long arg = Long.parseLong(val);
				yield chunk -> chunk.getInhabitedTime()>arg;
			}
            default -> null;
		};
	}
	public static final Map<String, String> help = new HashMap<>();
	static {
		help.put("inhabited:long","Minimum time players have loaded the chunk in ticks");
	}
	public Map<String, String> getHelp(){
		return help;
	}
	public Map<String, String> getAllHelp(Set<Class<?>> dejavu){
		return getHelp();
	}
}
