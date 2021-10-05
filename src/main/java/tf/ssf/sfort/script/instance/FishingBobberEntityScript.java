package tf.ssf.sfort.script.instance;

import net.minecraft.entity.projectile.FishingBobberEntity;
import tf.ssf.sfort.script.Default;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.mixin_extended.Config;
import tf.ssf.sfort.script.mixin_extended.FishingBobberEntityExtended;

import java.util.*;
import java.util.function.Predicate;

public class FishingBobberEntityScript implements PredicateProvider<FishingBobberEntity>, Help {
    public EntityScript<FishingBobberEntity> ENTITY = new EntityScript<>();

    public Predicate<FishingBobberEntityExtended> getEP(String in){
        return switch (in){
            case "caught_fish", "has_caught_fish" -> FishingBobberEntityExtended::fscript$caughtFish;
            default -> null;
        };
    }
    public Predicate<FishingBobberEntity> getLP(String in){
        return switch (in){
            case "is_bobber_in_open_water", "bobber_in_open_water" -> FishingBobberEntity::isInOpenWater;
            default -> null;
        };
    }

    //==================================================================================================================

    @Override
    public Predicate<FishingBobberEntity> getPredicate(String in, String val, Set<Class<?>> dejavu){
        if (dejavu.add(EntityScript.class)){
            final Predicate<FishingBobberEntity> out = ENTITY.getPredicate(in, val, dejavu);
            if (out !=null) return out;
        }
        return null;
    }

    @Override
    public Predicate<FishingBobberEntity> getPredicate(String in, Set<Class<?>> dejavu){
        {
            final Predicate<FishingBobberEntity> out = getLP(in);
            if (out != null) return out;
        }
        if (Config.extended){
            final Predicate<FishingBobberEntityExtended> out = getEP(in);
            if (out != null) return entity -> out.test((FishingBobberEntityExtended)entity);
        }
        if (dejavu.add(EntityScript.class)){
            final Predicate<FishingBobberEntity> out = ENTITY.getPredicate(in, dejavu);
            if (out !=null) return out;
        }
        return null;
    }

    //==================================================================================================================


    @Override
    public Map<String, String> getHelp(){
        return help;
    }
    @Override
    public List<Help> getImported(){
        return extend_help;
    }
    public static final Map<String, String> help = new HashMap<String, String>();
    public static final List<Help> extend_help = new ArrayList<>();
    static {
        help.put("bobber_in_open_water is_bobber_in_open_water","Require a fishing bobber in open water");
        if (Config.extended) help.put("caught_fish has_caught_fish", "Require bobber to have cought a fish");

        extend_help.add(new EntityScript<FishingBobberEntity>());
    }
}
