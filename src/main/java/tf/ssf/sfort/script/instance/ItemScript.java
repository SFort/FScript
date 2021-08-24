package tf.ssf.sfort.script.instance;

import net.minecraft.item.Item;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class ItemScript implements PredicateProvider<Item>, Help {
	public Predicate<Item> getLP(String in, String val){
		return switch (in){
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


	//==================================================================================================================

	@Override
	public Predicate<Item> getPredicate(String in, String val, Set<Class<?>> dejavu){
		return getLP(in,val);
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
