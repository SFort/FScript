package tf.ssf.sfort.script;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.network.ServerPlayerEntity;
import tf.ssf.sfort.script.extended.mixin.MixinExtendedFishingBobberEntityScript;
import tf.ssf.sfort.script.extended.mixin.MixinExtendedItemScript;
import tf.ssf.sfort.script.extended.mixin.MixinExtendedLivingEntityScript;
import tf.ssf.sfort.script.extended.mixin.MixinExtendedServerPlayerEntityScript;
import tf.ssf.sfort.script.extended.trinkets.TrinketExtendedLivingEntityScript;
import tf.ssf.sfort.script.instance.*;
import tf.ssf.sfort.script.instance.InventoryScript;

import java.util.HashMap;
import java.util.Map;

public class Default {
    public static final Parameters PARAMETERS = new Parameters();

    public static final EntityScript<Entity> ENTITY = new EntityScript<>();
    public static final LivingEntityScript<LivingEntity> LIVING_ENTITY = new LivingEntityScript<>();
    public static final PlayerEntityScript<PlayerEntity> PLAYER_ENTITY = new PlayerEntityScript<>();
    public static final ServerPlayerEntityScript<ServerPlayerEntity> SERVER_PLAYER_ENTITY = new ServerPlayerEntityScript<>();
    public static final DimensionTypeScript DIMENSION_TYPE = new DimensionTypeScript();
    public static final ChunkScript CHUNK = new ChunkScript();
    public static final WorldScript WORLD = new WorldScript();
    public static final BiomeScript BIOME = new BiomeScript();
    public static final ItemScript ITEM = new ItemScript();
    public static final InventoryScript<Inventory> INVENTORY = new InventoryScript<>();
    public static final PlayerInventoryScript PLAYER_INVENTORY = new PlayerInventoryScript();
    public static final ItemStackScript ITEM_STACK = new ItemStackScript();
    public static final EnchantmentScript ENCHANTMENT = new EnchantmentScript();
    public static final EnchantmentLevelEntryScript ENCHANTMENT_LEVEL_ENTRY = new EnchantmentLevelEntryScript();
    public static final GameModeScript GAME_MODE = new GameModeScript();
    public static final FishingBobberEntityScript FISHING_BOBBER_ENTITY = new FishingBobberEntityScript();

    protected static final Map<String, PredicateProvider<?>> defaults = new HashMap<>();
    public static Map<String, PredicateProvider<?>> getDefaultMap(){
        return defaults;
    }
    static {
        ENCHANTMENT_LEVEL_ENTRY.addProvider(ENCHANTMENT, enchant -> set -> enchant.test(set.getKey()), 3000);
        ENTITY.addProvider(WORLD, world -> entity -> world.test(entity.world), 3002);
        ENTITY.addProvider(BIOME, biom -> entity -> biom.test(entity.world.getBiome(entity.getBlockPos())), 3001);
        ENTITY.addProvider(CHUNK, chunk -> entity -> chunk.test(entity.world.getWorldChunk(entity.getBlockPos())), 3000);
        FISHING_BOBBER_ENTITY.addProvider(ENTITY, entity -> entity::test, 3000);
        ITEM_STACK.addProvider(ITEM, item -> stack -> item.test(stack.getItem()), 3000);
        LIVING_ENTITY.addProvider(ENTITY, entity -> entity::test, 3000);
        PLAYER_ENTITY.addProvider(LIVING_ENTITY, entity -> entity::test, 3001);
        PLAYER_ENTITY.addProvider(FISHING_BOBBER_ENTITY, fis -> player -> fis.test(player.fishHook), 3000);
        SERVER_PLAYER_ENTITY.addProvider(PLAYER_ENTITY, entity -> entity::test, 3001);
        SERVER_PLAYER_ENTITY.addProvider(GAME_MODE, mode -> player -> mode.test(player.interactionManager.getGameMode()), 3000);
        WORLD.addProvider(DIMENSION_TYPE, dim -> world -> dim.test(world.getDimension()), 3000);

        //Mixin
        SERVER_PLAYER_ENTITY.addProvider(new MixinExtendedServerPlayerEntityScript(), 1000);
        LIVING_ENTITY.addProvider(new MixinExtendedLivingEntityScript(), 1000);
        ITEM.addProvider(new MixinExtendedItemScript(), 1000);
        FISHING_BOBBER_ENTITY.addProvider(new MixinExtendedFishingBobberEntityScript(), 1000);

        //Mod Compat
        if (FabricLoader.getInstance().isModLoaded("trinkets") && TrinketExtendedLivingEntityScript.success)
            LIVING_ENTITY.addProvider(new TrinketExtendedLivingEntityScript());

        defaults.put("ENTITY", ENTITY);
        defaults.put("LIVING_ENTITY", LIVING_ENTITY);
        defaults.put("PLAYER_ENTITY", PLAYER_ENTITY);
        defaults.put("SERVER_PLAYER_ENTITY", SERVER_PLAYER_ENTITY);
        defaults.put("DIMENSION_TYPE", DIMENSION_TYPE);
        defaults.put("CHUNK", CHUNK);
        defaults.put("WORLD", WORLD);
        defaults.put("BIOME", BIOME);
        defaults.put("ITEM", ITEM);
        defaults.put("INVENTORY", INVENTORY);
        defaults.put("PLAYER_INVENTORY", PLAYER_INVENTORY);
        defaults.put("ITEM_STACK", ITEM_STACK);
        defaults.put("ENCHANTMENT", ENCHANTMENT);
        defaults.put("ENCHANTMENT_LEVEL_ENTRY", ENCHANTMENT_LEVEL_ENTRY);
        defaults.put("GAME_MODE", GAME_MODE);
        defaults.put("FISHING_BOBBER_ENTITY", FISHING_BOBBER_ENTITY);

        FabricLoader.getInstance().getEntrypoints("fscript", ModInitializer.class).forEach(ModInitializer::onInitialize);
    }
}
