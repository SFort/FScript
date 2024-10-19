package tf.ssf.sfort.script.instance;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;

import java.util.function.Predicate;

public class EnchantmentScript extends AbstractExtendablePredicateProvider<Enchantment> {

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

	@Override
	public Predicate<Enchantment> getLocalPredicate(String in){
		switch (in){
			case "max_level": case "is_max_level" : return enchant -> enchant.getMinLevel() == enchant.getMaxLevel();
			case "treasure": case "is_treasure" : return Enchantment::isTreasure;
			case "cursed": case "is_cursed" : return Enchantment::isCursed;
			case "villager_traded": case "is_villager_traded": case "available_for_enchanted_book_offer" :
				return Enchantment::isAvailableForEnchantedBookOffer;
			case "loot": case "is_loot": case "available_for_random_selection" :
					return Enchantment::isAvailableForRandomSelection;
			default : return null;
		}
	}
	@Override
	public Predicate<Enchantment> getLocalPredicate(String in, String val){
		switch (in){
			case ".": case "enchant" : {
				final Enchantment arg = Registries.ENCHANTMENT.get(Identifier.of(val));
				return enchant -> enchant.equals(arg);
			}
			case "min_level" : {
				final int arg = Integer.parseInt(val);
				return enchant -> enchant.getMinLevel()>arg;
			}
			case "max_level" : {
				final int arg = Integer.parseInt(val);
				return enchant -> enchant.getMaxLevel()>arg;
			}
			case "acceptable_item" : {
				final Item arg = Registries.ITEM.get(new Identifier(val));
				return enchant -> enchant.isAcceptableItem(arg.getDefaultStack());
			}
			case "rarity" :{
				final Enchantment.Rarity arg = Enchantment.Rarity.valueOf(val);
				return enchant -> enchant.getRarity().equals(arg);
			}
			case "target" : {
				final EnchantmentTarget arg = EnchantmentTarget.valueOf(val);
				return enchant -> enchant.target.equals(arg);
			}
			default : return null;
		}
	}
}
