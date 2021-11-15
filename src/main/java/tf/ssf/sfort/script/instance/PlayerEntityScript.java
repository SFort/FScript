package tf.ssf.sfort.script.instance;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import tf.ssf.sfort.script.Default;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;

import java.util.*;
import java.util.function.Predicate;

public class PlayerEntityScript<T extends PlayerEntity> implements PredicateProvider<T>, Help {
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
    public Predicate<T> getLE(String in, String script){
        return switch (in) {
            case "inventory" -> {
                final Predicate<PlayerInventory> predicate = Default.PLAYER_INVENTORY_PARSER.parse(script);
                if (predicate == null) yield null;
                yield player -> predicate.test(player.getInventory());
            }
            case "server_player" -> {
                final Predicate<ServerPlayerEntity> predicate = Default.SERVER_PLAYER_ENTITY_PARSER.parse(script);
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
    //==================================================================================================================

    @Override
    public Predicate<T> getPredicate(String in, String val, Set<Class<?>> dejavu){
        {
            final Predicate<T> out = getLP(in, val);
            if (out != null) return out;
        }
        if (dejavu.add(LIVING_ENTITY.getClass())){
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
        if (dejavu.add(LIVING_ENTITY.getClass())){
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
        {
            final Predicate<T> out = getLE(in, script);
            if (out !=null) return out;
        }
        if (dejavu.add(LIVING_ENTITY.getClass())){
            final Predicate<T> out = LIVING_ENTITY.getEmbed(in, script, dejavu);
            if (out !=null) return out;
        }
        return null;
    }
    @Override
    public Predicate<T> getEmbed(String in, String val, String script, Set<Class<?>> dejavu){
        if (dejavu.add(LIVING_ENTITY.getClass())){
            final Predicate<T> out = LIVING_ENTITY.getEmbed(in, val, script, dejavu);
            if (out !=null) return out;
        }
        return null;
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
    public static final Map<String, String> help = new HashMap<String, String>();
    public static final List<Help> extend_help = new ArrayList<>();
    static {
        help.put("level:int","Minimum required player level");
        help.put("food:float","Minimum required food");
        help.put("~inventory:PLAYER_INVENTORY", "Require matching inventory");
        help.put("~server_player:SERVER_PLAYER_ENTITY", "Require a server player entity");

        extend_help.add(new LivingEntityScript<PlayerEntity>());
        extend_help.add(Default.FISHING_BOBBER_ENTITY);
    }
}
