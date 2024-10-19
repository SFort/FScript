package tf.ssf.sfort.script.instance;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;

import java.util.function.Predicate;

public class ItemScript extends AbstractExtendablePredicateProvider<Item> {

	public ItemScript() {
		help.put("block_item is_block_item","Item is a block");
		help.put("item .:ItemID", "Has to be the specified item");
	}
	@Override
	public Predicate<Item> getLocalPredicate(String in, String val){
		switch (in){
			case ".": case "item" : {
				final Item arg = Registries.ITEM.get(Identifier.of(val));
				return item -> item == arg;
			}
			default : return null;
		}
	}
	@Override
	public Predicate<Item> getLocalPredicate(String in){
		switch (in){
			case "block_item": case "is_block_item" : return item -> item instanceof BlockItem;
			default: return null;
		}
	}

}
