package tf.ssf.sfort.script.instance;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import tf.ssf.sfort.script.Default;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.PredicateProviderExtendable;

import java.util.*;
import java.util.function.Predicate;

public class InventoryScript<T extends Inventory> implements PredicateProviderExtendable<T>, Help {

	public Predicate<T> getLE(String in, String script){
		return switch (in) {
			case "slot" -> {
				final Predicate<ItemStack> predicate = Default.ITEM_STACK_PARSER.parse(script);
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
				final Predicate<ItemStack> predicate = Default.ITEM_STACK_PARSER.parse(script);
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
		{
			final Predicate<T> out = getLE(in, script);
			if (out !=null) return out;
		}
		return PredicateProviderExtendable.super.getEmbed(in, script, dejavu);
	}

	@Override
	public Predicate<T> getEmbed(String in, String val, String script, Set<Class<?>> dejavu){
		{
			final Predicate<T> out = getLE(in, val, script);
			if (out !=null) return out;
		}
		return PredicateProviderExtendable.super.getEmbed(in, val, script, dejavu);
	}

	//==================================================================================================================

	@Override
	public Map<String, String> getHelp(){
		return help;
	}
	@Override
	public List<Help> getImported(){
		return extend_help;
	}
	public final Map<String, String> help = new HashMap<>();
	public final List<Help> extend_help = new ArrayList<>();
	public InventoryScript() {
		help.put("~slot:ITEM_STACK", "Inventory has to contain matching item");
		help.put("~slot~int:ITEM_STACK","Inventory slot has to contain matching item");
	}
	//==================================================================================================================

	public final TreeSet<Pair<Integer, PredicateProvider<T>>> EXTEND = new TreeSet<>(Comparator.comparingInt(Pair::getLeft));

	@Override
	public void addProvider(PredicateProvider<T> predicateProvider, int priority) {
		if (predicateProvider instanceof Help) extend_help.add((Help) predicateProvider);
		EXTEND.add(new Pair<>(priority, predicateProvider));
	}

	@Override
	public List<PredicateProvider<T>> getProviders() {
		return EXTEND.stream().map(Pair::getRight).toList();
	}

}
