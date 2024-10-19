package tf.ssf.sfort.script.instance;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;

import java.util.function.Predicate;

public class EnchantmentLevelEntryScript extends AbstractExtendablePredicateProvider<Object2IntMap.Entry<RegistryEntry<Enchantment>>> {

    public EnchantmentLevelEntryScript() {
        help.put("enchant_level level:int","Minimum enchantment level");
    }

    @Override
    public Predicate<Object2IntMap.Entry<RegistryEntry<Enchantment>>> getLocalPredicate(String in, String val){
        switch (in) {
            case "level": case "enchant_level" : {
                final int arg = Integer.parseInt(val);
                return entry -> entry.getIntValue()>=arg;
            }
            default : return null;
        }
    }

}
