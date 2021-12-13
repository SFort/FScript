package tf.ssf.sfort.script.instance;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import tf.ssf.sfort.script.instance.support.AbstractExtendablePredicateProvider;
import tf.ssf.sfort.script.instance.support.DefaultParsers;

import java.util.function.Predicate;

public class PlayerEntityScript<T extends PlayerEntity> extends AbstractExtendablePredicateProvider<T> {

    public PlayerEntityScript() {
        help.put("level:int","Minimum required player level");
        help.put("food:float","Minimum required food");
        help.put("~inventory:PLAYER_INVENTORY", "Require matching inventory");
        help.put("~server_player:SERVER_PLAYER_ENTITY", "Require a server player entity");
    }

    @Override
    public Predicate<T> getLocalPredicate(String in, String val){
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
    @Override
    public Predicate<T> getLocalEmbed(String in, String script){
        return switch (in) {
            case "inventory" -> {
                final Predicate<PlayerInventory> predicate = DefaultParsers.PLAYER_INVENTORY_PARSER.parse(script);
                if (predicate == null) yield null;
                yield player -> predicate.test(player.getInventory());
            }
            case "server_player" -> {
                final Predicate<ServerPlayerEntity> predicate = DefaultParsers.SERVER_PLAYER_ENTITY_PARSER.parse(script);
                if (predicate == null) yield null;
                yield entity -> {
                    if (entity instanceof ServerPlayerEntity)
                        return predicate.test(((ServerPlayerEntity) entity));
                    return false;
                };
            }
            default -> null;
        };
    }

}
