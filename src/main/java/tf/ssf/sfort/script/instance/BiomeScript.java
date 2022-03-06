package tf.ssf.sfort.script.instance;

import net.minecraft.world.biome.Biome;
import tf.ssf.sfort.script.instance.util.AbstractExtendablePredicateProvider;

import java.util.function.Predicate;

public class BiomeScript extends AbstractExtendablePredicateProvider<Biome> {

	public BiomeScript() {
		help.put("temperature tempeture:float","Player must be in biome warmer then this");
		help.put("precipitation:BiomePrecipitationID","Player must be in biome with this precipitation: rain | snow | none");
	}

	@Override
	public Predicate<Biome> getLocalPredicate(String in, String val){
		return switch (in){
			case "temperature", "tempeture" -> {
				final float arg = Float.parseFloat(val);
				yield biome -> biome.getTemperature()>arg;
			}
            case "precipitation" -> {
                final Biome.Precipitation arg = Biome.Precipitation.byName(val);
                yield biome -> biome.getPrecipitation() == arg;
            }
            default -> null;
		};
	}

}
