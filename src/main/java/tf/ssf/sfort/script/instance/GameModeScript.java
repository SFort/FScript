package tf.ssf.sfort.script.instance;

import net.minecraft.world.GameMode;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;

import java.util.Arrays;
import java.util.function.Predicate;

public class GameModeScript extends AbstractExtendablePredicateProvider<GameMode> {

    public GameModeScript() {
        help.put("block_breaking_restricted is_block_breaking_restricted","Require player gamemode to prevent breaking blocks");
        help.put("creative is_creative","Require player gamemode to be creative");
        help.put("survival_like is_survival_like","Require player gamemode to be survival or adventure");
        help.put("game_mode_any .:GameModeID GameModeNameID int","Require specified gamemode");
        help.put("game_mode:GameModeID","Require specified gamemode");
        help.put("game_mode_name name:GameModeNameID","Require specified gamemode");
        help.put("game_mode_id id:int","Require specified gamemode");
    }

    @Override
    public Predicate<GameMode> getLocalPredicate(String in){
        switch (in){
            case "block_breaking_restricted": case "is_block_breaking_restricted" : return GameMode::isBlockBreakingRestricted;
            case "creative": case "is_creative" : return GameMode::isCreative;
            case "survival_like": case "is_survival_like" : return GameMode::isSurvivalLike;
            default : return null;
        }
    }
    @Override
    public Predicate<GameMode> getLocalPredicate(String in, String val){
        switch (in){
            case ".": case "game_mode_any": case "game_mode": case "game_mode_name": case "game_mode_id": case "name": case "id" :{
                GameMode arg;
                switch (in) {
                    case "game_mode": {
                        try {
                            arg = GameMode.valueOf(val);
                        } catch (Exception ignore) {
                            arg = null;
                        }
                        break;
                    }
                    case "name":
                        arg = GameMode.byName(val);
                        break;
                    case "id":
                        arg = GameMode.byId(Integer.parseInt(val));
                        break;
                    default:
                        arg = Arrays.stream(GameMode.values()).filter(g -> g.name().equals(val) || g.getName().equals(val) || g.getId() == Integer.getInteger(val)).findAny().orElse(null);
                        break;
                };
                final GameMode farg = arg;
                if (arg != null) return mode -> mode.equals(farg);
                return null;
            }
            default: return null;
        }
    }
}
