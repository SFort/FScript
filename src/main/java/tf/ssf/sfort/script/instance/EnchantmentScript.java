package tf.ssf.sfort.script.instance;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class EnchantmentScript implements PredicateProvider<Enchantment>, Help {

	public Predicate<Enchantment> getLP(String in){
		return switch (in){
			case "max_level" -> enchant -> enchant.getMinLevel() == enchant.getMaxLevel();
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
			case "." -> {
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
			case "rarity" -> enchant -> enchant.getRarity().name().equals(val);
			case "type" -> enchant -> enchant.type.name().equals(val);
			default -> null;
		};
	}

	//==================================================================================================================

	@Override
	public Predicate<Enchantment> getPredicate(String in, Set<Class<?>> dejavu){
		return getLP(in);
	}

	@Override
	public Predicate<Enchantment> getPredicate(String in, String val, Set<Class<?>> dejavu){
		return getLP(in,val);
	}

	//==================================================================================================================

	//TODO
	public static final Map<String, String> help = new HashMap<>();
	static {
		help.put("enchant:EnchantID","Require specified enchant");
	}
	public Map<String, String> getHelp(){
		return help;
	}
	public Map<String, String> getAllHelp(Set<Class<?>> dejavu){
		return getHelp();
	}
}
