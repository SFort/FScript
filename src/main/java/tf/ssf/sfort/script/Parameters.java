package tf.ssf.sfort.script;

import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.*;
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
        map.put(key, supplier);
    }
    public Map<String, Supplier<Set<String>>> map = new HashMap<>();
    public Set<String> DimensionID;
    public Set<String> ItemID;
    public Set<String> ItemGroupID;
    public Set<String> RarityID;
    public Set<String> BlockID;
    public Set<String> BiomeCatagoryID;
    public Set<String> EntityTypeID;
    public Set<String> EnchantID;
    public Set<String> EnchantRarityID;
    public Set<String> EnchantTargetID;
    public Set<String> BiomePrecipitationID;
    public Set<String> GameModeID;
    public Set<String> GameModeNameID;
    public Parameters() {
        map.put("AdvancementID", () -> {
            try {
                return MinecraftClient.getInstance().getServer().getAdvancementLoader().getAdvancements().stream().map(a -> a.getId().toString()).collect(Collectors.toSet());
            }catch (Exception ignore){}
            return new HashSet<>();
        });

        map.put("BiomeID", () ->{
            try {
                return MinecraftClient.getInstance().world.getRegistryManager().get(Registry.BIOME_KEY).getIds().stream().map(Identifier::toString).collect(Collectors.toSet());
            }catch (Exception ignore){}
            return new HashSet<>();
        });

        map.put("DimensionID", () -> DimensionID);
        DimensionID = new HashSet<>();
        DimensionID.add(World.OVERWORLD.getValue().toString());
        DimensionID.add(World.NETHER.getValue().toString());
        DimensionID.add(World.END.getValue().toString());

        map.put("ItemID", () -> ItemID);
        ItemID = Registry.ITEM.getIds().stream().map(Identifier::toString).collect(Collectors.toSet());

        map.put("ItemGroupID", () -> ItemGroupID);
        ItemGroupID = Arrays.stream(ItemGroup.GROUPS).map(ItemGroup::getName).collect(Collectors.toSet());

        map.put("RarityID", () -> RarityID);
        RarityID = Arrays.stream(Rarity.values()).map(Enum::name).collect(Collectors.toSet());

        map.put("BlockID", () -> BlockID);
        BlockID = Registry.BLOCK.getIds().stream().map(Identifier::toString).collect(Collectors.toSet());

        map.put("BiomeCatagoryID", () -> BiomeCatagoryID);
        BiomeCatagoryID = Arrays.stream(Biome.Category.values()).map(Biome.Category::getName).collect(Collectors.toSet());

        map.put("EntityTypeID", () -> EntityTypeID);
        EntityTypeID = Registry.ENTITY_TYPE.getIds().stream().map(Identifier::toString).collect(Collectors.toSet());

        map.put("EnchantID", () -> EnchantID);
        EnchantID = Registry.ENCHANTMENT.getIds().stream().map(Identifier::toString).collect(Collectors.toSet());

        map.put("EnchantRarityID", () -> EnchantRarityID);
        EnchantRarityID = Arrays.stream(Enchantment.Rarity.values()).map(Enum::name).collect(Collectors.toSet());

        map.put("EnchantTargetID", () -> EnchantTargetID);
        EnchantTargetID = Registry.ENCHANTMENT.getIds().stream().map(Identifier::toString).collect(Collectors.toSet());

        map.put("BiomePrecipitationID", () -> BiomePrecipitationID);
        BiomePrecipitationID = Arrays.stream(Biome.Precipitation.values()).map(Biome.Precipitation::getName).collect(Collectors.toSet());

        map.put("GameModeID", () -> GameModeID);
        GameModeID = Arrays.stream(GameMode.values()).map(Enum::name).collect(Collectors.toSet());

        map.put("GameModeNameID", () -> GameModeNameID);
        GameModeNameID = Arrays.stream(EnchantmentTarget.values()).map(Enum::name).collect(Collectors.toSet());
    }
}
