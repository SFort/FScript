package tf.ssf.sfort.script.instance;

import net.minecraft.world.GameMode;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class GameModeScript implements PredicateProvider<GameMode>, Help {
    public Predicate<GameMode> getLP(String in){
        return switch (in){
            case "block_breaking_restricted", "is_block_breaking_restricted" -> GameMode::isBlockBreakingRestricted;
            case "creative", "is_creative" -> GameMode::isCreative;
            case "survival_like", "is_survival_like" -> GameMode::isSurvivalLike;
            default -> null;
        };
    }
    public Predicate<GameMode> getLP(String in, String val){
        return switch (in){
            case ".", "game_mode_any", "game_mode","game_mode_name", "game_mode_id", "name", "id"  ->{
                final GameMode arg = switch (in) {
                    case "game_mode"-> {
                        try {
                            yield GameMode.valueOf(val);
                        } catch (Exception ignore) { }
                        yield null;
                    }
                    case "name"->GameMode.byName(val);
                    case "id"->GameMode.byId(Integer.parseInt(val));
                    default->Arrays.stream(GameMode.values()).filter(g -> g.name().equals(val) || g.getName().equals(val) || g.getId() == Integer.getInteger(val)).findAny().orElse(null);
                };
                if (arg != null) yield mode -> mode.equals(arg);
                yield null;
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

    public static final Map<String, Object> help = new HashMap<>();
    static {
        help.put("block_breaking_restricted is_block_breaking_restricted","Require player gamemode to prevent breaking blocks");
        help.put("creative is_creative","Require player gamemode to be creative");
        help.put("survival_like is_survival_like","Require player gamemode to be survival or adventure");
        help.put("game_mode_any .:GameModeID GameModeNameID int","Require specified gamemode");
        help.put("game_mode:GameModeID","Require specified gamemode");
        help.put("game_mode_name name:GameModeNameID","Require specified gamemode");
        help.put("game_mode_id id:int","Require specified gamemode");

    }
    @Override
    public Map<String, Object> getHelp(){
        return help;
    }
}
