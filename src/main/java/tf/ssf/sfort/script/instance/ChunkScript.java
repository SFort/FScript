package tf.ssf.sfort.script.instance;

import net.minecraft.world.chunk.Chunk;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.Help;

import java.util.Set;
import java.util.function.Predicate;

public class ChunkScript implements PredicateProvider<Chunk>, Help {
	@Override
	public Predicate<Chunk> getPredicate(String in, String val, Set<Class<?>> dejavu){
		return getLP(in,val);
	}
	@Override
	public Predicate<Chunk> getPredicate(String in, Set<Class<?>> dejavu){
		return getLP(in);
	}
	public Predicate<Chunk> getLP(String in){
		return null;
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
	public String getHelp(){
		return
				String.format("\t%-20s%-70s%s%n","inhabited","- Minimum time players have loaded the chunk in ticks","long")
        ;
	}
	public String getAllHelp(Set<Class<?>> dejavu){
		return getHelp();
	}
}
