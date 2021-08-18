package tf.ssf.sfort.script;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface Help {
    Map<String, String> getHelp();
    Map<String, String> getAllHelp(Set<Class<?>> dejavu);
    default Map<String, String> getAllHelp(){
        return getAllHelp(new HashSet<>());
    }
    static String formatHelp(Map<String, String> in, Set<String> exclude){
        StringBuilder out = new StringBuilder();
        for (String key :in.keySet()){
            if(exclude==null || !exclude.contains(key))
                out.append(String.format("\t%-20s- %s%n", key, in.get(key)));
        }
        return out.toString();
    }
}
