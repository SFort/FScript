package tf.ssf.sfort.script.instance;

import net.minecraft.world.biome.Biome;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class BiomeScript implements PredicateProvider<Biome>, Help {
	public static final Map<String, String> help = new HashMap<>();
	@Override
	public Predicate<Biome> getPredicate(String in, String val, Set<Class<?>> dejavu){
		return getLP(in,val);
	}
	@Override
	public Predicate<Biome> getPredicate(String in, Set<Class<?>> dejavu){
		return getLP(in);
	}
	public Predicate<Biome> getLP(String in){
		return null;
	}
	public Predicate<Biome> getLP(String in, String val){
		return switch (in){
			case "tempeture" -> {
				float arg = Float.parseFloat(val);
				yield biome -> biome.getTemperature()>arg;
			}
			case "biome_catagory" -> {
			    Biome.Category arg = Biome.Category.byName(val);
			    yield biome -> biome.getCategory() == arg;
			}
            case "precipitation" -> {
                Biome.Precipitation arg = Biome.Precipitation.byName(val);
                yield biome -> biome.getPrecipitation() == arg;
            }
            default -> null;
		};
	}
	static {
		help.put("tempeture:float","Player must be in biome warmer then this");
		help.put("precipitation:BiomePrecipitationID","Player must be in biome with this precipitation: rain | snow | none");
		help.put("catagory:BiomeCatagoryID","Player must be in biome with this catagory");
	}
	public Map<String, String> getHelp(){
		return help;
	}
	public Map<String, String> getAllHelp(Set<Class<?>> dejavu){
		return getHelp();
	}
}
