package tf.ssf.sfort.script;

import java.util.HashMap;
import java.util.Map;

public class Default {
    public static final Parameters PARAMETERS = new Parameters();

    protected static final Map<String, PredicateProvider<?>> defaults = new HashMap<>();
    public static Map<String, PredicateProvider<?>> getDefaultMap(){
        return defaults;
    }

}
