package tf.ssf.sfort.script.instance;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.mixin_extended.Config;
import tf.ssf.sfort.script.mixin_extended.ItemExtended;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class ItemScript implements PredicateProvider<Item>, Help {
	public Predicate<Item> getLP(String in, String val){
		return switch (in){
			case ".", "item" -> {
				final Item arg = Registry.ITEM.get(new Identifier(val));
				yield item -> item == arg;
			}
			case "group" -> item -> {
				final ItemGroup group = item.getGroup();
				return group != null && group.getName().equals(val);
			};
			default -> null;
		};
	}

	public Predicate<Item> getLP(String in){
		return switch (in){
			case "damageable", "is_damageable" -> Item::isDamageable;
			case "food", "is_food" -> Item::isFood;
			case "block_item", "is_block_item" -> item -> item instanceof BlockItem;
			case "fireproof", "is_fireproof" -> Item::isFireproof;
			default -> null;
		};
	}

	public Predicate<ItemExtended> getEP(String in, String val){
		return switch (in){
			case "rarity" ->{
				Rarity arg = Rarity.valueOf(val);
				yield item -> item.fscript$rarity().equals(arg);
			}
			default -> null;
		};
	}

	//==================================================================================================================

	@Override
	public Predicate<Item> getPredicate(String in, String val, Set<Class<?>> dejavu){
		{
			final Predicate<Item> out = getLP(in, val);
			if (out != null) return out;
		}
		if (Config.extended){
			final Predicate<ItemExtended> out = getEP(in, val);
			if (out != null) return item -> out.test((ItemExtended) item);
		}
		return null;
	}

	@Override
	public Predicate<Item> getPredicate(String in, Set<Class<?>> dejavu){
		return getLP(in);
	}


	//==================================================================================================================

	public static final Map<String, Object> help = new HashMap<>();
	static {
		help.put("damageable is_damageable","Item has to be damageable");
		help.put("food is_food","Item is food");
		help.put("fireproof is_fireproof","Item is fireproof");
		help.put("block_item is_block_item","Item is a block");
		if (Config.extended) help.put("rarity:RarityID", "Item has specified rarity");
		help.put("item .:ItemID", "Has to be the specified item");
		help.put("group:ItemGroupID", "Item has specified item group");
	}
	@Override
	public Map<String, Object> getHelp(){
		return help;
	}

}
