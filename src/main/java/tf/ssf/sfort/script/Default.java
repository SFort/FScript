package tf.ssf.sfort.script;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import tf.ssf.sfort.script.instance.*;

import java.util.HashMap;
import java.util.Map;

public class Default {
    public static EntityScript<Entity> ENTITY = new EntityScript<>();
    public static LivingEntityScript<LivingEntity> LIVING_ENTITY = new LivingEntityScript<>();
    public static PlayerEntityScript<PlayerEntity> PLAYER_ENTITY = new PlayerEntityScript<>();
    public static ServerPlayerEntityScript<ServerPlayerEntity> SERVER_PLAYER_ENTITY = new ServerPlayerEntityScript<>();
    public static DimensionTypeScript DIMENSION_TYPE = new DimensionTypeScript();
    public static ChunkScript CHUNK = new ChunkScript();
    public static WorldScript WORLD = new WorldScript();
    public static BiomeScript BIOME = new BiomeScript();
    public static ItemScript ITEM = new ItemScript();
    public static PlayerInventoryScript PLAYER_INVENTORY = new PlayerInventoryScript();
    public static ItemStackScript ITEM_STACK = new ItemStackScript();
    public static EnchantmentScript ENCHANTMENT = new EnchantmentScript();
    public static EnchantmentLevelEntryScript ENCHANTMENT_LEVEL_ENTRY = new EnchantmentLevelEntryScript();
    public static GameModeScript GAME_MODE = new GameModeScript();
    public static FishingBobberEntityScript FISHING_BOBBER_ENTITY = new FishingBobberEntityScript();

    protected static final Map<String, PredicateProvider<?>> defaults = new HashMap<>();
    public static Map<String, PredicateProvider<?>> getDefaultMap(){
        return defaults;
    }
    static {
        defaults.put("ENTITY", Default.ENTITY);
        defaults.put("LIVING_ENTITY", Default.LIVING_ENTITY);
        defaults.put("PLAYER_ENTITY", Default.PLAYER_ENTITY);
        defaults.put("SERVER_PLAYER_ENTITY", Default.SERVER_PLAYER_ENTITY);
        defaults.put("DIMENSION_TYPE", Default.DIMENSION_TYPE);
        defaults.put("CHUNK", Default.CHUNK);
        defaults.put("WORLD", Default.WORLD);
        defaults.put("BIOME", Default.BIOME);
        defaults.put("ITEM", Default.ITEM);
        defaults.put("PLAYER_INVENTORY", Default.PLAYER_INVENTORY);
        defaults.put("ITEM_STACK", Default.ITEM_STACK);
        defaults.put("ENCHANTMENT", Default.ENCHANTMENT);
        defaults.put("ENCHANTMENT_LEVEL_ENTRY", Default.ENCHANTMENT_LEVEL_ENTRY);
        defaults.put("GAME_MODE", Default.GAME_MODE);
        defaults.put("FISHING_BOBBER_ENTITY", Default.FISHING_BOBBER_ENTITY);
    }
}
