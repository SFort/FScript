package tf.ssf.sfort.script.instance;

import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import tf.ssf.sfort.script.instance.util.AbstractExtendablePredicateProvider;

import java.util.function.Predicate;

public class WorldScript extends AbstractExtendablePredicateProvider<World> {

    public WorldScript() {
        help.put("dimension:DimensionID","Require being in dimension");
        help.put("thundering is_thundering","Require thunder");
        help.put("raining is_raining","Require rain");
        help.put("day is_day","Require daytime");
    }

    @Override
    public Predicate<World> getLocalPredicate(String in){
        return switch (in){
            case "day", "is_day" -> World::isDay;
            case "raining", "is_raining" -> World::isRaining;
            case "thundering", "is_thundering" -> World::isThundering;
            default -> null;
        };
    }

    @Override
    public Predicate<World> getLocalPredicate(String in, String val){
        return switch (in){
            case "dimension" -> {
                final Identifier arg = new Identifier(val);
                yield world -> world.getRegistryKey().getValue().equals(arg);
            }
            default -> null;
        };
    }

}
