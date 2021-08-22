package tf.ssf.sfort.script.instance;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import tf.ssf.sfort.script.Default;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.Help;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityScript<T extends Entity> implements PredicateProvider<T>, Help {
	public Predicate<T> getLP(String in, String val){
		return switch (in){
			case "x" -> {
				double arg = Double.parseDouble(val);
				yield entity -> entity.getX()>=arg;
			}
			case "y" -> {
				double arg = Double.parseDouble(val);
				yield entity -> entity.getY()>=arg;
			}
			case "z" -> {
				double arg = Double.parseDouble(val);
				yield entity -> entity.getZ()>=arg;
			}
			case "age" -> {
				int arg = Integer.parseInt(val);
				yield entity -> entity.age>arg;
			}
			case "local_difficulty" -> {
				float arg = Float.parseFloat(val);
				yield entity -> entity.world.getLocalDifficulty(entity.getBlockPos()).isHarderThan(arg);
			}
			case "biome" -> {
				Identifier arg = new Identifier(val);
				yield entity -> entity.world.getBiomeKey(entity.getBlockPos()).map(x->x.getValue().equals(arg)).orElse(false);
			}
			case "air" -> {
				int arg = Integer.parseInt(val);
				yield entity -> entity.getAir()>arg;
			}
			case "max_air" -> {
				int arg = Integer.parseInt(val);
				yield entity -> entity.getMaxAir()>arg;
			}
			case "frozen_ticks" -> {
				int arg = Integer.parseInt(val);
				yield entity -> entity.getFrozenTicks()>arg;
			}
			case "height" -> {
				float arg = Float.parseFloat(val);
				yield entity -> entity.getHeight()>arg;
			}
			case "width" -> {
				float arg = Float.parseFloat(val);
				yield entity -> entity.getWidth()>arg;
			}
			case "in_block" -> {
				Block arg = Registry.BLOCK.get(new Identifier(val));;
				yield entity -> entity.world.getBlockState(entity.getBlockPos()).isOf(arg);
			}
			default -> null;
		};
	}
	public Predicate<T> getLP(String in){
		return switch (in) {
			case "full_air" -> entity -> entity.getAir() == entity.getMaxAir();
			case "sprinting" -> Entity::isSprinting;
			case "in_lava" -> Entity::isInLava;
			case "on_fire" -> Entity::isOnFire;
			case "wet" -> Entity::isWet;
			case "fire_immune" -> Entity::isFireImmune;
			case "freezing" -> Entity::isFreezing;
			case "glowing" -> Entity::isGlowing;
			case "explosion_immune" -> Entity::isImmuneToExplosion;
			case "invisible" -> Entity::isInvisible;
			case "on_ground" -> Entity::isOnGround;
			case "is_silent" -> Entity::isSilent;
			case "has_no_gravity" -> Entity::hasNoGravity;
			case "is_inside_wall" -> Entity::isInsideWall;
			case "is_touching_water" -> Entity::isTouchingWater;
			case "is_touching_water_or_rain" -> Entity::isTouchingWaterOrRain;
			case "is_submerged_in_water" -> Entity::isSubmergedInWater;
			case "has_vehicle" -> Entity::hasVehicle;
			case "has_passengers" ->Entity::hasPassengers;
			case "has_player_rider" ->Entity::hasPlayerRider;
			case "sneaking" -> Entity::isSneaky;
			case "swimming" -> Entity::isSwimming;
			default -> null;
		};
	}
	@Override
	public Predicate<T> getPredicate(String in, String val, Set<Class<?>> dejavu) {
		{
			Predicate<T> out = getLP(in, val);
			if (out != null) return out;
		}
		if (dejavu.add(WorldScript.class)){
			Predicate<World> out = Default.WORLD.getPredicate(in, val, dejavu);
			if (out !=null) return entity -> out.test(entity.world);
		}
		if (dejavu.add(BiomeScript.class)){
			Predicate<Biome> out = Default.BIOME.getPredicate(in, val, dejavu);
			if (out !=null) return entity -> out.test(entity.world.getBiome(entity.getBlockPos()));
		}
		if (dejavu.add(ChunkScript.class)){
			Predicate<Chunk> out = Default.CHUNK.getPredicate(in, val, dejavu);
			if (out !=null) return entity -> out.test(entity.world.getWorldChunk(entity.getBlockPos()));
		}
		return null;
	}
	@Override
	public Predicate<T> getPredicate(String in, Set<Class<?>> dejavu){
		{
			Predicate<T> out = getLP(in);
			if (out != null) return out;
		}
		if (dejavu.add(WorldScript.class)){
			Predicate<World> out = Default.WORLD.getPredicate(in, dejavu);
			if (out !=null) return entity -> out.test(entity.world);
		}
		if (dejavu.add(BiomeScript.class)){
			Predicate<Biome> out = Default.BIOME.getPredicate(in, dejavu);
			if (out !=null) return entity -> out.test(entity.world.getBiome(entity.getBlockPos()));
		}
		if (dejavu.add(ChunkScript.class)){
			Predicate<Chunk> out = Default.CHUNK.getPredicate(in, dejavu);
			if (out !=null) return entity -> out.test(entity.world.getWorldChunk(entity.getBlockPos()));
		}
		return null;
	}
	public static final Map<String, String> help = new HashMap<>();
	static {
		help.put("air:int", "Minimum required air");
		help.put("max_air:int", "Minimum required max air");
		help.put("frozen_ticks:int", "Minimum ticks the entity must have been freezing for");
		help.put("height:int", "Minimum required height");
		help.put("width:int", "Minimum required width");
		help.put("age:int","Minimum ticks the entity must have existed");
		help.put("X:double","Minimum required entity x");
		help.put("Y:double","Minimum required entity y height");
		help.put("Z:double","Minimum required entity z");
		help.put("local_difficulty:float","Minimum required regional/local difficulty");
		help.put("in_block:BlockID", "Require being in specified block");
		help.put("biome:BiomeID","Required biome");
		help.put("sprinting","Require Sprinting");
		help.put("in_lava","Require being in lava");
		help.put("on_fire","Require being on fire");
		help.put("wet","Require being wet");
		help.put("fire_immune","Require being immune to fire");
		help.put("freezing","Require to be freezing");
		help.put("glowing","Require to be glowing");
		help.put("explosion_immune","Require being immune to explosions");
		help.put("invisible","Require being invisible");
		help.put("on_ground", "Require being on ground");
		help.put( "is_silent", "Require being silent");
		help.put( "has_no_gravity", "Require having no gravity");
		help.put( "is_inside_wall", "Requite being inside a solid block");
		help.put( "is_touching_water", "Require touching water");
		help.put( "is_touching_water_or_rain", "Require touching water or rain");
		help.put( "is_submerged_in_water", "Require being submerged in water");
		help.put( "has_vehicle", "Require having a vehicle");
		help.put( "has_passengers", "Require being a vehicle");
		help.put( "has_player_rider", "Require being a vehicle to a player");
		help.put( "sneaking", "Require sneaking");
		help.put( "swimming", "Require swimming");
		help.put("full_air", "Require having full air");
	}
	@Override
	public Map<String, String> getHelp(){
		return help;
	}
	@Override
	public Map<String, String> getAllHelp(Set<Class<?>> dejavu){
		Stream<Map.Entry<String, String>> out = new HashMap<String, String>().entrySet().stream();
		if (dejavu.add(WorldScript.class)) out = Stream.concat(out, Default.WORLD.getAllHelp(dejavu).entrySet().stream());
		if (dejavu.add(BiomeScript.class)) out = Stream.concat(out, Default.BIOME.getAllHelp(dejavu).entrySet().stream());
		if (dejavu.add(ChunkScript.class)) out = Stream.concat(out, Default.CHUNK.getAllHelp(dejavu).entrySet().stream());
		out = Stream.concat(out, getHelp().entrySet().stream());

		return out.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
}
