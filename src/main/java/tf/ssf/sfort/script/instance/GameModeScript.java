package tf.ssf.sfort.script.instance;

import net.minecraft.util.Pair;
import net.minecraft.world.GameMode;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.PredicateProviderExtendable;

import java.util.*;
import java.util.function.Predicate;

public class GameModeScript implements PredicateProviderExtendable<GameMode>, Help {
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
        {
            final Predicate<GameMode> out = getLP(in);
            if (out != null) return out;
        }
        return PredicateProviderExtendable.super.getPredicate(in, dejavu);
    }

    @Override
    public Predicate<GameMode> getPredicate(String in, String val, Set<Class<?>> dejavu){
        {
            final Predicate<GameMode> out = getLP(in, val);
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
    public GameModeScript() {
        help.put("block_breaking_restricted is_block_breaking_restricted","Require player gamemode to prevent breaking blocks");
        help.put("creative is_creative","Require player gamemode to be creative");
        help.put("survival_like is_survival_like","Require player gamemode to be survival or adventure");
        help.put("game_mode_any .:GameModeID GameModeNameID int","Require specified gamemode");
        help.put("game_mode:GameModeID","Require specified gamemode");
        help.put("game_mode_name name:GameModeNameID","Require specified gamemode");
        help.put("game_mode_id id:int","Require specified gamemode");

    }

    //==================================================================================================================

    public final TreeSet<Pair<Integer, PredicateProvider<GameMode>>> EXTEND = new TreeSet<>(Comparator.comparingInt(Pair::getLeft));

    @Override
    public void addProvider(PredicateProvider<GameMode> predicateProvider, int priority) {
        if (predicateProvider instanceof Help) extend_help.add((Help) predicateProvider);
        EXTEND.add(new Pair<>(priority, predicateProvider));
    }

    @Override
    public List<PredicateProvider<GameMode>> getProviders() {
        return EXTEND.stream().map(Pair::getRight).toList();
    }

}
