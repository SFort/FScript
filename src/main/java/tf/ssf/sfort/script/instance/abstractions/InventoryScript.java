package tf.ssf.sfort.script.instance.abstractions;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.ScriptParser;
import tf.ssf.sfort.script.instance.ItemStackScript;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public abstract class InventoryScript<T extends Inventory> implements PredicateProvider<T>, Help {
	public ScriptParser<ItemStack> ITEM_STACK_PARSER = new ScriptParser<>(new ItemStackScript());

	public Predicate<T> getLE(String in, String script){
		return switch (in) {
			case "slot" -> {
				final Predicate<ItemStack> predicate = ITEM_STACK_PARSER.parse(script);
				if (predicate == null) yield null;
				yield inventory -> {
					boolean rez = false;
					for(int i = 0; i<inventory.size() && !rez; i++)
						rez=predicate.test(inventory.getStack(i));
					return rez;
				};
			}
			default -> null;
		};
	}
	public Predicate<T> getLE(String in, String val, String script){
		return switch (in) {
			case "slot" ->{
				final Predicate<ItemStack> predicate = ITEM_STACK_PARSER.parse(script);
				if (predicate == null) yield null;
				final int arg = Integer.parseInt(val);
				yield inventory -> predicate.test(inventory.getStack(arg));
			}
			default -> null;
		};
	}

	//==================================================================================================================

	@Override
	public Predicate<T> getEmbed(String in, String script, Set<Class<?>> dejavu){
		return getLE(in, script);
	}

	@Override
	public Predicate<T> getEmbed(String in, String val, String script, Set<Class<?>> dejavu){
		return getLE(in, val, script);
	}

	//==================================================================================================================

	@Override
	public Map<String, String> getHelp(){
		return help;
	}
	public static final Map<String, String> help = new HashMap<String, String>();
	static {
		help.put("~slot:ITEM_STACK", "Inventory has to contain matching item");
		help.put("~slot~int:ITEM_STACK","Inventory slot has to contain matching item");
	}
}
