package tf.ssf.sfort.script;

import oshi.util.tuples.Triplet;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public interface Help {

    default List<Help> getImported(){
        return new ArrayList<>();
    }
    Map<String, String> getHelp();
    static String formatHelp(Help help, Set<String> exclude){
        Map<String, String> in = recurseImported(help, new HashSet<>());
        StringBuilder out = new StringBuilder();
        int space = 8;
        for (String key :in.keySet())
            if (space<key.length())
                space = key.length();
        for (Map.Entry<String, String> entry :in.entrySet()){
            final String key = entry.getKey();
            if(exclude==null || !exclude.contains(key))
                out.append(String.format("\t%-"+(space+2)+"s- %s%n", key, entry.getValue()));
        }
        return out.toString();
    }

    static Triplet<String, List<String>, String> dismantle (String s) {
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
        return new Triplet<>(prefix, Arrays.stream(s.split(" ")).collect(Collectors.toList()), postfix);
    };
    static Map<String, String> recurseImported(Help help, Set<Help> dejavu){
        Map<String, String> out = new HashMap<>();
        Set<String> existing = new HashSet<>();
        recurseAcceptor(help, dejavu,s ->{
            final Triplet<String, List<String>, String> triple = dismantle(s.getKey());
            List<String> names = triple.getB();
            names.removeIf(n -> !existing.add(triple.getA()+n+triple.getC()));
            if (names.size()>0)
                out.put(triple.getA()+String.join(" ", names)+triple.getC(), s.getValue());
        });
        return out;
    }
    static void recurseAcceptor(Help help, Set<Help> dejavu, Consumer<Map.Entry<String, String>> acceptor){
        if (dejavu.add(help)) {
            for (Map.Entry<String, String> str : help.getHelp().entrySet())
                acceptor.accept(str);
            for (Help h : help.getImported())
                if(h != null)
                    for (Map.Entry<String, String> str : recurseImported(h, dejavu).entrySet())
                        acceptor.accept(str);
        }
    }

    @Deprecated(forRemoval = true)
    enum Parameter{
        DIMENSION("DimensionID"),
        ADVANCMENT("AdvancementID"),
        ITEM("ItemID"),
        ITEM_GROUP("ItemGroupID"),
        RARITY("RarityID"),
        BLOCK("BlockID"),
        BIOME("BiomeID"),
        BIOME_CATAGORY_NAME("BiomeCatagoryID"),
        ENTITY_TYPE("EntityTypeID"),
        ENCHANT("EnchantID"),
        ENCHANT_RARITY("EnchantRarityID"),
        ENCHANT_TARGET("EnchantTargetID"),
        PRECIPITATION("BiomePrecipitationID"),
        GAME_MODE("GameModeID"),
        GAME_MODE_NAME("GameModeID"),
        DOUBLE,
        FLOAT,
        INT,
        LONG,
        BYTE,
        SHORT,
        ;
        public final String name;
        Parameter(){
            name=this.name().toLowerCase();
        }
        Parameter(String name){
            this.name = name;
        }
        @Deprecated(forRemoval = true)
        public static Parameter byName(String name){
            return Arrays.stream(values()).filter(p -> p.name.equals(name)).findAny().orElse(null);
        }
        @Deprecated(forRemoval = true)
        Set<String> getParameters(){return Default.PARAMETERS.getParameters(name);}

        @Deprecated(forRemoval = true)
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
