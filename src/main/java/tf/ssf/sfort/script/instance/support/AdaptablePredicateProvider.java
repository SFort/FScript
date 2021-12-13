package tf.ssf.sfort.script.instance.support;

import tf.ssf.sfort.script.PredicateProvider;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class AdaptablePredicateProvider<T, S> implements PredicateProvider<T> {
    PredicateProvider<S> predicateProvider;
    Function<Predicate<S>, Predicate<T>> adapter;

    public AdaptablePredicateProvider(PredicateProvider<S> predicateProvider, Function<Predicate<S>, Predicate<T>> adapter) {
        this.predicateProvider = predicateProvider;
        this.adapter = adapter;
    }
    @Override
    public Predicate<T> getPredicate(String key, Set<String> dejavu) {
        Predicate<S> ret = predicateProvider.getPredicate(key, dejavu);
        if (ret!=null) return adapter.apply(ret);
        return null;
    }
    @Override
    public Predicate<T> getPredicate(String key, String arg, Set<String> dejavu) {
        Predicate<S> ret = predicateProvider.getPredicate(key, arg, dejavu);
        if (ret!=null) return adapter.apply(ret);
        return null;
    }
    @Override
    public Predicate<T> getEmbed(String key, String script, Set<String> dejavu) {
        Predicate<S> ret = predicateProvider.getEmbed(key, script, dejavu);
        if (ret!=null) return adapter.apply(ret);
        return null;
    }
    @Override
    public Predicate<T> getEmbed(String key, String arg, String script, Set<String> dejavu) {
        Predicate<S> ret = predicateProvider.getEmbed(key, arg, script, dejavu);
        if (ret!=null) return adapter.apply(ret);
        return null;
    }

}
