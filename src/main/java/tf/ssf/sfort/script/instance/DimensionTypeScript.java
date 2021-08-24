package tf.ssf.sfort.script.instance;

import net.minecraft.world.dimension.DimensionType;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class DimensionTypeScript implements PredicateProvider<DimensionType>, Help {

    public Predicate<DimensionType> getLP(String in){
        return switch (in){
            case "dim_natural" -> DimensionType::isNatural;
            case "dim_ultrawarn" -> DimensionType::isUltrawarm;
            case "dim_piglin_safe" -> DimensionType::isPiglinSafe;
            case "dim_does_bed_work" -> DimensionType::isBedWorking;
            case "dim_does_anchor_work" -> DimensionType::isRespawnAnchorWorking;
            default -> null;
        };
    }

    //==================================================================================================================

    @Override
    public Predicate<DimensionType> getPredicate(String in, Set<Class<?>> dejavu){
        return getLP(in);
    }

    //==================================================================================================================

    public static final Map<String, String> help = new HashMap<>();
    static {
        help.put("dim_natural","Require natural dimension");
        help.put("dim_ultrawarn","Require ultra warm dimension");
        help.put("dim_piglin_safe","Require piglin safe dimension");
        help.put("dim_does_bed_work","Require dimension where beds don't blow");
        help.put("dim_does_anchor_work","Require dimension where respawn anchors work");
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
