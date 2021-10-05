package tf.ssf.sfort.script.instance;

import net.minecraft.enchantment.Enchantment;
import tf.ssf.sfort.script.Default;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;

import java.util.*;
import java.util.function.Predicate;

public class EnchantmentLevelEntryScript implements PredicateProvider<Map.Entry<Enchantment, Integer>>, Help {
    @Override
    public Predicate<Map.Entry<Enchantment, Integer>> getPredicate(String in, String val, Set<Class<?>> dejavu) {
        return switch (in) {
            case "level", "enchant_level" -> {
                final int arg = Integer.parseInt(val);
                yield entry -> entry.getValue()>=arg;
            }
            default -> entry -> Default.ENCHANTMENT.getPredicate(in, val, dejavu).test(entry.getKey());
        };
    }

    @Override
    public Predicate<Map.Entry<Enchantment, Integer>> getPredicate(String in, Set<Class<?>> dejavu) {
        return entry -> Default.ENCHANTMENT.getPredicate(in, dejavu).test(entry.getKey());
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

    public static final Map<String, String> help = new HashMap<String, String>();
    public static final List<Help> extend_help = new ArrayList<>();

    static {
        help.put("enchant_level level:int","Minimum enchantment level");

        extend_help.add(new EnchantmentScript());
    }

}
