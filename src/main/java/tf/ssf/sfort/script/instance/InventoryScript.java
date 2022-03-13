package tf.ssf.sfort.script.instance;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;
import tf.ssf.sfort.script.util.DefaultParsers;

import java.util.function.Predicate;

public class InventoryScript<T extends Inventory> extends AbstractExtendablePredicateProvider<T> {

	public InventoryScript() {
		help.put("~slot:ITEM_STACK", "Inventory has to contain matching item");
		help.put("~slot~int:ITEM_STACK","Inventory slot has to contain matching item");
	}

	@Override
	public Predicate<T> getLocalEmbed(String in, String script){
		switch (in) {
			case "slot" : {
				final Predicate<ItemStack> predicate = DefaultParsers.ITEM_STACK_PARSER.parse(script);
				if (predicate == null) return null;
				return inventory -> {
					boolean rez = false;
					for(int i = 0; i<inventory.size() && !rez; i++)
						rez=predicate.test(inventory.getStack(i));
					return rez;
				};
			}
			default : return null;
		}
	}
	@Override
	public Predicate<T> getLocalEmbed(String in, String val, String script){
		switch (in) {
			case "slot" :{
				final Predicate<ItemStack> predicate = DefaultParsers.ITEM_STACK_PARSER.parse(script);
				if (predicate == null) return null;
				final int arg = Integer.parseInt(val);
				return inventory -> predicate.test(inventory.getStack(arg));
			}
			default : return null;
		}
	}

}
