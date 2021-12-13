package tf.ssf.sfort.script.instance;

import net.minecraft.util.Pair;
import net.minecraft.world.dimension.DimensionType;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.PredicateProviderExtendable;

import java.util.*;
import java.util.function.Predicate;

public class DimensionTypeScript implements PredicateProviderExtendable<DimensionType>, Help {

    public Predicate<DimensionType> getLP(String in){
        return switch (in){
            case "natural", "is_natural" -> DimensionType::isNatural;
            case "ultrawarn", "is_ultrawarm" -> DimensionType::isUltrawarm;
            case "piglin_safe", "is_piglin_safe" -> DimensionType::isPiglinSafe;
            case "does_bed_work", "is_bed_working", "bed_working" -> DimensionType::isBedWorking;
            case "does_anchor_work", "respawn_anchor_working", "is_respawn_anchor_working" -> DimensionType::isRespawnAnchorWorking;
            case "skylight", "has_skylight" -> DimensionType::hasSkyLight;
            case "ceiling", "has_ceiling" -> DimensionType::hasCeiling;
            case "raids", "has_raids" -> DimensionType::hasRaids;
            case "ender_dragon_fight", "has_ender_dragon_fight" -> DimensionType::hasEnderDragonFight;
            case "fixed_time", "has_fixed_time" -> DimensionType::hasFixedTime;
            default -> null;
        };
    }
    public Predicate<DimensionType> getLP(String in, String val){
        return switch (in){
            case "coordinate_scale" -> {
                final double arg = Double.parseDouble(val);
                yield dim -> dim.getCoordinateScale() >= arg;
            }
            default -> null;
        };
    }
    //==================================================================================================================

    @Override
    public Predicate<DimensionType> getPredicate(String in, Set<Class<?>> dejavu){
        {
            final Predicate<DimensionType> out = getLP(in);
            if (out != null) return out;
        }
        return PredicateProviderExtendable.super.getPredicate(in, dejavu);
    }

    @Override
    public Predicate<DimensionType> getPredicate(String in, String val, Set<Class<?>> dejavu){
        {
            final Predicate<DimensionType> out = getLP(in, val);
            if (out != null) return out;
        }
        return PredicateProviderExtendable.super.getPredicate(in, val, dejavu);
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

    public DimensionTypeScript() {
        help.put("natural is_natural","Require natural dimension");
        help.put("ultrawarn is_ultrawarm","Require ultra warm dimension");
        help.put("piglin_safe is_piglin_safe","Require piglin safe dimension");
        help.put("bed_working is_bed_working does_bed_work","Require dimension where beds don't blow");
        help.put("respawn_anchor_working is_respawn_anchor_working does_anchor_work","Require dimension where respawn anchors work");
        help.put("skylight has_skylight", "Require dimension to have a skylight");
        help.put("ceiling has_ceiling", "Require dimension to have a ceiling");
        help.put("raids has_raids", "Require dimension to have raids");
        help.put("ender_dragon_fight has_ender_dragon_fight", "Require dimension to have an ender dragon");
        help.put("fixed_time has_fixed_time", "Require dimension to have fixed time");
        help.put("coordinate_scale:double", "Minimum dimension coordinate scale");
    }
    //==================================================================================================================

    public final TreeSet<Pair<Integer, PredicateProvider<DimensionType>>> EXTEND = new TreeSet<>(Comparator.comparingInt(Pair::getLeft));

    @Override
    public void addProvider(PredicateProvider<DimensionType> predicateProvider, int priority) {
        if (predicateProvider instanceof Help) extend_help.add((Help) predicateProvider);
        EXTEND.add(new Pair<>(priority, predicateProvider));
    }

    @Override
    public List<PredicateProvider<DimensionType>> getProviders() {
        return EXTEND.stream().map(Pair::getRight).toList();
    }

}