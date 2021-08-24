package tf.ssf.sfort.script.instance;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.mixin_extended.Config;
import tf.ssf.sfort.script.mixin_extended.ItemExtended;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class ItemScript implements PredicateProvider<Item>, Help {
	public Predicate<Item> getLP(String in, String val){
		return switch (in){
			case "." -> {
				final Item arg = Registry.ITEM.get(new Identifier(val));
				yield item -> item == arg;
			}
			case "group" -> item -> {
				final ItemGroup group = item.getGroup();
				return group != null && group.getName().equals(val);
			};
			//TODO
			default -> null;
		};
	}

	public Predicate<Item> getLP(String in){
		return switch (in){
			case "damageable" -> Item::isDamageable;
			case "food" -> Item::isFood;
			case "fireproof" -> Item::isFireproof;
			default -> null;
		};
	}

	public Predicate<ItemExtended> getEP(String in, String val){
		return switch (in){
			case "rarity" -> item -> item.fscript$rarity().name().equals(val);
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
		if (Config.extended){
			final Predicate<ItemExtended> out = getEP(in, val);
			if (out != null) return item -> out.test((ItemExtended) item);
		}
		return null;
	}

	@Override
	public Predicate<Item> getPredicate(String in, Set<Class<?>> dejavu){
		return getLP(in);
	}


	//==================================================================================================================

	public static final Map<String, String> help = new HashMap<>();
	static {
		help.put("damageable","Item has to be damageable");
		help.put("food","Item is food");
		help.put("fireproof","Item is fireproof");
	}
	public Map<String, String> getHelp(){
		return help;
	}
	public Map<String, String> getAllHelp(Set<Class<?>> dejavu){
		return getHelp();
	}
}
