package tf.ssf.sfort.script.instance;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.ScriptParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlayerEntityScript<T extends PlayerEntity> implements PredicateProvider<T>, Help {
    public ScriptParser<ItemStack> ITEM_STACK_PARSER = new ScriptParser<>(new ItemStackScript());
    public LivingEntityScript<T> LIVING_ENTITY = new LivingEntityScript<>();
    public Predicate<T> getLP(String in, String val){
        return switch (in){
            case "level" -> {
                final int arg = Integer.parseInt(val);
                yield player -> player.experienceLevel>=arg;
            }
            case "food" -> {
                final float arg = Float.parseFloat(val);
                yield player -> player.getHungerManager().getFoodLevel()>=arg;
            }
            default -> null;
        };
    }
    //TODO
    public Predicate<T> getLE(String in, String script){
        return null;
    }
    public Predicate<T> getLE(String in, String val, String script){
        return null;
    }
    //==================================================================================================================

    @Override
    public Predicate<T> getPredicate(String in, String val, Set<Class<?>> dejavu){
        {
            final Predicate<T> out = getLP(in, val);
            if (out != null) return out;
        }
        if (dejavu.add(LivingEntityScript.class)){
            final Predicate<T> out = LIVING_ENTITY.getPredicate(in, val, dejavu);
            if (out !=null) return out;
        }
        return null;
    }
    @Override
    public Predicate<T> getPredicate(String in, Set<Class<?>> dejavu){
        if (dejavu.add(LivingEntityScript.class)){
            final Predicate<T> out = LIVING_ENTITY.getPredicate(in, dejavu);
            if (out !=null) return out;
        }
        return null;
    }
    @Override
    public Predicate<T> getEmbed(String in, String script){
        {
            final Predicate<T> out = getLE(in, script);
            if (out != null) return out;
        }
        {
            final Predicate<T> out = LIVING_ENTITY.getEmbed(in, script);
            if (out !=null) return out;
        }
        return null;
    }
    @Override
    public Predicate<T> getEmbed(String in, String val, String script){
        return getLE(in, val, script);
    }

    //==================================================================================================================

    //TODO inv help
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
        if (dejavu.add(LivingEntityScript.class)) out = Stream.concat(out, LIVING_ENTITY.getAllHelp(dejavu).entrySet().stream());
        out = Stream.concat(out, getHelp().entrySet().stream());

        return out.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
