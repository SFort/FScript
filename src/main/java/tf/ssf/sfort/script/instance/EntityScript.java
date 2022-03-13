package tf.ssf.sfort.script.instance;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;
import tf.ssf.sfort.script.util.DefaultParsers;

import java.util.function.Predicate;

public class EntityScript<T extends Entity> extends AbstractExtendablePredicateProvider<T> {

	public EntityScript() {
		help.put("air:int", "Minimum required air");
		help.put("max_air:int", "Minimum required max air");
		help.put("frozen_ticks:int", "Minimum ticks the entity must have been freezing for");
		help.put("height:int", "Minimum required height");
		help.put("width:int", "Minimum required width");
		help.put("age:int","Minimum ticks the entity must have existed");
		help.put("x:double","Minimum required entity x");
		help.put("y:double","Minimum required entity y height");
		help.put("z:double","Minimum required entity z");
		help.put("local_difficulty:float","Minimum required regional/local difficulty");
		help.put("in_block:BlockID", "Require being in specified block");
		help.put("biome:BiomeID","Required biome");
		help.put("type:EntityTypeID","Required entity type");
		help.put("sprinting is_sprinting","Require Sprinting");
		help.put("in_lava is_in_lava","Require being in lava");
		help.put("on_fire is_on_fire","Require being on fire");
		help.put("wet is_wet","Require being wet");
		help.put("fire_immune is_fire_immune","Require being immune to fire");
		help.put("freezing is_freezing","Require to be freezing");
		help.put("glowing is_glowing","Require to be glowing");
		help.put("explosion_immune is_explosion_immune","Require being immune to explosions");
		help.put("invisible is_invisible","Require being invisible");
		help.put("on_ground is_on_ground", "Require being on ground");
		help.put("silent is_silent", "Require being silent");
		help.put("has_no_gravity", "Require having no gravity");
		help.put("inside_wall is_inside_wall", "Requite being inside a solid block");
		help.put("touching_water is_touching_water", "Require touching water");
		help.put("touching_water_or_rain is_touching_water_or_rain", "Require touching water or rain");
		help.put("submerged_in_water is_submerged_in_water", "Require being submerged in water");
		help.put("has_vehicle", "Require having a vehicle");
		help.put("has_passengers", "Require being a vehicle");
		help.put("has_player_rider", "Require being a vehicle to a player");
		help.put("sneaky is_sneaking sneaking is_sneaky", "Require sneaking");
		help.put("swimming is_swimming", "Require swimming");
		help.put("full_air max_air", "Require having full air");
		help.put("~living:LIVING_ENTITY", "Require a living entity");
		help.put("~player:PLAYER_ENTITY", "Require a player entity");
		help.put("~server_player:SERVER_PLAYER_ENTITY", "Require a server player entity");
	}

	@Override
	public Predicate<T> getLocalPredicate(String in, String val){
		switch (in){
			case "x": {
				final double arg = Double.parseDouble(val);
				return entity -> entity.getX() >= arg;
			}
			case "y": {
				final double arg = Double.parseDouble(val);
				return entity -> entity.getY() >= arg;
			}
			case "z" : {
				final double arg = Double.parseDouble(val);
				return entity -> entity.getZ()>=arg;
			}
			case "age" : {
				final int arg = Integer.parseInt(val);
				return entity -> entity.age>arg;
			}
			case "local_difficulty" : {
				final float arg = Float.parseFloat(val);
				return entity -> entity.world.getLocalDifficulty(entity.getBlockPos()).isHarderThan(arg);
			}
			case "biome" : {
				final Identifier arg = new Identifier(val);
				return entity -> entity.world.getBiomeKey(entity.getBlockPos()).map(x->x.getValue().equals(arg)).orElse(false);
			}
			case "air" : {
				final int arg = Integer.parseInt(val);
				return entity -> entity.getAir()>arg;
			}
			case "max_air" : {
				final int arg = Integer.parseInt(val);
				return entity -> entity.getMaxAir()>arg;
			}
			case "frozen_ticks" : {
				final int arg = Integer.parseInt(val);
				return entity -> entity.getFrozenTicks()>arg;
			}
			case "height" : {
				final float arg = Float.parseFloat(val);
				return entity -> entity.getHeight()>arg;
			}
			case "width" : {
				final float arg = Float.parseFloat(val);
				return entity -> entity.getWidth()>arg;
			}
			case "in_block" : {
				final Block arg = Registry.BLOCK.get(new Identifier(val));
				return entity -> entity.world.getBlockState(entity.getBlockPos()).isOf(arg);
			}
			case "type" : {
				final EntityType<?> arg = Registry.ENTITY_TYPE.get(new Identifier(val));
				return entity -> entity.getType().equals(arg);
			}
			default: return null;
		}
	}
	@Override
	public Predicate<T> getLocalPredicate(String in){
		switch (in) {
			case "full_air": case "max_air" : return entity -> entity.getAir() == entity.getMaxAir();
			case "sprinting": case "is_sprinting" : return Entity::isSprinting;
			case "in_lava": case "is_in_lava" : return Entity::isInLava;
			case "on_fire": case "is_on_fire" : return Entity::isOnFire;
			case "wet": case "is_wet" : return Entity::isWet;
			case "fire_immune": case "is_fire_immune" : return Entity::isFireImmune;
			case "freezing": case "is_freezing" : return Entity::isFreezing;
			case "glowing": case "is_glowing" : return Entity::isGlowing;
			case "explosion_immune": case "is_explosion_immune" : return Entity::isImmuneToExplosion;
			case "invisible": case "is_invisible" : return Entity::isInvisible;
			case "on_ground": case "is_on_ground" : return Entity::isOnGround;
			case "silent": case "is_silent" : return Entity::isSilent;
			case "has_no_gravity" : return Entity::hasNoGravity;
			case "inside_wall": case "is_inside_wall" : return Entity::isInsideWall;
			case "touching_water": case "is_touching_water" : return Entity::isTouchingWater;
			case "touching_water_or_rain": case "is_touching_water_or_rain" : return Entity::isTouchingWaterOrRain;
			case "submerged_in_water": case "is_submerged_in_water" : return Entity::isSubmergedInWater;
			case "has_vehicle" : return Entity::hasVehicle;
			case "has_passengers" : return Entity::hasPassengers;
			case "has_player_rider" : return Entity::hasPlayerRider;
			case "is_sneaking": case "sneaking": case "is_sneaky": case "sneaky" : return Entity::isSneaky;
			case "swimming": case "is_swimming" : return Entity::isSwimming;
			default : return null;
		}
	}
	@Override
	public Predicate<T> getLocalEmbed(String in, String script){
		switch (in){
			case "living" : {
				final Predicate<LivingEntity> predicate = DefaultParsers.LIVING_ENTITY_PARSER.parse(script);
				if (predicate == null) return null;
				return entity -> {
					if (entity instanceof LivingEntity)
						return predicate.test(((LivingEntity) entity));
					return false;
				};
			}
			case "player" : {
				final Predicate<PlayerEntity> predicate = DefaultParsers.PLAYER_ENTITY_PARSER.parse(script);
				if (predicate == null) return null;
				return entity -> {
					if (entity instanceof PlayerEntity)
						return predicate.test(((PlayerEntity) entity));
					return false;
				};
			}
			case "server_player" : {
				final Predicate<ServerPlayerEntity> predicate = DefaultParsers.SERVER_PLAYER_ENTITY_PARSER.parse(script);
				if (predicate == null) return null;
				return entity -> {
					if (entity instanceof ServerPlayerEntity)
						return predicate.test(((ServerPlayerEntity) entity));
					return false;
				};
			}
			default : return null;
		}
	}

}
