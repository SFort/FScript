package tf.ssf.sfort.script.instance;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.SimpleRegistry;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;
import tf.ssf.sfort.script.util.DefaultParsers;

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
		switch (in){
			case "hand": case "offhand": case "helm": case "chest": case "legs": case "boots" :
				return getLocalEmbed(in, ".:"+val);
			case "health": case "hp" : {
				final float arg = Float.parseFloat(val);
				return entity -> entity.getHealth()>=arg;
			}
			case "max_health": case "max_hp" : {
				final float arg = Float.parseFloat(val);
				return entity -> entity.getMaxHealth()>=arg;
			}
			case "armor" : {
				final float arg = Integer.parseInt(val);
				return entity -> entity.getArmor()>=arg;
			}
			case "effect" : {
				final StatusEffect arg = SimpleRegistry.STATUS_EFFECT.get(new Identifier(val));
				return entity -> entity.hasStatusEffect(arg);
			}
			case "attack" : {
				final int arg = Integer.parseInt(val);
				return entity ->{
					int a = entity.getLastAttackTime();
					return entity.age - a > arg || a == 0;
				};
			}
			case "stuck_arrow_count" : {
				final int arg = Integer.parseInt(val);
				return entity -> entity.getStuckArrowCount() > arg;
			}
			case "stinger_count" : {
				final int arg = Integer.parseInt(val);
				return entity -> entity.getStingerCount() > arg;
			}
			case "sideways_speed" : {
				final float arg = Float.parseFloat(val);
				return entity -> entity.sidewaysSpeed>=arg;
			}
			case "upward_speed" : {
				final float arg = Float.parseFloat(val);
				return entity -> entity.upwardSpeed>=arg;
			}
			case "forward_speed" : {
				final float arg = Float.parseFloat(val);
				return entity -> entity.forwardSpeed>=arg;
			}
			case "movement_speed" : {
				final float arg = Float.parseFloat(val);
				return entity -> entity.getMovementSpeed()>=arg;
			}
			default : return null;
		}
	}

	@Override
	public Predicate<T> getLocalPredicate(String in){
		switch (in) {
			case "full_hp": case "max_hp": case "full_health": case "max_health": case "is_full_hp": case "is_max_hp": case "is_full_health": case "is_max_health"
					: return entity -> entity.getHealth() == entity.getMaxHealth();
			case "blocking": case "is_blocking" : return LivingEntity::isBlocking;
			case "climbing": case "is_climbing" : return LivingEntity::isClimbing;
			case "using": case "is_using" : return LivingEntity::isUsingItem;
			case "fall_flying": case "is_fall_flying" : return LivingEntity::isFallFlying;
			default : return null;
		}
	}

	@Override
	public Predicate<T> getLocalEmbed(String in, String script){
		switch (in) {
			case "hand": case "offhand": case "helm": case "chest": case "legs": case "boots" :{
				final Predicate<ItemStack> predicate = DefaultParsers.ITEM_STACK_PARSER.parse(script);
				if (predicate == null) return null;
				switch (in) {
					case "hand" : return entity -> predicate.test(entity.getMainHandStack());
					case "offhand" : return entity -> predicate.test(entity.getOffHandStack());
					case "helm" : return entity -> predicate.test(entity.getEquippedStack(EquipmentSlot.HEAD));
					case "chest" : return entity -> predicate.test(entity.getEquippedStack(EquipmentSlot.CHEST));
					case "legs" : return entity -> predicate.test(entity.getEquippedStack(EquipmentSlot.LEGS));
					default : return entity -> predicate.test(entity.getEquippedStack(EquipmentSlot.FEET));
				}
			}
			case "player" : {
				final Predicate<PlayerEntity> predicate = DefaultParsers.PLAYER_ENTITY_PARSER.parse(script);
				if (predicate == null) return null;
				return entity -> entity instanceof PlayerEntity && predicate.test(((PlayerEntity) entity));
			}
			case "server_player" : {
				final Predicate<ServerPlayerEntity> predicate = DefaultParsers.SERVER_PLAYER_ENTITY_PARSER.parse(script);
				if (predicate == null) return null;
				return entity -> entity instanceof ServerPlayerEntity && predicate.test(((ServerPlayerEntity) entity));
			}
			default : return null;
		}
	}
}
