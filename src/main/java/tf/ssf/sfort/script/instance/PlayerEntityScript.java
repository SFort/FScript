package tf.ssf.sfort.script.instance;

import net.minecraft.entity.player.PlayerEntity;
import tf.ssf.sfort.script.Default;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlayerEntityScript<T extends PlayerEntity> implements PredicateProvider<T>, Help {
    private final LivingEntityScript<T> LIVING_ENTITY = new LivingEntityScript<>();
    public Predicate<T> getLP(String in, String val){
        return switch (in){
            case "level" -> {
                int arg = Integer.parseInt(val);
                yield player -> player.experienceLevel>=arg;
            }
            case "food" -> {
                float arg = Float.parseFloat(val);
                yield player -> player.getHungerManager().getFoodLevel()>=arg;
            }
            default -> null;
        };
    }
    public Predicate<T> getLP(String in){
        return null;
    }
    @Override
    public Predicate<T> getPredicate(String in, String val, Set<Class<?>> dejavu){
        {
            Predicate<T> out = getLP(in, val);
            if (out != null) return out;
        }
        if (dejavu.add(LivingEntityScript.class)){
            Predicate<T> out = LIVING_ENTITY.getPredicate(in, val, dejavu);
            if (out !=null) return out;
        }
        return null;
    }
    @Override
    public Predicate<T> getPredicate(String in, Set<Class<?>> dejavu){
        {
            Predicate<T> out = getLP(in);
            if (out != null) return out;
        }
        if (dejavu.add(LivingEntityScript.class)){
            Predicate<T> out = LIVING_ENTITY.getPredicate(in, dejavu);
            if (out !=null) return out;
        }
        return null;
    }
    public static final Map<String, String> help = new HashMap<>();
    static {
        help.put("level:int","Minimum required player level");
        help.put("food:float","Minimum required food");
    }
    @Override
    public Map<String, String> getHelp(){
        return help;
    }
    @Override
    public Map<String, String> getAllHelp(Set<Class<?>> dejavu){
        Stream<Map.Entry<String, String>> out = new HashMap<String, String>().entrySet().stream();
        if (dejavu.add(LivingEntityScript.class)) out = Stream.concat(out, Default.LIVING_ENTITY.getAllHelp(dejavu).entrySet().stream());
        out = Stream.concat(out, getHelp().entrySet().stream());

        return out.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
