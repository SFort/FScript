package tf.ssf.sfort.script.instance;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
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
		return getLP(in);
	}

	@Override
	public Predicate<Enchantment> getPredicate(String in, String val, Set<Class<?>> dejavu){
		return getLP(in,val);
	}

	//==================================================================================================================

	@Override
	public Map<String, String> getHelp(){
		return help;
	}

	public static final Map<String, String> help = new HashMap<>();
	static {
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
}
