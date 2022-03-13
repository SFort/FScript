package tf.ssf.sfort.script.extended.mixin;

import net.minecraft.entity.projectile.FishingBobberEntity;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.mixin.FishingBobberEntityExtended;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class MixinExtendedFishingBobberEntityScript implements PredicateProvider<FishingBobberEntity>, Help {
    @Override
    public Predicate<FishingBobberEntity> getPredicate(String in, String val, Set<String> dejavu){
        switch (in){
            case "caught_fish": case "has_caught_fish": return fis -> fis instanceof FishingBobberEntityExtended && ((FishingBobberEntityExtended)fis).fscript$caughtFish();
            default: return null;
        }
    }
    //==================================================================================================================

    @Override
    public Map<String, String> getHelp(){
        return help;
    }
    public final Map<String, String> help = new HashMap<>();
    public MixinExtendedFishingBobberEntityScript() {
        help.put("caught_fish has_caught_fish", "Require bobber to have cought a fish");
    }
}
