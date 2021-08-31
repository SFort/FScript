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
import oshi.util.tuples.Triplet;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface Help {

    default Set<Help> getImported(){
        return new HashSet<>();
    }
    Map<String, Object> getHelp();
    static String formatHelp(Help help, Set<String> exclude){
        Map<String, Object> in = recurseImported(help, new HashSet<>());
        StringBuilder out = new StringBuilder();
        int space = 8;
        for (String key :in.keySet())
            if (space<key.length())
                space = key.length();
        for (Map.Entry<String, Object> entry :in.entrySet()){
            final String key = entry.getKey();
            if(exclude==null || !exclude.contains(key))
                out.append(String.format("\t%-"+(space+2)+"s- %s%n", key, entry.getValue()));
        }
        return out.toString();
    }
    static Map<String, Object> recurseImported(Help help, Set<Help> dejavu){
        Map<String, Object> out = new HashMap<>();
        final Function<String, Triplet<String, Set<String>, String>> dismantle = s -> {
            String prefix = "";
            String postfix = "";
            int colon = s.indexOf(':');
            if (colon != -1) {
                if (s.startsWith("~")) {
                    prefix = "~";
                    s = s.substring(1);
                    int i = s.indexOf('~');
                    if (i != -1) colon = i;
                }
                postfix = s.substring(colon);
                s = s.substring(0, colon);
            }
            return new Triplet<>(prefix, Arrays.stream(s.split(" ")).collect(Collectors.toSet()), postfix);
        };
        Set<String> existing = new HashSet<>();
        final Consumer<Map.Entry<String, Object>> addUnique = s ->{
            final Triplet<String, Set<String>, String> triple = dismantle.apply(s.getKey());
            Set<String> names = triple.getB();
            names.removeIf(n -> !existing.add(triple.getA()+n+triple.getC()));
            if (names.size()>0)
                out.put(triple.getA()+String.join(" ", names)+triple.getC(), s.getValue());
        };
        if (dejavu.add(help)) {
            for (Map.Entry<String, Object> str : help.getHelp().entrySet())
                addUnique.accept(str);
            for (Help h : help.getImported())
                if(h != null)
                for (Map.Entry<String, Object> str : recurseImported(h, dejavu).entrySet())
                    addUnique.accept(str);
        }
        return out;
    }
    enum Parameter{
        DIMENSION("DimensionID"){
            public Set<String> getParameters(){
                Set<String> out = new HashSet<>();
                out.add(World.OVERWORLD.getValue().toString());
                out.add(World.NETHER.getValue().toString());
                out.add(World.END.getValue().toString());
                return out;
            }
        },
        ADVANCMENT("AdvancementID"){
            public Set<String> getParameters(){
                try {
                    return MinecraftClient.getInstance().getServer().getAdvancementLoader().getAdvancements().stream().map(a -> a.getId().toString()).collect(Collectors.toSet());
                }catch (Exception ignore){}
                return super.getParameters();
            }
        },
        ITEM("ItemID"){
            public Set<String> getParameters(){
                return Registry.ITEM.getIds().stream().map(Identifier::toString).collect(Collectors.toSet());
            }
        },
        ITEM_GROUP("ItemGroupID"){
            public Set<String> getParameters(){
                return Arrays.stream(ItemGroup.GROUPS).map(ItemGroup::getName).collect(Collectors.toSet());
            }
        },
        RARITY("RarityID"){
            public Set<String> getParameters(){
                return Arrays.stream(Rarity.values()).map(Enum::name).collect(Collectors.toSet());
            }
        },
        BLOCK("BlockID"){
            public Set<String> getParameters(){
                return Registry.BLOCK.getIds().stream().map(Identifier::toString).collect(Collectors.toSet());
            }
        },
        BIOME("BiomeID"){
            public Set<String> getParameters(){
                try {
                    return MinecraftClient.getInstance().world.getRegistryManager().get(Registry.BIOME_KEY).getIds().stream().map(Identifier::toString).collect(Collectors.toSet());
                }catch (Exception ignore){}
                return super.getParameters();
            }
        },
        BIOME_CATAGORY_NAME("BiomeCatagoryID"){
            public Set<String> getParameters(){
                return Arrays.stream(Biome.Category.values()).map(Biome.Category::getName).collect(Collectors.toSet());
            }
        },
        ENTITY_TYPE("EntityTypeID"){
            public Set<String> getParameters(){
                return Registry.ENTITY_TYPE.getIds().stream().map(Identifier::toString).collect(Collectors.toSet());
            }
        },
        ENCHANT("EnchantID"){
            public Set<String> getParameters(){
                return Registry.ENCHANTMENT.getIds().stream().map(Identifier::toString).collect(Collectors.toSet());
            }
        },
        ENCHANT_RARITY("EnchantRarityID"){
            public Set<String> getParameters(){
                return Arrays.stream(Enchantment.Rarity.values()).map(Enum::name).collect(Collectors.toSet());
            }
        },
        ENCHANT_TARGET("EnchantTargetID"){
            public Set<String> getParameters(){
                return Registry.ENCHANTMENT.getIds().stream().map(Identifier::toString).collect(Collectors.toSet());
            }
        },
        PRECIPITATION("BiomePrecipitationID"){
            public Set<String> getParameters(){
                return Arrays.stream(Biome.Precipitation.values()).map(Biome.Precipitation::getName).collect(Collectors.toSet());
            }
        },
        GAME_MODE("GameModeID"){
            public Set<String> getParameters(){
                return Arrays.stream(GameMode.values()).map(Enum::name).collect(Collectors.toSet());
            }
        },
        GAME_MODE_NAME("GameModeID"){
            public Set<String> getParameters(){
                return Arrays.stream(EnchantmentTarget.values()).map(Enum::name).collect(Collectors.toSet());
            }
        },
        DOUBLE,
        FLOAT,
        INT,
        LONG,
        ;
        public final String name;
        Parameter(){
            name=this.name().toLowerCase();
        }
        Parameter(String name){
            this.name = name;
        }
        public static Parameter byName(String name){
            return Arrays.stream(values()).filter(p -> p.name.equals(name)).findAny().orElse(null);
        }
        Set<String> getParameters(){return new HashSet<>();};
        
        public static String intoString(){
            StringBuilder out = new StringBuilder();
            for (Parameter h : Parameter.values()) {
                out.append("\n\n").append(h.name).append("\n")
                        .append("======================================================================")
                        .append(":\n").append(String.join("\n", h.getParameters()));
            }
            return out.toString();
        }
    }
}
