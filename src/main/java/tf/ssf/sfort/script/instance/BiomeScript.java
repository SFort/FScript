package tf.ssf.sfort.script.instance;

import net.minecraft.world.biome.Biome;
import tf.ssf.sfort.script.instance.support.AbstractExtendablePredicateProvider;

import java.util.function.Predicate;

public class BiomeScript extends AbstractExtendablePredicateProvider<Biome> {

	public BiomeScript() {
		help.put("tempeture:float","Player must be in biome warmer then this");
		help.put("precipitation:BiomePrecipitationID","Player must be in biome with this precipitation: rain | snow | none");
		help.put("catagory:BiomeCatagoryID","Player must be in biome with this catagory");
	}

	@Override
	public Predicate<Biome> getLocalPredicate(String in, String val){
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

}
