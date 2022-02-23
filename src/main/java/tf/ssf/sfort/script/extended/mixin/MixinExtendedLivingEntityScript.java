package tf.ssf.sfort.script.extended.mixin;

import net.minecraft.entity.LivingEntity;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.extended.mixin.interfaces.LivingEntityInjected;
import tf.ssf.sfort.script.mixin.LivingEntityExtended;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class MixinExtendedLivingEntityScript implements PredicateProvider<LivingEntity>, Help {
    @Override
    public Predicate<LivingEntity> getPredicate(String in, Set<String> dejavu){
        return switch (in){
            case "sleeping_in_bed", "is_sleeping_in_bed" -> entity -> entity instanceof LivingEntityExtended && ((LivingEntityExtended)entity).fscript$isSleepingInBed();
            default -> null;
        };
    }
    @Override
    public Predicate<LivingEntity> getPredicate(String in, String val, Set<String> dejavu){
        return switch (in){
            case "attacked" -> {
                final int arg = Integer.parseInt(val);
                yield entity -> entity instanceof LivingEntityInjected && ((LivingEntityInjected)entity).fscript$attacked(arg);
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
    public MixinExtendedLivingEntityScript() {
        help.put("sleeping_in_bed is_sleeping_in_bed","Require sleeping in a bed");
        help.put("attacked:int","Minimum ticks passed since player was attacked");

    }
}
