package tf.ssf.sfort.script;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

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

    public Parameters() {
    }
}
