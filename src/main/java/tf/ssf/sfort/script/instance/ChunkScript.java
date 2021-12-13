package tf.ssf.sfort.script.instance;

import net.minecraft.util.Pair;
import net.minecraft.world.chunk.Chunk;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProviderExtendable;

import java.util.*;
import java.util.function.Predicate;

public class ChunkScript implements PredicateProviderExtendable<Chunk>, Help {

	public Predicate<Chunk> getLP(String in, String val){
		return switch (in){
			case "inhabited" -> {
				final long arg = Long.parseLong(val);
				yield chunk -> chunk.getInhabitedTime()>arg;
			}
			default -> null;
		};
	}

	//==================================================================================================================

	@Override
	public Predicate<Chunk> getPredicate(String in, String val, Set<Class<?>> dejavu){
		{
			Predicate<Chunk> out = getLP(in, val);
			if (out!=null) return out;
		}
		return PredicateProviderExtendable.super.getPredicate(in, val, dejavu);
	}

	//==================================================================================================================

	@Override
	public Map<String, String> getHelp(){
		return help;
	}
	@Override
	public List<Help> getImported(){
		return extend_help;
	}
	public final Map<String, String> help = new HashMap<>();
	public final List<Help> extend_help = new ArrayList<>();

	public ChunkScript() {
		help.put("inhabited:long","Minimum time players have loaded the chunk in ticks");
	}
	//==================================================================================================================

	public final TreeSet<Pair<Integer, PredicateProvider<Chunk>>> EXTEND = new TreeSet<>(Comparator.comparingInt(Pair::getLeft));

	@Override
	public void addProvider(PredicateProvider<Chunk> predicateProvider, int priority) {
		if (predicateProvider instanceof Help) extend_help.add((Help) predicateProvider);
		EXTEND.add(new Pair<>(priority, predicateProvider));
	}

	@Override
	public List<PredicateProvider<Chunk>> getProviders() {
		return EXTEND.stream().map(Pair::getRight).toList();
	}

}
