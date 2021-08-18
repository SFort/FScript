package tf.ssf.sfort.script.instance;

import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import tf.ssf.sfort.script.Default;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WorldScript implements PredicateProvider<World>, Help {
    public Predicate<World> getLP(String in){
        return switch (in){
            case "is_day" -> World::isDay;
            case "is_raining" -> World::isRaining;
            case "is_thundering" -> World::isThundering;
            default -> null;
        };
    }
    public Predicate<World> getLP(String in, String val){
        return switch (in){
            case "dimension" -> {
                Identifier arg = new Identifier(val);
                yield world -> world.getRegistryKey().getValue().equals(arg);
            }
            default -> null;
        };
    }
    @Override
    public Predicate<World> getPredicate(String in, String val, Set<Class<?>> dejavu){
        {
            Predicate<World> out = getLP(in, val);
            if (out != null) return out;
        }
        if (dejavu.add(Default.DIMENSION_TYPE.getClass())){
            Predicate<DimensionType> out = Default.DIMENSION_TYPE.getPredicate(in, val, dejavu);
            if (out !=null) return world -> out.test(world.getDimension());
        }
        return null;
    }
    @Override
    public Predicate<World> getPredicate(String in, Set<Class<?>> dejavu){
        {
            Predicate<World> out = getLP(in);
            if (out != null) return out;
        }
        if (dejavu.add(Default.DIMENSION_TYPE.getClass())){
            Predicate<DimensionType> out = Default.DIMENSION_TYPE.getPredicate(in, dejavu);
            if (out !=null) return world -> out.test(world.getDimension());
        }
        return null;
    }
    public static final Map<String, String> help = new HashMap<>();
    static {
        help.put("dimension:DimensionID","Require being in dimension overworld|the_nether|the_end");
        help.put("is_thundering","Require thunder");
        help.put("is_raining","Require rain");
        help.put("is_day","Require daytime");
    }
    @Override
    public Map<String, String> getHelp(){
        return help;
    }
    @Override
    public Map<String, String> getAllHelp(Set<Class<?>> dejavu){
        Stream<Map.Entry<String, String>> out = new HashMap<String, String>().entrySet().stream();
        if (dejavu.add(DimensionTypeScript.class)) out = Stream.concat(out, Default.DIMENSION_TYPE.getAllHelp(dejavu).entrySet().stream());
        out = Stream.concat(out, getAllHelp().entrySet().stream());

        return out.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
