package tf.ssf.sfort.script.instance;

import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;

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
        switch (in){
            case "day": case "is_day" : return World::isDay;
            case "raining": case "is_raining" : return World::isRaining;
            case "thundering": case "is_thundering" : return World::isThundering;
            default : return null;
        }
    }

    @Override
    public Predicate<World> getLocalPredicate(String in, String val){
        switch (in){
            case "dimension" : {
                final Identifier arg = new Identifier(val);
                return world -> world.getRegistryKey().getValue().equals(arg);
            }
            default : return null;
        }
    }

}
