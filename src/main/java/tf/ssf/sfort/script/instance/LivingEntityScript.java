package tf.ssf.sfort.script.instance;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.SimpleRegistry;
import tf.ssf.sfort.script.instance.support.AbstractExtendablePredicateProvider;
import tf.ssf.sfort.script.instance.support.DefaultParsers;

import java.util.function.Predicate;

public class LivingEntityScript<T extends LivingEntity> extends AbstractExtendablePredicateProvider<T> {

    public LivingEntityScript() {
        help.put("hand:ItemID","Require item in main hand");
        help.put("offhand:ItemID","Require item in off hand");
        help.put("helm:ItemID","Require item as helmet");
        help.put("chest:ItemID","Require item as chestplate");
        help.put("legs:ItemID","Require item as leggings");
        help.put("boots:ItemID","Require item as boots");
        help.put("~hand:ITEM_STACK","Require item in main hand");
        help.put("~offhand:ITEM_STACK","Require item in off hand");
        help.put("~helm:ITEM_STACK","Require item as helmet");
        help.put("~chest:ITEM_STACK","Require item as chestplate");
        help.put("~legs:ITEM_STACK","Require item as leggings");
        help.put("~boots:ITEM_STACK","Require item as boots");
        help.put("effect:EffectID","Require potion effect");
        help.put("hp health:float","Minimum required heath");
        help.put("max_hp max_health:float","Minimum required max heath");
        help.put("movement_speed:float","Require going at this speed");
        help.put("sideways_speed:float","Require going sideways at this speed");
        help.put("upward_speed:float","Require going up at this speed");
        help.put("forward_speed:float","Require going forward at this speed");
        help.put("attack:int","Minimum ticked passed since player attacked");
        help.put("attacked:int","Minimum ticks passed since player was attacked");
        help.put("stuck_arrow_count:int", "Minimum amount of arrows stuck in entity");
        help.put("stinger_count:int", "Minimum amount of stingers");
        help.put("armor:int","Minimum required armor");
        help.put("full_hp max_hp full_health max_health is_full_hp is_max_hp is_full_health is_max_health","Require full health");
        help.put("blocking is_blocking","Require Blocking");
        help.put("climbing is_climbing","Require Climbing");
        help.put("using is_using","Require using items");
        help.put("fall_flying is_fall_flying","Require flying with elytra");
        help.put("~player:PLAYER_ENTITY", "Require a player entity");
        help.put("~server_player:SERVER_PLAYER_ENTITY", "Require a server player entity");
    }

    @Override
    public Predicate<T> getLocalPredicate(String in, String val){
        return switch (in){
            case "hand", "offhand", "helm", "chest", "legs", "boots" ->
                    getLocalEmbed(in, ".:"+val);
            case "health", "hp" -> {
                final float arg = Float.parseFloat(val);
                yield entity -> entity.getHealth()>=arg;
            }
            case "max_health", "max_hp" -> {
                final float arg = Float.parseFloat(val);
                yield entity -> entity.getMaxHealth()>=arg;
            }
            case "armor" -> {
                final float arg = Integer.parseInt(val);
                yield entity -> entity.getArmor()>=arg;
            }
            case "effect" -> {
                final StatusEffect arg = SimpleRegistry.STATUS_EFFECT.get(new Identifier(val));
                yield entity -> entity.hasStatusEffect(arg);
            }
            case "attack" -> {
                final int arg = Integer.parseInt(val);
                yield entity -> entity.age - entity.getLastAttackTime() > arg;
            }
            case "attacked" -> {
                final int arg = Integer.parseInt(val);
                yield entity -> entity.age - entity.getLastAttackedTime() > arg;
            }
            case "stuck_arrow_count" -> {
                final int arg = Integer.parseInt(val);
                yield entity -> entity.getStuckArrowCount() > arg;
            }
            case "stinger_count" -> {
                final int arg = Integer.parseInt(val);
                yield entity -> entity.getStingerCount() > arg;
            }
            case "sideways_speed" -> {
                final float arg = Float.parseFloat(val);
                yield entity -> entity.sidewaysSpeed>=arg;
            }
            case "upward_speed" -> {
                final float arg = Float.parseFloat(val);
                yield entity -> entity.upwardSpeed>=arg;
            }
            case "forward_speed" -> {
                final float arg = Float.parseFloat(val);
                yield entity -> entity.forwardSpeed>=arg;
            }
            case "movement_speed" -> {
                final float arg = Float.parseFloat(val);
                yield entity -> entity.getMovementSpeed()>=arg;
            }
            default -> null;
        };
    }

    @Override
    public Predicate<T> getLocalPredicate(String in){
        return switch (in) {
            case "full_hp", "max_hp", "full_health", "max_health", "is_full_hp", "is_max_hp", "is_full_health", "is_max_health"
                    -> entity -> entity.getHealth() == entity.getMaxHealth();
            case "blocking", "is_blocking" -> LivingEntity::isBlocking;
            case "climbing", "is_climbing" -> LivingEntity::isClimbing;
            case "using", "is_using" -> LivingEntity::isUsingItem;
            case "fall_flying", "is_fall_flying" -> LivingEntity::isFallFlying;
            default -> null;
        };
    }

    @Override
    public Predicate<T> getLocalEmbed(String in, String script){
        return switch (in) {
            case "hand", "offhand", "helm", "chest", "legs", "boots" ->{
                final Predicate<ItemStack> predicate = DefaultParsers.ITEM_STACK_PARSER.parse(script);
                if (predicate == null) yield null;
                yield switch (in) {
                    case "hand" -> entity -> predicate.test(entity.getMainHandStack());
                    case "offhand" -> entity -> predicate.test(entity.getOffHandStack());
                    case "helm" -> entity -> predicate.test(entity.getEquippedStack(EquipmentSlot.HEAD));
                    case "chest" -> entity -> predicate.test(entity.getEquippedStack(EquipmentSlot.CHEST));
                    case "legs" -> entity -> predicate.test(entity.getEquippedStack(EquipmentSlot.LEGS));
                    default -> entity -> predicate.test(entity.getEquippedStack(EquipmentSlot.FEET));
                };
            }
            case "player" -> {
                final Predicate<PlayerEntity> predicate = DefaultParsers.PLAYER_ENTITY_PARSER.parse(script);
                if (predicate == null) yield null;
                yield entity -> {
                    if (entity instanceof PlayerEntity)
                        return predicate.test(((PlayerEntity) entity));
                    return false;
                };
            }
            case "server_player" -> {
                final Predicate<ServerPlayerEntity> predicate = DefaultParsers.SERVER_PLAYER_ENTITY_PARSER.parse(script);
                if (predicate == null) yield null;
                yield entity -> {
                    if (entity instanceof ServerPlayerEntity)
                        return predicate.test(((ServerPlayerEntity) entity));
                    return false;
                };
            }
            default -> null;
        };
    }
}
