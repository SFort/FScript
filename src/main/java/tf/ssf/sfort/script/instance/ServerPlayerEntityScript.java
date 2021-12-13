package tf.ssf.sfort.script.instance;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import tf.ssf.sfort.script.Default;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.PredicateProviderExtendable;

import java.util.*;
import java.util.function.Predicate;

public class ServerPlayerEntityScript<T extends ServerPlayerEntity> implements PredicateProviderExtendable<T>, Help {

    public PlayerEntityScript<T> PLAYER_ENTITY = new PlayerEntityScript<>();

    public Predicate<T> getLP(String in, String val){
        return switch (in){
            case "respawn_distance" ->{
                final double arg = Double.parseDouble(val);
                yield player -> {
                    final BlockPos pos = player.getSpawnPointPosition();
                    final ServerWorld world = player.getWorld();
                    final RegistryKey<World> dim = player.getSpawnPointDimension();
                    if (pos == null || world == null) return false;
                    return dim.equals(world.getRegistryKey()) && pos.isWithinDistance(player.getPos(), arg);
                };
            }
            case "advancement" -> {
                final Identifier arg = new Identifier(val);
                yield player -> {
                    final MinecraftServer server = player.getServer();
                    if (server == null) return false;
                    return player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(arg)).isDone();
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
        if (dejavu.add(PLAYER_ENTITY.getClass())){
            final Predicate<T> out = PLAYER_ENTITY.getPredicate(in, val, dejavu);
            if (out !=null) return out;
        }
        if (dejavu.add(GameModeScript.class)){
            final Predicate<GameMode> out = Default.GAME_MODE.getPredicate(in, val, dejavu);
            if (out !=null) return player -> out.test(player.interactionManager.getGameMode());
        }
        return PredicateProviderExtendable.super.getPredicate(in, val, dejavu);
    }

    @Override
    public Predicate<T> getPredicate(String in, Set<Class<?>> dejavu){
        if (dejavu.add(PlayerEntityScript.class)){
            final Predicate<T> out = PLAYER_ENTITY.getPredicate(in, dejavu);
            if (out !=null) return out;
        }
        if (dejavu.add(GameModeScript.class)){
            final Predicate<GameMode> out = Default.GAME_MODE.getPredicate(in, dejavu);
            if (out !=null) return player -> out.test(player.interactionManager.getGameMode());
        }
        return PredicateProviderExtendable.super.getPredicate(in, dejavu);
    }

    @Override
    public Predicate<T> getEmbed(String in, String script, Set<Class<?>> dejavu){
        if (dejavu.add(PLAYER_ENTITY.getClass())){
            final Predicate<T> out = PLAYER_ENTITY.getEmbed(in, script, dejavu);
            if (out !=null) return out;
        }
        return PredicateProviderExtendable.super.getEmbed(in, script, dejavu);
    }

    @Override
    public Predicate<T> getEmbed(String in, String val, String script, Set<Class<?>> dejavu){
        if (dejavu.add(PLAYER_ENTITY.getClass())){
            final Predicate<T> out = PLAYER_ENTITY.getEmbed(in, val, script, dejavu);
            if (out !=null) return out;
        }
        return PredicateProviderExtendable.super.getEmbed(in, val, script, dejavu);
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
    public ServerPlayerEntityScript() {
        help.put("advancement:AdvancementID","Require advancement unlocked");
        help.put("respawn_distance:double","Require player to be nearby their respawn (usually a bed)");

        extend_help.add(new PlayerEntityScript<ServerPlayerEntity>());
        extend_help.add(Default.GAME_MODE);
    }
    //==================================================================================================================

    public final TreeSet<Pair<Integer, PredicateProvider<T>>> EXTEND = new TreeSet<>(Comparator.comparingInt(Pair::getLeft));

    @Override
    public void addProvider(PredicateProvider<T> predicateProvider, int priority) {
        if (predicateProvider instanceof Help) extend_help.add((Help) predicateProvider);
        EXTEND.add(new Pair<>(priority, predicateProvider));
    }

    @Override
    public List<PredicateProvider<T>> getProviders() {
        return EXTEND.stream().map(Pair::getRight).toList();
    }
}
