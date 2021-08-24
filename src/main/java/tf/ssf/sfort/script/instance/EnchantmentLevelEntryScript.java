package tf.ssf.sfort.script.instance;

import net.minecraft.enchantment.Enchantment;
import tf.ssf.sfort.script.Default;
import tf.ssf.sfort.script.PredicateProvider;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class EnchantmentLevelEntryScript implements PredicateProvider<Map.Entry<Enchantment, Integer>> {
    @Override
    public Predicate<Map.Entry<Enchantment, Integer>> getPredicate(String in, String val, Set<Class<?>> dejavu) {
        return switch (in) {
            case "level" -> {
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

}
