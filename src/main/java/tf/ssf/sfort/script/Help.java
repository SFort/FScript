package tf.ssf.sfort.script;

import java.util.HashSet;
import java.util.Set;

public interface Help {
    String getHelp();
    String getAllHelp(Set<Class<?>> dejavu);
    default String getAllHelp(){
        return getAllHelp(new HashSet<>());
    }
}
