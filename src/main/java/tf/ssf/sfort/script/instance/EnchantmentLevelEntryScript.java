package tf.ssf.sfort.script.instance;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.Pair;
import tf.ssf.sfort.script.Default;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.PredicateProviderExtendable;

import java.util.*;
import java.util.function.Predicate;

public class EnchantmentLevelEntryScript implements PredicateProviderExtendable<Map.Entry<Enchantment, Integer>>, Help {
    @Override
    public Predicate<Map.Entry<Enchantment, Integer>> getPredicate(String in, String val, Set<Class<?>> dejavu) {
        switch (in) {
            case "level", "enchant_level" -> {
                final int arg = Integer.parseInt(val);
                return entry -> entry.getValue()>=arg;
            }
        }
        if (dejavu.add(Default.ENCHANTMENT.getClass())){
            Predicate<Enchantment> out = Default.ENCHANTMENT.getPredicate(in, val, dejavu);
            if (out != null) return entry -> out.test(entry.getKey());
        }
        return PredicateProviderExtendable.super.getPredicate(in, val, dejavu);
    }

    @Override
    public Predicate<Map.Entry<Enchantment, Integer>> getPredicate(String in, Set<Class<?>> dejavu) {
        if (dejavu.add(Default.ENCHANTMENT.getClass())){
            Predicate<Enchantment> out = Default.ENCHANTMENT.getPredicate(in, dejavu);
            if (out != null) return entry -> out.test(entry.getKey());
        }
        return PredicateProviderExtendable.super.getPredicate(in, dejavu);
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

    public final Map<String, String> help = new HashMap<>();
    public final List<Help> extend_help = new ArrayList<>();

    public EnchantmentLevelEntryScript() {
        help.put("enchant_level level:int","Minimum enchantment level");

        extend_help.add(new EnchantmentScript());
    }
    //==================================================================================================================

    public final TreeSet<Pair<Integer, PredicateProvider<Map.Entry<Enchantment, Integer>>>> EXTEND = new TreeSet<>(Comparator.comparingInt(Pair::getLeft));

    @Override
    public void addProvider(PredicateProvider<Map.Entry<Enchantment, Integer>> predicateProvider, int priority) {
        if (predicateProvider instanceof Help) extend_help.add((Help) predicateProvider);
        EXTEND.add(new Pair<>(priority, predicateProvider));
    }

    @Override
    public List<PredicateProvider<Map.Entry<Enchantment, Integer>>> getProviders() {
        return EXTEND.stream().map(Pair::getRight).toList();
    }

}
