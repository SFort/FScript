package tf.ssf.sfort.script.util;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import tf.ssf.sfort.script.ScriptParser;

import java.util.Map;

import static tf.ssf.sfort.script.Default.*;

public class DefaultParsers {
    public static ScriptParser<LivingEntity> LIVING_ENTITY_PARSER = new ScriptParser<>(LIVING_ENTITY);
    public static ScriptParser<PlayerEntity> PLAYER_ENTITY_PARSER = new ScriptParser<>(PLAYER_ENTITY);
    public static ScriptParser<ServerPlayerEntity> SERVER_PLAYER_ENTITY_PARSER = new ScriptParser<>(SERVER_PLAYER_ENTITY);
    public static ScriptParser<ItemStack> ITEM_STACK_PARSER = new ScriptParser<>(ITEM_STACK);
    public static ScriptParser<Map.Entry<Enchantment, Integer>> ENCHANTMENT_PARSER = new ScriptParser<>(ENCHANTMENT_LEVEL_ENTRY);
    public static ScriptParser<Entity> ENTITY_PARSER = new ScriptParser<>(ENTITY);
    public static ScriptParser<PlayerInventory> PLAYER_INVENTORY_PARSER = new ScriptParser<>(PLAYER_INVENTORY);
    public static ScriptParser<EnderChestInventory> ENDERCHEST_INVENTORY_PARSER = new ScriptParser<>(ENDERCHEST_INVENTORY);
    public static ScriptParser<NbtElement> NBT_ELEMENT_PARSER = new ScriptParser<>(NBT_ELEMENT);
}
