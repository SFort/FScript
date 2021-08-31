package tf.ssf.sfort.script.instance;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import tf.ssf.sfort.script.Default;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.ScriptParser;

import java.util.*;
import java.util.function.Predicate;

public class PlayerEntityScript<T extends PlayerEntity> implements PredicateProvider<T>, Help {
    //TODO inventory slots
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
        if (dejavu.add(FishingBobberEntityScript.class)){
            final Predicate<FishingBobberEntity> out = Default.FISHING_BOBBER_ENTITY.getPredicate(in, val, dejavu);
            if (out !=null) return player -> out.test(player.fishHook);
        }
        return null;
    }
    @Override
    public Predicate<T> getPredicate(String in, Set<Class<?>> dejavu){
        if (dejavu.add(LivingEntityScript.class)){
            final Predicate<T> out = LIVING_ENTITY.getPredicate(in, dejavu);
            if (out !=null) return out;
        }
        if (dejavu.add(FishingBobberEntityScript.class)){
            final Predicate<FishingBobberEntity> out = Default.FISHING_BOBBER_ENTITY.getPredicate(in, dejavu);
            if (out !=null) return player -> out.test(player.fishHook);
        }
        return null;
    }
    @Override
    public Predicate<T> getEmbed(String in, String script, Set<Class<?>> dejavu){
        if (dejavu.add(LIVING_ENTITY.getClass())){
            final Predicate<T> out = LIVING_ENTITY.getEmbed(in, script);
            if (out !=null) return out;
        }
        return null;
    }

    //==================================================================================================================

    @Override
    public Map<String, Object> getHelp(){
        return help;
    }
    @Override
    public Set<Help> getImported(){
        return extend_help;
    }
    public static final Map<String, Object> help = new HashMap<>();
    public static final Set<Help> extend_help = new LinkedHashSet<>();
    static {
        help.put("level:int","Minimum required player level");
        help.put("food:float","Minimum required food");

        extend_help.add(Default.LIVING_ENTITY);
        extend_help.add(Default.FISHING_BOBBER_ENTITY);
    }
}
