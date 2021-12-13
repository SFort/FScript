package tf.ssf.sfort.script.instance;

import net.minecraft.util.Pair;
import net.minecraft.world.biome.Biome;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.PredicateProviderExtendable;

import java.util.*;
import java.util.function.Predicate;

public class BiomeScript implements PredicateProviderExtendable<Biome>, Help {

	public Predicate<Biome> getLP(String in, String val){
		return switch (in){
			case "tempeture" -> {
				final float arg = Float.parseFloat(val);
				yield biome -> biome.getTemperature()>arg;
			}
			case "biome_catagory" -> {
			    final Biome.Category arg = Biome.Category.byName(val);
			    yield biome -> biome.getCategory() == arg;
			}
            case "precipitation" -> {
                final Biome.Precipitation arg = Biome.Precipitation.byName(val);
                yield biome -> biome.getPrecipitation() == arg;
            }
            default -> null;
		};
	}

	//==================================================================================================================
	@Override
	public Predicate<Biome> getPredicate(String in, String val, Set<Class<?>> dejavu){
		{
			final Predicate<Biome> out = getLP(in, val);
			if (out != null) return out;
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

	public BiomeScript() {
		help.put("tempeture:float","Player must be in biome warmer then this");
		help.put("precipitation:BiomePrecipitationID","Player must be in biome with this precipitation: rain | snow | none");
		help.put("catagory:BiomeCatagoryID","Player must be in biome with this catagory");
	}
	//==================================================================================================================

	public final TreeSet<Pair<Integer, PredicateProvider<Biome>>> EXTEND = new TreeSet<>(Comparator.comparingInt(Pair::getLeft));

	@Override
	public void addProvider(PredicateProvider<Biome> predicateProvider, int priority) {
		if (predicateProvider instanceof Help) extend_help.add((Help) predicateProvider);
		EXTEND.add(new Pair<>(priority, predicateProvider));
	}

	@Override
	public List<PredicateProvider<Biome>> getProviders() {
		return EXTEND.stream().map(Pair::getRight).toList();
	}

}
