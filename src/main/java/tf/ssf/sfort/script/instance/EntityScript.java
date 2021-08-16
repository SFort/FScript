package tf.ssf.sfort.script.instance;

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import tf.ssf.sfort.script.Default;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.Help;

import java.util.Set;
import java.util.function.Predicate;

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
	@Override
	public String getHelp(){
		return
			String.format("\t%-20s%-70s%s%n","age","- Minimum ticks the player must have existed","int")+
			String.format("\t%-20s%-70s%s%n","height","- Minimum required player y height","float")+
			String.format("\t%-20s%-70s%s%n","local_difficulty","- Minimum required regional/local difficulty","float")+
			String.format("\t%-20s%-70s%s%n","biome","- Required biome","BiomeID")+
			String.format("\t%-20s%s%n","sprinting","- Require Sprinting")+
			String.format("\t%-20s%s%n","in_lava","- Require being in lava")+
			String.format("\t%-20s%s%n","on_fire","- Require being on fire")+
			String.format("\t%-20s%s%n","wet","- Require being wet")+
			String.format("\t%-20s%s%n","fire_immune","- Require being immune to fire")+
            String.format("\t%-20s%s%n","freezing","- Require to be freezing")+
            String.format("\t%-20s%s%n","glowing","- Require to be glowing")+
            String.format("\t%-20s%s%n","explosion_immune","- Require being immune to explosions")+
            String.format("\t%-20s%s%n","invisible","- Require being invisible")
		;
	}
	@Override
	public String getAllHelp(Set<Class<?>> dejavu){
		return (dejavu.add(WorldScript.class)?Default.WORLD.getAllHelp(dejavu):"")+
				(dejavu.add(BiomeScript.class)?Default.BIOME.getAllHelp(dejavu):"")+
				(dejavu.add(ChunkScript.class)?Default.CHUNK.getAllHelp(dejavu):"")+
				getHelp();
	}
}
