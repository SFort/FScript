package tf.ssf.sfort.script;

import java.util.*;
import java.util.function.Predicate;

public interface PredicateProviderExtendable<T> extends PredicateProvider<T> {
    default void addProvider(PredicateProvider<T> predicateProvider){
        addProvider(predicateProvider, 0);
    }
    void addProvider(PredicateProvider<T> predicateProvider, int priority);
    List<PredicateProvider<T>> getProviders();
    default Predicate<T> getPredicate(String key, Set<Class<?>> dejavu){
        for (PredicateProvider<T> provider : getProviders()){
            if (dejavu.add(provider.getClass())) {
                Predicate<T> ret = provider.getPredicate(key, dejavu);
                if (ret != null) return ret;
            }
        }
        return PredicateProvider.super.getPredicate(key, dejavu);
    }
    default Predicate<T> getPredicate(String key, String arg, Set<Class<?>> dejavu){
        for (PredicateProvider<T> provider : getProviders()){
            if (dejavu.add(provider.getClass())) {
                Predicate<T> ret = provider.getPredicate(key, arg, dejavu);
                if (ret != null) return ret;
            }
        }
        return PredicateProvider.super.getPredicate(key, arg, dejavu);
    }
    default Predicate<T> getEmbed(String key, String script, Set<Class<?>> dejavu){
        for (PredicateProvider<T> provider : getProviders()){
            Predicate<T> ret = provider.getPredicate(key, dejavu);
            if (ret != null) return ret;
        }
        return PredicateProvider.super.getEmbed(key, script, dejavu);
    }
    default Predicate<T> getEmbed(String key, String arg, String script, Set<Class<?>> dejavu){
        for (PredicateProvider<T> provider : getProviders()){
            Predicate<T> ret = provider.getPredicate(key, dejavu);
            if (ret != null) return ret;
        }
        return PredicateProvider.super.getEmbed(key, arg, script, dejavu);
    }
}
