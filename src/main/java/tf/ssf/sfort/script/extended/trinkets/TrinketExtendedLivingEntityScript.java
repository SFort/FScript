package tf.ssf.sfort.script.extended.trinkets;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.util.DefaultParsers;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class TrinketExtendedLivingEntityScript implements PredicateProvider<LivingEntity>, Help {
    private static Method getComponent, isEquiped;
    public static boolean success;

    static {
        try {
            getComponent = Class.forName("dev.emi.trinkets.api.TrinketsApi").getMethod("getTrinketComponent", LivingEntity.class);
            isEquiped = Class.forName("dev.emi.trinkets.api.TrinketComponent").getMethod("isEquipped", Predicate.class);

            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
    }

    @Override
    public Predicate<LivingEntity> getPredicate(String in, String val, Set<String> dejavu){
        switch (in){
            case "trinket":
                final Item arg = Registries.ITEM.get(new Identifier(val));
                return entity -> {
                    try {
                        Optional<?> comp = (Optional<?>) getComponent.invoke(null, entity);
                        if (!comp.isPresent()) return true;
                        return (boolean) isEquiped.invoke(comp.get(), (Predicate<ItemStack>)(ItemStack stack) -> stack.getItem() == arg);
                    }catch (Exception ignore){
                        return true;
                    }
                };
            default: return null;
        }
    }
    @Override
    public Predicate<LivingEntity> getEmbed(String key, String script, Set<String> dejavu){
        switch (key) {
            case "trinket" : {
                final Predicate<ItemStack> predicate = DefaultParsers.ITEM_STACK_PARSER.parse(script);
                if (predicate == null) return null;
                return entity -> {
                    try {
                        Optional<?> comp = (Optional<?>) getComponent.invoke(null, entity);
                        if (!comp.isPresent()) return true;
                        return (boolean) isEquiped.invoke(comp, predicate);
                    }catch (Exception ignore){
                        return true;
                    }
                };
            }
            default : return null;
        }
    }
    //TODO indexed embed

    //==================================================================================================================

    @Override
    public Map<String, String> getHelp(){
        return help;
    }
    public final Map<String, String> help = new HashMap<>();
    public TrinketExtendedLivingEntityScript() {
        help.put("~trinket:ITEM_STACK","Require item stack equipped as a trinket");
        help.put("trinket:ItemID","Require item equipped as a trinket");

    }
}
