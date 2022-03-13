package tf.ssf.sfort.script.extended.mixin;

import net.minecraft.item.Item;
import net.minecraft.util.Rarity;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.mixin.ItemExtended;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class MixinExtendedItemScript implements PredicateProvider<Item>, Help {
    @Override
    public Predicate<Item> getPredicate(String in, String val, Set<String> dejavu){
        switch (in){
            case "rarity":
                Rarity arg = Rarity.valueOf(val);
                return item -> item instanceof ItemExtended && ((ItemExtended)item).fscript$rarity().equals(arg);
            default: return null;
        }
    }
    //==================================================================================================================

    @Override
    public Map<String, String> getHelp(){
        return help;
    }
    public final Map<String, String> help = new HashMap<>();
    public MixinExtendedItemScript() {
        help.put("rarity:RarityID", "Item has specified rarity");
    }
}
