package tf.ssf.sfort.script.instance;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;

import java.util.Set;
import java.util.function.Predicate;

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
    @Override
    public String getHelp(){
        return
                String.format("\t%-20s%-70s%s%n","hand","- Require item in main hand","ItemID")+
                String.format("\t%-20s%-70s%s%n","offhand","- Require item in off hand","ItemID")+
                String.format("\t%-20s%-70s%s%n","helm","- Require item as helmet","ItemID")+
                String.format("\t%-20s%-70s%s%n","chest","- Require item as chestplate","ItemID")+
                String.format("\t%-20s%-70s%s%n","legs","- Require item as leggings","ItemID")+
                String.format("\t%-20s%-70s%s%n","boots","- Require item as boots","ItemID")+
                String.format("\t%-20s%-70s%s%n","effect","- Require potion effect","EffectID")+
                String.format("\t%-20s%-70s%s%n","health","- Minimum required heath","float")+
                String.format("\t%-20s%-70s%s%n","attack","- Minimum ticked passed since player attacked","float")+
                String.format("\t%-20s%-70s%s%n","attacked","- Minimum ticks passed since player was attacked","float")+
                String.format("\t%-20s%s%n","full_hp","- Require full health")+
                String.format("\t%-20s%s%n","sprinting","- Require Sprinting")+
                String.format("\t%-20s%s%n","blocking","- Require Blocking")+
                String.format("\t%-20s%s%n","using","- Require using items")
                ;
    }
    @Override
    public String getAllHelp(Set<Class<?>> dejavu){
        return (dejavu.add(ENTITY.getClass())?ENTITY.getAllHelp(dejavu):"")+getHelp();
    }
    private static Item getItem(String id){
        return Registry.ITEM.get(new Identifier(id));
    }
    private static boolean eq(Item required, ItemStack current){
        return required != null && required == current.getItem();
    }
}
