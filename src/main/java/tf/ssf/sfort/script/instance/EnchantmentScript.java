package tf.ssf.sfort.script.instance;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.util.Identifier;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;

import java.util.function.Predicate;

public class EnchantmentScript extends AbstractExtendablePredicateProvider<RegistryEntry<Enchantment>> {

	public EnchantmentScript() {
		help.put("enchant .:EnchantID","Require specified enchant");
		help.put("min_level:int","Minimum min enchantment level");
		help.put("max_level:int","Minimum max enchantment level");
		help.put("acceptable_item:ItemID","Require enchantment to be applicable to item");
		help.put("treasure is_treasure","Require enchantment to be obtainable as a treasure");
		help.put("max_level is_max_level","Require maximum obtainable enchantment level");
		help.put("cursed is_cursed","Require enchantment to be a curse");
		help.put("villager_traded is_villager_traded available_for_enchanted_book_offer","Require enchantment to be tradable by villagers");
		help.put("loot is_loot available_for_random_selection","Require enchantment to be obtainable as a random chest loot");

	}

	@Override
	public Predicate<RegistryEntry<Enchantment>> getLocalPredicate(String in){
		switch (in){
			case "max_level": case "is_max_level" : return enchant -> enchant.value().getMinLevel() == enchant.value().getMaxLevel();
			case "treasure": case "is_treasure" : return entry -> entry.isIn(EnchantmentTags.TREASURE);
			case "cursed": case "is_cursed" : return entry -> entry.isIn(EnchantmentTags.CURSE);
			case "villager_traded": case "is_villager_traded": case "available_for_enchanted_book_offer" :
				return entry -> entry.isIn(EnchantmentTags.TRADEABLE);
			case "loot": case "is_loot": case "available_for_random_selection" :
					return entry -> entry.isIn(EnchantmentTags.ON_RANDOM_LOOT);
			default : return null;
		}
	}
	@Override
	public Predicate<RegistryEntry<Enchantment>> getLocalPredicate(String in, String val){
		switch (in){
			case ".": case "enchant" : {
				final Identifier id = Identifier.of(val);
				return enchant -> enchant.matchesId(id);
			}
			case "min_level" : {
				final int arg = Integer.parseInt(val);
				return enchant -> enchant.value().getMinLevel()>arg;
			}
			case "max_level" : {
				final int arg = Integer.parseInt(val);
				return enchant -> enchant.value().getMaxLevel()>arg;
			}
			case "acceptable_item" : {
				final Item arg = Registries.ITEM.get(Identifier.of(val));
				return enchant -> enchant.value().isAcceptableItem(arg.getDefaultStack());
			}
			default : return null;
		}
	}
}
