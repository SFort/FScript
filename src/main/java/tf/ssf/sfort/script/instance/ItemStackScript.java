package tf.ssf.sfort.script.instance;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.ScriptParser;

import java.util.*;
import java.util.function.Predicate;

public class ItemStackScript implements PredicateProvider<ItemStack>, Help {
	public ScriptParser<Map.Entry<Enchantment, Integer>> ENCHANTMENT_PARSER = new ScriptParser<>(new EnchantmentLevelEntryScript());

	public Predicate<ItemStack> getLP(String in, String val){
		return switch (in){
			case "item" -> {
				final Item arg = Registry.ITEM.get(new Identifier(val));
				yield item -> item.isOf(arg);
			}
			case "enchant" -> {
				final Enchantment arg = Registry.ENCHANTMENT.get(new Identifier(val));
				yield item -> EnchantmentHelper.get(item).containsKey(arg);
			}
			case "count" -> {
				final int arg = Integer.parseInt(val);
				yield item -> item.getCount()>=arg;
			}
			default -> null;
		};
	}

	public Predicate<ItemStack> getLP(String in){
		return switch (in){
			case "damageable" -> ItemStack::isDamageable;
			case "empty" -> ItemStack::isEmpty;
			case "damaged" -> ItemStack::isDamaged;
			case "stackable" -> ItemStack::isStackable;
			case "enchantable" -> ItemStack::isEnchantable;
			case "has_glint" -> ItemStack::hasGlint;
			case "has_nbt" -> ItemStack::hasNbt;
			case "has_enchantments" -> ItemStack::hasEnchantments;
			case "in_frame" -> ItemStack::isInFrame;
			default -> null;
		};
	}

	public Predicate<ItemStack> getLE(String in, String script){
		return switch (in) {
			case "enchant" -> {
				final Predicate<Map.Entry<Enchantment, Integer>> predicate = ENCHANTMENT_PARSER.parse(script);
				if (predicate == null) yield null;
				yield item -> {
					boolean rez = false;
					final Iterator<Map.Entry<Enchantment, Integer>> i = EnchantmentHelper.get(item).entrySet().iterator();
					while(i.hasNext() && !rez)
						rez=predicate.test(i.next());
					return rez;
				};
			}
			default -> null;
		};
	}
	public Predicate<ItemStack> getLE(String in, String val, String script){
		return switch (in) {
			case "enchant" ->{
				final Predicate<Map.Entry<Enchantment, Integer>> predicate = ENCHANTMENT_PARSER.parse(script);
				if (predicate == null) yield null;
				final Enchantment arg = Registry.ENCHANTMENT.get(new Identifier(val));
				//TODO should probably throw exception
				if (arg == null) yield null;
				yield item -> {
					final Integer lvl = EnchantmentHelper.get(item).get(arg);
					return lvl != null && predicate.test(Map.entry(arg, lvl));
				};
			}
			default -> null;
		};
	}

	//==================================================================================================================

	@Override
	public Predicate<ItemStack> getPredicate(String in, String val, Set<Class<?>> dejavu){
		return getLP(in,val);
	}

	@Override
	public Predicate<ItemStack> getPredicate(String in, Set<Class<?>> dejavu){
		return getLP(in);
	}

	@Override
	public Predicate<ItemStack> getEmbed(String in, String script){
		return getLE(in, script);
	}

	@Override
	public Predicate<ItemStack> getEmbed(String in, String val, String script){
		return getLE(in, val, script);
	}

	//==================================================================================================================

	public static final Map<String, String> help = new HashMap<>();
	static {
		help.put("item:ItemID","Has to be the specified item");
		help.put("enchant:EnchantID","Item has to have specified enchantment");
	}
	public Map<String, String> getHelp(){
		return help;
	}
	public Map<String, String> getAllHelp(Set<Class<?>> dejavu){
		return getHelp();
	}
}
