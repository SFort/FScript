package tf.ssf.sfort.script.instance;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;

import java.util.function.Predicate;

public class ItemScript extends AbstractExtendablePredicateProvider<Item> {

	public ItemScript() {
		help.put("damageable is_damageable","Item has to be damageable");
		help.put("food is_food","Item is food");
		help.put("fireproof is_fireproof","Item is fireproof");
		help.put("block_item is_block_item","Item is a block");
		help.put("item .:ItemID", "Has to be the specified item");
	}
	@Override
	public Predicate<Item> getLocalPredicate(String in, String val){
		switch (in){
			case ".": case "item" : {
				final Item arg = Registries.ITEM.get(new Identifier(val));
				return item -> item == arg;
			}
			default : return null;
		}
	}
	@Override
	public Predicate<Item> getLocalPredicate(String in){
		switch (in){
			case "damageable": case "is_damageable" : return Item::isDamageable;
			case "food": case "is_food" : return Item::isFood;
			case "block_item": case "is_block_item" : return item -> item instanceof BlockItem;
			case "fireproof": case "is_fireproof" : return Item::isFireproof;
			default: return null;
		}
	}

}
