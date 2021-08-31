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
    public static ItemStackScript ITEM_STACK = new ItemStackScript();
    public static EnchantmentScript ENCHANTMENT = new EnchantmentScript();
    public static EnchantmentLevelEntryScript ENCHANTMENT_LEVEL_ENTRY = new EnchantmentLevelEntryScript();
    public static GameModeScript GAME_MODE = new GameModeScript();
    public static FishingBobberEntityScript FISHING_BOBBER_ENTITY = new FishingBobberEntityScript();
}
