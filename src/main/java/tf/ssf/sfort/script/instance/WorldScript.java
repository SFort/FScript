package tf.ssf.sfort.script.instance;

import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import tf.ssf.sfort.script.Default;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;

import java.util.*;
import java.util.function.Predicate;

public class WorldScript implements PredicateProvider<World>, Help {
    public Predicate<World> getLP(String in){
        return switch (in){
            case "day", "is_day" -> World::isDay;
            case "raining", "is_raining" -> World::isRaining;
            case "thundering", "is_thundering" -> World::isThundering;
            default -> null;
        };
    }
    public Predicate<World> getLP(String in, String val){
        return switch (in){
            case "dimension" -> {
                final Identifier arg = new Identifier(val);
                yield world -> world.getRegistryKey().getValue().equals(arg);
            }
            default -> null;
        };
    }

    //==================================================================================================================

    @Override
    public Predicate<World> getPredicate(String in, String val, Set<Class<?>> dejavu){
        {
            final Predicate<World> out = getLP(in, val);
            if (out != null) return out;
        }
        if (dejavu.add(Default.DIMENSION_TYPE.getClass())){
            final Predicate<DimensionType> out = Default.DIMENSION_TYPE.getPredicate(in, val, dejavu);
            if (out !=null) return world -> out.test(world.getDimension());
        }
        return null;
    }
    @Override
    public Predicate<World> getPredicate(String in, Set<Class<?>> dejavu){
        {
            final Predicate<World> out = getLP(in);
            if (out != null) return out;
        }
        if (dejavu.add(Default.DIMENSION_TYPE.getClass())){
            final Predicate<DimensionType> out = Default.DIMENSION_TYPE.getPredicate(in, dejavu);
            if (out !=null) return world -> out.test(world.getDimension());
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
        help.put("dimension:DimensionID","Require being in dimension");
        help.put("thundering is_thundering","Require thunder");
        help.put("raining is_raining","Require rain");
        help.put("day is_day","Require daytime");

        extend_help.add(new DimensionTypeScript());
    }
}
