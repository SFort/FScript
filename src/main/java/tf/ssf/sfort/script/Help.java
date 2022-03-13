package tf.ssf.sfort.script;

import tf.ssf.sfort.script.util.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    static Triple<String, List<String>, String> dismantle (String s) {
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
        return new Triple<>(prefix, Arrays.stream(s.split(" ")).collect(Collectors.toList()), postfix);
    }

    static Map<String, String> recurseImported(Help help, Set<Help> dejavu){
        Map<String, String> out = new HashMap<>();
        Set<String> existing = new HashSet<>();
        recurseAcceptor(help, dejavu,s ->{
            final Triple<String, List<String>, String> triple = dismantle(s.getKey());
            List<String> names = triple.b;
            names.removeIf(n -> !existing.add(triple.a+n+triple.c));
            if (names.size()>0)
                out.put(triple.a+String.join(" ", names)+triple.c, s.getValue());
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

}
