package tf.ssf.sfort.script;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public interface PredicateProvider<T> {
    Predicate<T> getPredicate(String key, Set<Class<?>> dejavu);
    Predicate<T> getPredicate(String key, String arg, Set<Class<?>> dejavu);
    default Predicate<T> getPredicate(String key){
        return getPredicate(key, new HashSet<>());
    }
    default Predicate<T> getPredicate(String key, String arg){
        return getPredicate(key, arg, new HashSet<>());
    }
}
