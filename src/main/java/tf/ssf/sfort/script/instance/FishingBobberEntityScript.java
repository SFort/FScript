package tf.ssf.sfort.script.instance;

import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.util.Pair;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.PredicateProviderExtendable;

import java.util.*;
import java.util.function.Predicate;

public class FishingBobberEntityScript implements PredicateProviderExtendable<FishingBobberEntity>, Help {
    public EntityScript<FishingBobberEntity> ENTITY = new EntityScript<>();

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
        return PredicateProviderExtendable.super.getPredicate(in, val, dejavu);
    }

    @Override
    public Predicate<FishingBobberEntity> getPredicate(String in, Set<Class<?>> dejavu){
        {
            final Predicate<FishingBobberEntity> out = getLP(in);
            if (out != null) return out;
        }
        if (dejavu.add(EntityScript.class)){
            final Predicate<FishingBobberEntity> out = ENTITY.getPredicate(in, dejavu);
            if (out !=null) return out;
        }
        return PredicateProviderExtendable.super.getPredicate(in, dejavu);
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
    public final Map<String, String> help = new HashMap<>();
    public final List<Help> extend_help = new ArrayList<>();
    public FishingBobberEntityScript() {
        help.put("bobber_in_open_water is_bobber_in_open_water","Require a fishing bobber in open water");

        extend_help.add(new EntityScript<FishingBobberEntity>());
    }
    //==================================================================================================================

    public final TreeSet<Pair<Integer, PredicateProvider<FishingBobberEntity>>> EXTEND = new TreeSet<>(Comparator.comparingInt(Pair::getLeft));

    @Override
    public void addProvider(PredicateProvider<FishingBobberEntity> predicateProvider, int priority) {
        if (predicateProvider instanceof Help) extend_help.add((Help) predicateProvider);
        EXTEND.add(new Pair<>(priority, predicateProvider));
    }

    @Override
    public List<PredicateProvider<FishingBobberEntity>> getProviders() {
        return EXTEND.stream().map(Pair::getRight).toList();
    }

}
