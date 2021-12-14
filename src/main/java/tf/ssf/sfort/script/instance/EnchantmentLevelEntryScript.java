package tf.ssf.sfort.script.instance;

import net.minecraft.enchantment.Enchantment;
import tf.ssf.sfort.script.instance.util.AbstractExtendablePredicateProvider;

import java.util.Map;
import java.util.function.Predicate;

public class EnchantmentLevelEntryScript extends AbstractExtendablePredicateProvider<Map.Entry<Enchantment, Integer>> {

    public EnchantmentLevelEntryScript() {
        help.put("enchant_level level:int","Minimum enchantment level");
    }

    @Override
    public Predicate<Map.Entry<Enchantment, Integer>> getLocalPredicate(String in, String val){
        return switch (in) {
            case "level", "enchant_level" -> {
                final int arg = Integer.parseInt(val);
                yield entry -> entry.getValue()>=arg;
            }
            default -> null;
        };
    }

}
