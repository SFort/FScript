package tf.ssf.sfort.script.instance;

import net.minecraft.world.GameMode;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class GameModeScript implements PredicateProvider<GameMode>, Help {
    public Predicate<GameMode> getLP(String in){
        return switch (in){
            case "is_block_breaking_restricted" -> GameMode::isBlockBreakingRestricted;
            case "is_creative" -> GameMode::isCreative;
            case "is_survival_like" -> GameMode::isSurvivalLike;
            default -> null;
        };
    }
    public Predicate<GameMode> getLP(String in, String val){
        return switch (in){
            case "." -> mode -> mode.name().equals(val);
            case "name" -> mode -> mode.getName().equals(val);
            case "id" ->{
                final int arg = Integer.parseInt(val);
                yield mode -> mode.getId() == arg;
            }
            default -> null;
        };
    }

    //==================================================================================================================

    @Override
    public Predicate<GameMode> getPredicate(String in, Set<Class<?>> dejavu){
        return getLP(in);
    }

    @Override
    public Predicate<GameMode> getPredicate(String in, String val, Set<Class<?>> dejavu){
        return getLP(in, val);
    }


    //==================================================================================================================

    public static final Map<String, String> help = new HashMap<>();
    static {
        help.put("is_block_breaking_restricted","Require player gamemode to prevent breaking blocks");
        help.put("is_creative","Require player gamemode to be creative");
        help.put("is_survival_like","Require player gamemode to be survival or adventure");
    }
    @Override
    public Map<String, String> getHelp(){
        return help;
    }
    @Override
    public Map<String, String> getAllHelp(Set<Class<?>> dejavu){
        return getHelp();
    }
}
