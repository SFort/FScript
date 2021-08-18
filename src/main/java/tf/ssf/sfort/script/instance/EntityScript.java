package tf.ssf.sfort.script.instance;

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
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
			case "height" -> {
				float arg = Float.parseFloat(val);
				yield entity -> entity.getPos().y>=arg;
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
			default -> null;
		};
	}
	public Predicate<T> getLP(String in){
		return switch (in) {
			case "sprinting" -> Entity::isSprinting;
			case "in_lava" -> Entity::isInLava;
			case "on_fire" -> Entity::isOnFire;
			case "wet" -> Entity::isWet;
			case "fire_immune" -> Entity::isFireImmune;
			case "freezing" -> Entity::isFreezing;
			case "glowing" -> Entity::isGlowing;
			case "explosion_immune" -> Entity::isImmuneToExplosion;
			case "invisible" -> Entity::isInvisible;
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
		help.put("age:int","Minimum ticks the player must have existed");
		help.put("height:float","Minimum required player y height");
		help.put("local_difficulty:float","Minimum required regional/local difficulty");
		help.put("biome:","Required biome");
		help.put("sprinting","Require Sprinting");
		help.put("in_lava","Require being in lava");
		help.put("on_fire","Require being on fire");
		help.put("wet","Require being wet");
		help.put("fire_immune","Require being immune to fire");
		help.put("freezing","Require to be freezing");
		help.put("glowing","Require to be glowing");
		help.put("explosion_immune","Require being immune to explosions");
		help.put("invisible","Require being invisible");
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
		out = Stream.concat(out, getAllHelp().entrySet().stream());

		return out.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
}
