package tf.ssf.sfort.script.instance;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import tf.ssf.sfort.script.instance.util.AbstractExtendablePredicateProvider;

import java.util.function.Predicate;

public class ItemScript extends AbstractExtendablePredicateProvider<Item> {

	public ItemScript() {
		help.put("damageable is_damageable","Item has to be damageable");
		help.put("food is_food","Item is food");
		help.put("fireproof is_fireproof","Item is fireproof");
		help.put("block_item is_block_item","Item is a block");
		help.put("item .:ItemID", "Has to be the specified item");
		help.put("group:ItemGroupID", "Item has specified item group");
	}
	@Override
	public Predicate<Item> getLocalPredicate(String in, String val){
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
	@Override
	public Predicate<Item> getLocalPredicate(String in){
		return switch (in){
			case "damageable", "is_damageable" -> Item::isDamageable;
			case "food", "is_food" -> Item::isFood;
			case "block_item", "is_block_item" -> item -> item instanceof BlockItem;
			case "fireproof", "is_fireproof" -> Item::isFireproof;
			default -> null;
		};
	}

}
