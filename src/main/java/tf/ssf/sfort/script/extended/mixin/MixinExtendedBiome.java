package tf.ssf.sfort.script.extended.mixin;

import net.minecraft.world.biome.Biome;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.mixin.BiomeExtended;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class MixinExtendedBiome implements PredicateProvider<Biome>, Help {

    @Override
    public Predicate<Biome> getPredicate(String in, String val, Set<String> dejavu){
        return switch (in){
            case "biome_category" -> {
                final Biome.Category arg = Biome.Category.byName(val);
                yield biome -> ((Object)biome) instanceof BiomeExtended  && ((BiomeExtended)(Object)biome).fscript$getCategory() == arg;
            }
            default -> null;
        };
    }
    //==================================================================================================================

    @Override
    public Map<String, String> getHelp(){
        return help;
    }
    public final Map<String, String> help = new HashMap<>();
    public MixinExtendedBiome() {
        help.put("biome_category:BiomeCatagoryID","Player must be in biome with this category");
    }
}
