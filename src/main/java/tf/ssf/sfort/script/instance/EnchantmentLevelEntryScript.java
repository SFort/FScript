package tf.ssf.sfort.script.instance;

import net.minecraft.enchantment.Enchantment;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;

import java.util.Map;
import java.util.function.Predicate;

public class EnchantmentLevelEntryScript extends AbstractExtendablePredicateProvider<Map.Entry<Enchantment, Integer>> {

    public EnchantmentLevelEntryScript() {
        help.put("enchant_level level:int","Minimum enchantment level");
    }

    @Override
    public Predicate<Map.Entry<Enchantment, Integer>> getLocalPredicate(String in, String val){
        switch (in) {
            case "level": case "enchant_level" : {
                final int arg = Integer.parseInt(val);
                return entry -> entry.getValue()>=arg;
            }
            default : return null;
        }
    }

}
