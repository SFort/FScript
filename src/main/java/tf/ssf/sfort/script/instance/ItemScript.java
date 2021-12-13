package tf.ssf.sfort.script.instance;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.PredicateProviderExtendable;

import java.util.*;
import java.util.function.Predicate;

public class ItemScript implements PredicateProviderExtendable<Item>, Help {
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

	//==================================================================================================================

	@Override
	public Predicate<Item> getPredicate(String in, String val, Set<Class<?>> dejavu){
		{
			final Predicate<Item> out = getLP(in, val);
			if (out != null) return out;
		}
		return PredicateProviderExtendable.super.getPredicate(in, val, dejavu);
	}

	@Override
	public Predicate<Item> getPredicate(String in, Set<Class<?>> dejavu){
		{
			final Predicate<Item> out = getLP(in);
			if (out != null) return out;
		}
		return PredicateProviderExtendable.super.getPredicate(in, dejavu);
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
	public ItemScript() {
		help.put("damageable is_damageable","Item has to be damageable");
		help.put("food is_food","Item is food");
		help.put("fireproof is_fireproof","Item is fireproof");
		help.put("block_item is_block_item","Item is a block");
		help.put("item .:ItemID", "Has to be the specified item");
		help.put("group:ItemGroupID", "Item has specified item group");
	}

	//==================================================================================================================

	public final TreeSet<Pair<Integer, PredicateProvider<Item>>> EXTEND = new TreeSet<>(Comparator.comparingInt(Pair::getLeft));

	@Override
	public void addProvider(PredicateProvider<Item> predicateProvider, int priority) {
		if (predicateProvider instanceof Help) extend_help.add((Help) predicateProvider);
		EXTEND.add(new Pair<>(priority, predicateProvider));
	}

	@Override
	public List<PredicateProvider<Item>> getProviders() {
		return EXTEND.stream().map(Pair::getRight).toList();
	}

}
