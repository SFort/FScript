package tf.ssf.sfort.script.instance;

import net.minecraft.world.biome.Biome;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class BiomeScript implements PredicateProvider<Biome>, Help {

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
		return getLP(in,val);
	}

	//==================================================================================================================
	public static final Map<String, String> help = new HashMap<>();
	static {
		help.put("tempeture:float","Player must be in biome warmer then this");
		help.put("precipitation:BiomePrecipitationID","Player must be in biome with this precipitation: rain | snow | none");
		help.put("catagory:BiomeCatagoryID","Player must be in biome with this catagory");
	}
	@Override
	public Map<String, String> getHelp(){
		return help;
	}
	@Override
	public Map<String, String> getAllHelp(Set<Class<?>> dejavu){
		return getHelp();
	}
}
