package tf.ssf.sfort.script.instance;

import net.minecraft.world.biome.Biome;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;

import java.util.function.Predicate;

public class BiomeScript extends AbstractExtendablePredicateProvider<Biome> {

	public BiomeScript() {
		help.put("temperature tempeture:float","Player must be in biome warmer then this");
	}

	@Override
	public Predicate<Biome> getLocalPredicate(String in, String val){
		switch (in){
			case "temperature": case  "tempeture" : {
				final float arg = Float.parseFloat(val);
				return biome -> biome.getTemperature()>arg;
			}
			default: return null;
		}
	}

}
