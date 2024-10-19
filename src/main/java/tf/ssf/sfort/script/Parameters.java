package tf.ssf.sfort.script;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Parameters {
    public Supplier<Set<String>> getSupplier(String key){
        if (!map.containsKey(key)) return HashSet::new;
        return map.get(key);
    }
    public Set<String> getParameters(String key){
        return getSupplier(key).get();
    }
    public void addParameterSupplier(String key, Supplier<Set<String>> supplier){
        if (map.containsKey(key)){
            Supplier<Set<String>> oldSup = map.get(key);
            map.put(key, () -> new HashSet<String>(){{addAll(oldSup.get());addAll(supplier.get());}});
        } else{
            map.put(key, supplier);
        }
    }
    public Map<String, Supplier<Set<String>>> map = new HashMap<>();
    public Set<String> DimensionID = new HashSet<>();
    public Set<String> ItemID = new HashSet<>();
    public Set<String> RarityID = new HashSet<>();
    public Set<String> BlockID = new HashSet<>();
    public Set<String> EntityTypeID = new HashSet<>();
    public Set<String> BiomePrecipitationID = new HashSet<>();
    public Set<String> GameModeID = new HashSet<>();
    public Set<String> GameModeNameID = new HashSet<>();
    public Set<String> DamageSourceID = new HashSet<>();
    public Set<String> EffectID = new HashSet<>();
    public Parameters() {
        map.put("AdvancementID", () -> {
            try {
                return MinecraftClient.getInstance().getServer().getAdvancementLoader().getAdvancements().stream().map(a -> a.id().toString()).collect(Collectors.toSet());
            }catch (Exception ignore){}
            return new HashSet<>();
        });

        map.put("BiomeID", () ->{
            try {
                return MinecraftClient.getInstance().world.getRegistryManager().get(Registries.BIOME_SOURCE.getKey()).getIds().stream().map(Identifier::toString).collect(Collectors.toSet());
            }catch (Exception ignore){}
            return new HashSet<>();
        });

        map.put("DamageSourceType", () -> {
            try {
                return MinecraftClient.getInstance().world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).getIds().stream().map(Identifier::toString).collect(Collectors.toSet());
            }catch (Exception ignore){}
            return new HashSet<>();
        });
        map.put("EnchantID", () -> {
            try {
                return MinecraftClient.getInstance().world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getIds().stream().map(Identifier::toString).collect(Collectors.toSet());
            } catch (Exception ignore){}
            return new HashSet<>();
        });

        map.put("DimensionID", () -> DimensionID);
        DimensionID.add(World.OVERWORLD.getValue().toString());
        DimensionID.add(World.NETHER.getValue().toString());
        DimensionID.add(World.END.getValue().toString());

        map.put("ItemID", () -> ItemID);
        ItemID.addAll(Registries.ITEM.getIds().stream().map(Identifier::toString).collect(Collectors.toSet()));

        map.put("RarityID", () -> RarityID);
        RarityID.addAll(Arrays.stream(Rarity.values()).map(Enum::name).collect(Collectors.toSet()));

        map.put("BlockID", () -> BlockID);
        BlockID.addAll(Registries.BLOCK.getIds().stream().map(Identifier::toString).collect(Collectors.toSet()));

        map.put("EntityTypeID", () -> EntityTypeID);
        EntityTypeID.addAll(Registries.ENTITY_TYPE.getIds().stream().map(Identifier::toString).collect(Collectors.toSet()));

        map.put("BiomePrecipitationID", () -> BiomePrecipitationID);
        BiomePrecipitationID.addAll(Arrays.stream(Biome.Precipitation.values()).map(Biome.Precipitation::name).collect(Collectors.toSet()));

        map.put("GameModeID", () -> GameModeID);
        GameModeID.addAll(Arrays.stream(GameMode.values()).map(gameMode -> Integer.toString(gameMode.getId())).collect(Collectors.toSet()));

        map.put("GameModeNameID", () -> GameModeNameID);
        GameModeNameID.addAll(Arrays.stream(GameMode.values()).map(GameMode::getName).collect(Collectors.toSet()));

        map.put("DamageSourceID", () -> DamageSourceID);

        for (Field field : DamageSource.class.getDeclaredFields()) {
            if ((field.getModifiers()&Opcodes.ACC_STATIC) != 0 && field.getType().isAssignableFrom(DamageSource.class)) {
                try {
                    Object ds = field.get(null);
                    if (ds instanceof DamageSource) {
                        DamageSourceID.add(((DamageSource) ds).getName());
                    }
                } catch (IllegalAccessException ignore) {
                }
            }
        }

        map.put("EffectID", () -> EffectID);
        EffectID.addAll(Registries.STATUS_EFFECT.getIds().stream().map(Identifier::toString).collect(Collectors.toSet()));

    }
}
