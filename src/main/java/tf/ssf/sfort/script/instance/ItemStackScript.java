package tf.ssf.sfort.script.instance;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class ItemStackScript implements PredicateProvider<ItemStack>, Help {
	public static final Map<String, String> help = new HashMap<>();
	@Override
	public Predicate<ItemStack> getPredicate(String in, String val, Set<Class<?>> dejavu){
		return getLP(in,val);
	}

	public Predicate<ItemStack> getLP(String in, String val){
		return switch (in){
			case "item" -> {
				Item arg = Registry.ITEM.get(new Identifier(val));
				yield item -> item.isOf(arg);
			}
			case "enchant" -> {
			    Enchantment arg = Registry.ENCHANTMENT.get(new Identifier(val));
			    yield item -> EnchantmentHelper.get(item).containsKey(arg);
			}
            default -> null;
		};
	}
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
