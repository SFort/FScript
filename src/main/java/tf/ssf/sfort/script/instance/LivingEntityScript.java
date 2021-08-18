package tf.ssf.sfort.script.instance;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import tf.ssf.sfort.script.Default;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LivingEntityScript<T extends LivingEntity> implements PredicateProvider<T>, Help {
    private final EntityScript<T> ENTITY = new EntityScript<>();
    public Predicate<T> getLP(String in, String val){
        return switch (in){
            case "hand" -> {
                Item arg = getItem(val);
                yield entity -> eq(arg, entity.getMainHandStack());
            }
            case "offhand" -> {
                Item arg = getItem(val);
                yield entity -> eq(arg, entity.getOffHandStack());
            }
            case "helm" -> {
                Item arg = getItem(val);
                yield entity -> eq(arg, entity.getEquippedStack(EquipmentSlot.HEAD));
            }
            case "chest" -> {
                Item arg = getItem(val);
                yield entity -> eq(arg, entity.getEquippedStack(EquipmentSlot.CHEST));
            }
            case "legs" -> {
                Item arg = getItem(val);
                yield entity -> eq(arg, entity.getEquippedStack(EquipmentSlot.LEGS));
            }
            case "boots" -> {
                Item arg = getItem(val);
                yield entity -> eq(arg, entity.getEquippedStack(EquipmentSlot.FEET));
            }
            case "health" -> {
                float arg = Float.parseFloat(val);
                yield entity -> entity.getHealth()>=arg;
            }
            case "effect" -> {
                StatusEffect arg = SimpleRegistry.STATUS_EFFECT.get(new Identifier(val));
                yield entity -> entity.hasStatusEffect(arg);
            }
            case "attack" -> {
                int arg = Integer.parseInt(val);
                yield entity -> entity.age - entity.getLastAttackTime() > arg;
            }
            case "attacked" -> {
                int arg = Integer.parseInt(val);
                yield entity -> entity.age - entity.getLastAttackedTime() > arg;
            }
            default -> null;
        };
    }
    public Predicate<T> getLP(String in){
        return switch (in) {
            case "full_hp" -> entity -> entity.getHealth() == entity.getMaxHealth();
            case "blocking" -> LivingEntity::isBlocking;
            case "using" -> LivingEntity::isUsingItem;
            case "fall_flying" -> LivingEntity::isFallFlying;
            default -> null;
        };
    }
    @Override
    public Predicate<T> getPredicate(String in, String val, Set<Class<?>> dejavu){
        {
            Predicate<T> out = getLP(in, val);
            if (out != null) return out;
        }
        if (dejavu.add(ENTITY.getClass())){
            Predicate<T> out = ENTITY.getPredicate(in, val, dejavu);
            if (out !=null) return out;
        }
        return null;
    }
    @Override
    public Predicate<T> getPredicate(String in, Set<Class<?>> dejavu){
        {
            Predicate<T> out = getLP(in);
            if (out != null) return out;
        }
        if (dejavu.add(ENTITY.getClass())){
            Predicate<T> out = ENTITY.getPredicate(in, dejavu);
            if (out !=null) return out;
        }
        return null;
    }
    public static final Map<String, String> help = new HashMap<>();
    static {
        help.put("hand:ItemID","Require item in main hand");
        help.put("offhand:ItemID","Require item in off hand");
        help.put("helm:ItemID","Require item as helmet");
        help.put("chest:ItemID","Require item as chestplate");
        help.put("legs:ItemID","Require item as leggings");
        help.put("boots:ItemID","Require item as boots");
        help.put("effect:EffectID","Require potion effect");
        help.put("health:float","Minimum required heath");
        help.put("attack:int","Minimum ticked passed since player attacked");
        help.put("attacked:int","Minimum ticks passed since player was attacked");
        help.put("full_hp","Require full health");
        help.put("sprinting","Require Sprinting");
        help.put("blocking","Require Blocking");
        help.put("using","Require using items");
        help.put("fall_flying","Require flying with elytra");

    }
    @Override
    public Map<String, String> getHelp(){
        return help;
    }
    @Override
    public Map<String, String> getAllHelp(Set<Class<?>> dejavu){
        Stream<Map.Entry<String, String>> out = new HashMap<String, String>().entrySet().stream();
        if (dejavu.add(WorldScript.class)) out = Stream.concat(out, Default.ENTITY.getAllHelp(dejavu).entrySet().stream());
        out = Stream.concat(out, getAllHelp().entrySet().stream());

        return out.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    private static Item getItem(String id){
        return Registry.ITEM.get(new Identifier(id));
    }
    private static boolean eq(Item required, ItemStack current){
        return required != null && required == current.getItem();
    }
}
