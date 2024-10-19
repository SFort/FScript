package tf.ssf.sfort.script.instance;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;
import tf.ssf.sfort.script.util.DefaultParsers;

import java.util.function.Predicate;

public class ItemStackScript extends AbstractExtendablePredicateProvider<ItemStack> {

	public ItemStackScript() {
		help.put("item .:ItemID", "Has to be the specified item");
		help.put("enchant:EnchantID","Item has to have specified enchantment");
		help.put("~enchant:ENCHANTMENT_LEVEL_ENTRY","Execute script on all enchantments");
		help.put("~enchant~EnchantID:ENCHANTMENT_LEVEL_ENTRY","Execute script on a specific enchantment");
		help.put("rarity:RarityID", "Item has specified rarity");
		help.put("damageable is_damageable","Require Item to be damageable");
		help.put("empty","Require there to be no item");
		help.put("damaged","Require Item to be damaged");
		help.put("stackable","Require Item to be stackable");
		help.put("enchantable","Require Item to be enchantbale");
		help.put("has_glint","Require Item to have a glint");
		help.put("has_nbt","Require item to have nbt data stored");
		help.put("has_enchants","Require item to have enchantments");
		help.put("in_frame","Require item to be in a item frame");
		help.put("~nbt~String:NBT_ELEMENT", "Has to have matching nbt");
		help.put("rarity:EnchantRarityID","Require enchantment to be this rarity");
		help.put("food is_food","Item is food");
		help.put("fireproof is_fireproof","Item is fireproof");
	}

	@Override
	public Predicate<ItemStack> getLocalPredicate(String in, String val){
		switch (in){
			case ".": case "item" : {
				final Item arg = Registries.ITEM.get(Identifier.of(val));
				return item -> item.isOf(arg);
			}
			case "enchant" : {
				final Identifier id = Identifier.of(val);
				return item -> {
					for (RegistryEntry<Enchantment> entry : item.getEnchantments().getEnchantments()) {
						if (entry.matchesId(id)) return true;
					}
					return false;
				};
			}
			case "count" : {
				final int arg = Integer.parseInt(val);
				return item -> item.getCount()>=arg;
			}
			case "rarity" :{
				Rarity arg = Rarity.valueOf(val);
				return item -> item.getRarity().equals(arg);
			}
			default : return null;
		}
	}
	@Override
	public Predicate<ItemStack> getLocalPredicate(String in){
		switch (in){
			case "damageable": case "is_damageable" : return ItemStack::isDamageable;
			case "empty" : return ItemStack::isEmpty;
			case "damaged" : return ItemStack::isDamaged;
			case "stackable" : return ItemStack::isStackable;
			case "enchantable" : return ItemStack::isEnchantable;
			case "has_glint" : return ItemStack::hasGlint;
			case "has_nbt" : return i -> i.contains(DataComponentTypes.BLOCK_ENTITY_DATA);
			case "food": case "is_food" : return i -> i.contains(DataComponentTypes.FOOD);
			//Whoever choose to name this "fire resistant" annoys me off this isn't real life, it's a game things can be invulnerable
			case "fireproof": case "is_fireproof" : return i -> i.contains(DataComponentTypes.FIRE_RESISTANT);
			case "has_enchants" : return ItemStack::hasEnchantments;
			case "in_frame" : return ItemStack::isInFrame;
			default : return null;
		}
	}
	//TODO allow embedding . item since rarity behaves diffrently
	@Override
	public Predicate<ItemStack> getLocalEmbed(String in, String script){
		switch (in) {
			case "enchant" : {
				final Predicate<Object2IntMap.Entry<RegistryEntry<Enchantment>>> predicate = DefaultParsers.ENCHANTMENT_PARSER.parse(script);
				if (predicate == null) return null;
				return item -> {
					ItemEnchantmentsComponent component = item.getEnchantments();
					for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : component.getEnchantmentEntries()) {
						if (predicate.test(entry)) return true;
					}
					return false;
				};
			}
			default : return null;
		}
	}
	@Override
	public Predicate<ItemStack> getLocalEmbed(String in, String val, String script){
		switch (in) {
			case "enchant" :{
				final Predicate<Object2IntMap.Entry<RegistryEntry<Enchantment>>> predicate = DefaultParsers.ENCHANTMENT_PARSER.parse(script);
				if (predicate == null) return null;
				final Identifier id = Identifier.of(val);
				return item -> {
					ItemEnchantmentsComponent component = item.getEnchantments();
					for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : component.getEnchantmentEntries()) {
						if (entry.getKey().matchesId(id)) {
							return predicate.test(entry);
						}
					}
					return false;
				};
			}
			case "nbt": {
				final Predicate<NbtElement> predicate = DefaultParsers.NBT_ELEMENT_PARSER.parse(script);
				if (predicate == null) return null;
				return entity -> {
					NbtCompound nbtc = entity.get(DataComponentTypes.BLOCK_ENTITY_DATA).getNbt();
					if (nbtc == null || nbtc.isEmpty()) return false;
					NbtElement nbt = nbtc.get(val);
					if (nbt == null) return false;
					return predicate.test(nbt);
				};
			}
			default : return null;
		}
	}

}
