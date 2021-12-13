package tf.ssf.sfort.script.instance;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.PredicateProviderExtendable;

import java.util.*;
import java.util.function.Predicate;

public class EnchantmentScript implements PredicateProviderExtendable<Enchantment>, Help {

	public Predicate<Enchantment> getLP(String in){
		return switch (in){
			case "max_level", "is_max_level" -> enchant -> enchant.getMinLevel() == enchant.getMaxLevel();
			case "treasure", "is_treasure" -> Enchantment::isTreasure;
			case "cursed", "is_cursed" -> Enchantment::isCursed;
			case "villager_traded", "is_villager_traded", "available_for_enchanted_book_offer" ->
					Enchantment::isAvailableForEnchantedBookOffer;
			case "loot", "is_loot", "available_for_random_selection" ->
					Enchantment::isAvailableForRandomSelection;
			default -> null;
		};
	}

	public Predicate<Enchantment> getLP(String in, String val){
		return switch (in){
			case ".", "enchant" -> {
				final Enchantment arg = Registry.ENCHANTMENT.get(new Identifier(val));
				yield enchant -> enchant.equals(arg);
			}
			case "min_level" -> {
				final int arg = Integer.parseInt(val);
				yield enchant -> enchant.getMinLevel()>arg;
			}
			case "max_level" -> {
				final int arg = Integer.parseInt(val);
				yield enchant -> enchant.getMaxLevel()>arg;
			}
			case "acceptable_item" -> {
				final Item arg = Registry.ITEM.get(new Identifier(val));
				yield enchant -> enchant.type.isAcceptableItem(arg);
			}
			case "rarity" ->{
				final Enchantment.Rarity arg = Enchantment.Rarity.valueOf(val);
				yield enchant -> enchant.getRarity().equals(arg);
			}
			case "target" -> {
				final EnchantmentTarget arg = EnchantmentTarget.valueOf(val);
				yield enchant -> enchant.type.equals(arg);
			}
			default -> null;
		};
	}

	//==================================================================================================================

	@Override
	public Predicate<Enchantment> getPredicate(String in, Set<Class<?>> dejavu){
		{
			final Predicate<Enchantment> out = getLP(in);
			if (out != null) return out;
		}
		return PredicateProviderExtendable.super.getPredicate(in, dejavu);
	}

	@Override
	public Predicate<Enchantment> getPredicate(String in, String val, Set<Class<?>> dejavu){
		{
			final Predicate<Enchantment> out = getLP(in, val);
			if (out != null) return out;
		}
		return PredicateProviderExtendable.super.getPredicate(in, val, dejavu);
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
	public EnchantmentScript() {
		help.put("enchant .:EnchantID","Require specified enchant");
		help.put("min_level:int","Minimum min enchantment level");
		help.put("max_level:int","Minimum max enchantment level");
		help.put("acceptable_item:ItemID","Require enchantment to be applicable to item");
		help.put("rarity:EnchantRarityID","Require enchantment to be this rarity");
		help.put("target:EnchantTargetID","Require enchantment to have this target type requirement");
		help.put("treasure is_treasure","Require enchantment to be obtainable as a treasure");
		help.put("max_level is_max_level","Require maximum obtainable enchantment level");
		help.put("cursed is_cursed","Require enchantment to be a curse");
		help.put("villager_traded is_villager_traded available_for_enchanted_book_offer","Require enchantment to be tradable by villagers");
		help.put("loot is_loot available_for_random_selection","Require enchantment to be obtainable as a random chest loot");

	}
	//==================================================================================================================

	public final TreeSet<Pair<Integer, PredicateProvider<Enchantment>>> EXTEND = new TreeSet<>(Comparator.comparingInt(Pair::getLeft));

	@Override
	public void addProvider(PredicateProvider<Enchantment> predicateProvider, int priority) {
		if (predicateProvider instanceof Help) extend_help.add((Help) predicateProvider);
		EXTEND.add(new Pair<>(priority, predicateProvider));
	}

	@Override
	public List<PredicateProvider<Enchantment>> getProviders() {
		return EXTEND.stream().map(Pair::getRight).toList();
	}

}
