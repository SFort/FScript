package tf.ssf.sfort.script.instance;

import net.minecraft.world.dimension.DimensionType;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;

import java.util.Set;
import java.util.function.Predicate;

public class DimensionTypeScript implements PredicateProvider<DimensionType>, Help {
    @Override
    public Predicate<DimensionType> getPredicate(String in, String val, Set<Class<?>> dejavu){
        return getLP(in,val);
    }
    @Override
    public Predicate<DimensionType> getPredicate(String in, Set<Class<?>> dejavu){
        return getLP(in);
    }
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
    public Predicate<DimensionType> getLP(String in, String val){
        return null;
    }
    public String getHelp(){
        return
                String.format("\t%-20s%s%n","dim_natural","- Require natural dimension")+
                String.format("\t%-20s%s%n","dim_ultrawarn","- Require ultra warm dimension")+
                String.format("\t%-20s%s%n","dim_piglin_safe","- Require piglin safe dimension")+
                String.format("\t%-20s%s%n","dim_does_bed_work","- Require dimension where beds don't blow")+
                String.format("\t%-20s%s%n","dim_does_anchor_work","- Require dimension where respawn anchors work")
        ;
    }
    public String getAllHelp(Set<Class<?>> dejavu){
        return getHelp();
    }
}
