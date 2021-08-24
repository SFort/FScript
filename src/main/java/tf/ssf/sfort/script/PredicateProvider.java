package tf.ssf.sfort.script;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public interface PredicateProvider<T> {
    default Predicate<T> getPredicate(String key, Set<Class<?>> dejavu){
        return null;
    }
    default Predicate<T> getPredicate(String key, String arg, Set<Class<?>> dejavu){
        return null;
    }
    default Predicate<T> getEmbed(String key, String script){
        return null;
    }
    default Predicate<T> getEmbed(String key, String arg, String script){
        return null;
    }
    default Predicate<T> getPredicate(String key){
        return getPredicate(key, new HashSet<>());
    }
    default Predicate<T> getPredicate(String key, String arg){
        return getPredicate(key, arg, new HashSet<>());
    }
}
