package tf.ssf.sfort.script.instance;

import net.minecraft.entity.player.PlayerEntity;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;

import java.util.Set;
import java.util.function.Predicate;

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
        if (dejavu.add(LIVING_ENTITY.getClass())){
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
        if (dejavu.add(LIVING_ENTITY.getClass())){
            Predicate<T> out = LIVING_ENTITY.getPredicate(in, dejavu);
            if (out !=null) return out;
        }
        return null;
    }
    @Override
    public String getHelp(){
        return
                String.format("\t%-20s%-70s%s%n","level","- Minimum required player level","int")+
                String.format("\t%-20s%-70s%s%n","food","- Minimum required food","float")
                ;
    }
    @Override
    public String getAllHelp(Set<Class<?>> dejavu){
        return (dejavu.add(LIVING_ENTITY.getClass())?LIVING_ENTITY.getAllHelp(dejavu):"")+getHelp();
    }
}
