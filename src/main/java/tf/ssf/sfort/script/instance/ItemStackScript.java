package tf.ssf.sfort.script.instance;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;
import tf.ssf.sfort.script.util.DefaultParsers;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
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
	}

	@Override
	public Predicate<ItemStack> getLocalPredicate(String in, String val){
		switch (in){
			case ".": case "item" : {
				final Item arg = Registry.ITEM.get(new Identifier(val));
				return item -> item.getItem() == arg;
			}
			case "enchant" : {
				final Enchantment arg = Registry.ENCHANTMENT.get(new Identifier(val));
				return item -> EnchantmentHelper.get(item).containsKey(arg);
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
			case "has_nbt" : return ItemStack::hasTag;
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
				final Predicate<Map.Entry<Enchantment, Integer>> predicate = DefaultParsers.ENCHANTMENT_PARSER.parse(script);
				if (predicate == null) return null;
				return item -> {
					boolean rez = false;
					final Iterator<Map.Entry<Enchantment, Integer>> i = EnchantmentHelper.get(item).entrySet().iterator();
					while(i.hasNext() && !rez)
						rez=predicate.test(i.next());
					return rez;
				};
			}
			default : return null;
		}
	}
	@Override
	public Predicate<ItemStack> getLocalEmbed(String in, String val, String script){
		switch (in) {
			case "enchant" :{
				final Predicate<Map.Entry<Enchantment, Integer>> predicate = DefaultParsers.ENCHANTMENT_PARSER.parse(script);
				if (predicate == null) return null;
				final Enchantment arg = Registry.ENCHANTMENT.get(new Identifier(val));
				if (arg == null) return null;
				return item -> {
					final Integer lvl = EnchantmentHelper.get(item).get(arg);
					return lvl != null && predicate.test(new AbstractMap.SimpleEntry<>(arg, lvl));
				};
			}
			default : return null;
		}
	}

}
