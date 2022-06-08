package tf.ssf.sfort.script.instance;

import net.minecraft.world.biome.Biome;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;

import java.util.Arrays;
import java.util.function.Predicate;

public class BiomeScript extends AbstractExtendablePredicateProvider<Biome> {

	public BiomeScript() {
		help.put("temperature tempeture:float","Player must be in biome warmer then this");
		help.put("precipitation:BiomePrecipitationID","Player must be in biome with this precipitation: rain | snow | none");
	}

	@Override
	public Predicate<Biome> getLocalPredicate(String in, String val){
		switch (in){
			case "temperature": case  "tempeture" : {
				final float arg = Float.parseFloat(val);
				return biome -> biome.getTemperature()>arg;
			}
			case "precipitation" : {
                final Biome.Precipitation arg = Arrays.stream(Biome.Precipitation.values()).filter(p -> val.equals(p.getName())).findFirst().orElse(null);
                return biome -> biome.getPrecipitation() == arg;
            }
			default: return null;
		}
	}

}
