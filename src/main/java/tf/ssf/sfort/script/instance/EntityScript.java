package tf.ssf.sfort.script.instance;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import tf.ssf.sfort.script.Default;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;

import java.util.*;
import java.util.function.Predicate;

public class EntityScript<T extends Entity> implements PredicateProvider<T>, Help {
	public Predicate<T> getLP(String in, String val){
		return switch (in){
			case "x" -> {
				final double arg = Double.parseDouble(val);
				yield entity -> entity.getX()>=arg;
			}
			case "y" -> {
				final double arg = Double.parseDouble(val);
				yield entity -> entity.getY()>=arg;
			}
			case "z" -> {
				final double arg = Double.parseDouble(val);
				yield entity -> entity.getZ()>=arg;
			}
			case "age" -> {
				final int arg = Integer.parseInt(val);
				yield entity -> entity.age>arg;
			}
			case "local_difficulty" -> {
				final float arg = Float.parseFloat(val);
				yield entity -> entity.world.getLocalDifficulty(entity.getBlockPos()).isHarderThan(arg);
			}
			case "biome" -> {
				final Identifier arg = new Identifier(val);
				yield entity -> entity.world.getBiomeKey(entity.getBlockPos()).map(x->x.getValue().equals(arg)).orElse(false);
			}
			case "air" -> {
				final int arg = Integer.parseInt(val);
				yield entity -> entity.getAir()>arg;
			}
			case "max_air" -> {
				final int arg = Integer.parseInt(val);
				yield entity -> entity.getMaxAir()>arg;
			}
			case "frozen_ticks" -> {
				final int arg = Integer.parseInt(val);
				yield entity -> entity.getFrozenTicks()>arg;
			}
			case "height" -> {
				final float arg = Float.parseFloat(val);
				yield entity -> entity.getHeight()>arg;
			}
			case "width" -> {
				final float arg = Float.parseFloat(val);
				yield entity -> entity.getWidth()>arg;
			}
			case "in_block" -> {
				final Block arg = Registry.BLOCK.get(new Identifier(val));;
				yield entity -> entity.world.getBlockState(entity.getBlockPos()).isOf(arg);
			}
			case "type" -> {
				final EntityType<?> arg = Registry.ENTITY_TYPE.get(new Identifier(val));
				yield entity -> entity.getType().equals(arg);
			}
			default -> null;
		};
	}
	public Predicate<T> getLP(String in){
		return switch (in) {
			case "full_air", "max_air" -> entity -> entity.getAir() == entity.getMaxAir();
			case "sprinting", "is_sprinting" -> Entity::isSprinting;
			case "in_lava", "is_in_lava" -> Entity::isInLava;
			case "on_fire", "is_on_fire" -> Entity::isOnFire;
			case "wet", "is_wet" -> Entity::isWet;
			case "fire_immune", "is_fire_immune" -> Entity::isFireImmune;
			case "freezing", "is_freezing" -> Entity::isFreezing;
			case "glowing", "is_glowing" -> Entity::isGlowing;
			case "explosion_immune", "is_explosion_immune" -> Entity::isImmuneToExplosion;
			case "invisible", "is_invisible" -> Entity::isInvisible;
			case "on_ground", "is_on_ground" -> Entity::isOnGround;
			case "silent", "is_silent" -> Entity::isSilent;
			case "has_no_gravity" -> Entity::hasNoGravity;
			case "inside_wall", "is_inside_wall" -> Entity::isInsideWall;
			case "touching_water","is_touching_water" -> Entity::isTouchingWater;
			case "touching_water_or_rain", "is_touching_water_or_rain" -> Entity::isTouchingWaterOrRain;
			case "submerged_in_water", "is_submerged_in_water" -> Entity::isSubmergedInWater;
			case "has_vehicle" -> Entity::hasVehicle;
			case "has_passengers" ->Entity::hasPassengers;
			case "has_player_rider" ->Entity::hasPlayerRider;
			case "is_sneaking","sneaking","is_sneaky","sneaky" -> Entity::isSneaky;
			case "swimming", "is_swimming" -> Entity::isSwimming;
			default -> null;
		};
	}

	//==================================================================================================================

	@Override
	public Predicate<T> getPredicate(String in, String val, Set<Class<?>> dejavu) {
		{
			final Predicate<T> out = getLP(in, val);
			if (out != null) return out;
		}
		if (dejavu.add(WorldScript.class)){
			final Predicate<World> out = Default.WORLD.getPredicate(in, val, dejavu);
			if (out !=null) return entity -> out.test(entity.world);
		}
		if (dejavu.add(BiomeScript.class)){
			final Predicate<Biome> out = Default.BIOME.getPredicate(in, val, dejavu);
			if (out !=null) return entity -> out.test(entity.world.getBiome(entity.getBlockPos()));
		}
		if (dejavu.add(ChunkScript.class)){
			final Predicate<Chunk> out = Default.CHUNK.getPredicate(in, val, dejavu);
			if (out !=null) return entity -> out.test(entity.world.getWorldChunk(entity.getBlockPos()));
		}
		return null;
	}
	@Override
	public Predicate<T> getPredicate(String in, Set<Class<?>> dejavu){
		{
			final Predicate<T> out = getLP(in);
			if (out != null) return out;
		}
		if (dejavu.add(WorldScript.class)){
			final Predicate<World> out = Default.WORLD.getPredicate(in, dejavu);
			if (out !=null) return entity -> out.test(entity.world);
		}
		if (dejavu.add(BiomeScript.class)){
			final Predicate<Biome> out = Default.BIOME.getPredicate(in, dejavu);
			if (out !=null) return entity -> out.test(entity.world.getBiome(entity.getBlockPos()));
		}
		if (dejavu.add(ChunkScript.class)){
			final Predicate<Chunk> out = Default.CHUNK.getPredicate(in, dejavu);
			if (out !=null) return entity -> out.test(entity.world.getWorldChunk(entity.getBlockPos()));
		}
		return null;
	}

	//==================================================================================================================

	@Override
	public Map<String, Object> getHelp(){
		return help;
	}
	@Override
	public Set<Help> getImported(){
		return extend_help;
	}

	public static final Map<String, Object> help = new HashMap<>();
	public static final Set<Help> extend_help = new LinkedHashSet<>();
	static {
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

		extend_help.add(Default.WORLD);
		extend_help.add(Default.BIOME);
		extend_help.add(Default.CHUNK);
	}
}
