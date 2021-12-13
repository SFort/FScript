package tf.ssf.sfort.script.extended.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.mixin.ServerPlayerEntityExtended;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class MixinExtendedServerPlayerEntityScript implements PredicateProvider<ServerPlayerEntity>, Help {
    @Override
    public Predicate<ServerPlayerEntity> getPredicate(String in, Set<Class<?>> dejavu){
        return switch (in){
            case "seen_credits" -> p -> ((ServerPlayerEntityExtended)p).fscript$seenCredits();
            default -> null;
        };
    }
    //==================================================================================================================

    @Override
    public Map<String, String> getHelp(){
        return help;
    }
    public final Map<String, String> help = new HashMap<>();
    public MixinExtendedServerPlayerEntityScript() {
        help.put("seen_credits", "Require player to have seen the end credits");
    }
}
