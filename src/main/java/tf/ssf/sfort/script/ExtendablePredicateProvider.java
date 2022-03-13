package tf.ssf.sfort.script;

import tf.ssf.sfort.script.util.AdaptableHelpPredicateProvider;
import tf.ssf.sfort.script.util.AdaptablePredicateProvider;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public interface ExtendablePredicateProvider<T> extends PredicateProvider<T> {
    default <S> void addProvider(PredicateProvider<S> predicateProvider, Function<Predicate<S>, Predicate<T>> adapter){
        addProvider(predicateProvider, adapter, 0);
    }
    default void addProvider(PredicateProvider<T> predicateProvider){
        addProvider(predicateProvider, 0);
    }

    default <S> void addProvider(PredicateProvider<S> predicateProvider, Function<Predicate<S>, Predicate<T>> adapter, int priority) {
        if (predicateProvider != null && adapter != null)
        addProvider(
                predicateProvider instanceof Help ?
                        new AdaptableHelpPredicateProvider<>((Help & PredicateProvider<S>) predicateProvider, adapter) :
                        new AdaptablePredicateProvider<>(predicateProvider, adapter),
                priority
        );
    }

    void addProvider(PredicateProvider<T> predicateProvider, int priority);
    List<PredicateProvider<T>> getProviders();

    default Predicate<T> getPredicate(String key, Set<String> dejavu){
        for (PredicateProvider<T> provider : getProviders()){
            if (dejavu.add(provider.toString())) {
                Predicate<T> ret = provider.getPredicate(key, dejavu);
                if (ret != null) return ret;
            }
        }
        return null;
    }
    default Predicate<T> getPredicate(String key, String arg, Set<String> dejavu){
        for (PredicateProvider<T> provider : getProviders()){
            if (dejavu.add(provider.toString())) {
                Predicate<T> ret = provider.getPredicate(key, arg, dejavu);
                if (ret != null) return ret;
            }
        }
        return null;
    }
    default Predicate<T> getEmbed(String key, String script, Set<String> dejavu){
        for (PredicateProvider<T> provider : getProviders()){
            if (dejavu.add(provider.toString())) {
                Predicate<T> ret = provider.getEmbed(key, script, dejavu);
                if (ret != null) return ret;
            }
        }
        return null;
    }
    default Predicate<T> getEmbed(String key, String arg, String script, Set<String> dejavu){
        for (PredicateProvider<T> provider : getProviders()){
            if (dejavu.add(provider.toString())) {
                Predicate<T> ret = provider.getEmbed(key, arg, script, dejavu);
                if (ret != null) return ret;
            }
        }
        return null;
    }
}
