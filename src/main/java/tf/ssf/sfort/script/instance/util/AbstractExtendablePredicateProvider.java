package tf.ssf.sfort.script.instance.util;

import net.minecraft.util.Pair;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.ExtendablePredicateProvider;

import java.util.*;
import java.util.function.Predicate;

public abstract class AbstractExtendablePredicateProvider<T> implements ExtendablePredicateProvider<T>, Help {

    public Predicate<T> getLocalPredicate(String in){
        return null;
    }
    public Predicate<T> getLocalPredicate(String in, String val){
        return null;
    }
    public Predicate<T> getLocalEmbed(String in, String script){
        return null;
    }
    public Predicate<T> getLocalEmbed(String in, String val, String script){
        return null;
    }

    //==================================================================================================================

    @Override
    public Predicate<T> getPredicate(String key, Set<String> dejavu) {
        final Predicate<T> out = getLocalPredicate(key);
        if (out != null) return out;
        return ExtendablePredicateProvider.super.getPredicate(key, dejavu);
    }

    @Override
    public Predicate<T> getPredicate(String key, String arg, Set<String> dejavu) {
        final Predicate<T> out = getLocalPredicate(key, arg);
        if (out != null) return out;
        return ExtendablePredicateProvider.super.getPredicate(key, arg, dejavu);
    }

    @Override
    public Predicate<T> getEmbed(String key, String script, Set<String> dejavu) {
        final Predicate<T> out = getLocalEmbed(key, script);
        if (out != null) return out;
        return ExtendablePredicateProvider.super.getEmbed(key, script, dejavu);
    }

    @Override
    public Predicate<T> getEmbed(String key, String arg, String script, Set<String> dejavu) {
        final Predicate<T> out = getLocalEmbed(key, arg, script);
        if (out != null) return out;
        return ExtendablePredicateProvider.super.getEmbed(key, arg, script, dejavu);
    }

    //==================================================================================================================

    @Override
    public Map<String, String> getHelp(){
        return help;
    }
    @Override
    public List<Help> getImported(){
        return extend_help;
    }
    public final Map<String, String> help = new HashMap<>();
    public final List<Help> extend_help = new ArrayList<>();

    //==================================================================================================================

    public final TreeSet<Pair<Integer, PredicateProvider<T>>> EXTEND = new TreeSet<>(Comparator.<Pair<Integer, PredicateProvider<T>>>comparingInt(Pair::getLeft).reversed());

    @Override
    public void addProvider(PredicateProvider<T> predicateProvider, int priority) {
        if (predicateProvider instanceof Help) extend_help.add((Help) predicateProvider);
        EXTEND.add(new Pair<>(priority, predicateProvider));
    }

    @Override
    public List<PredicateProvider<T>> getProviders() {
        return EXTEND.stream().map(Pair::getRight).toList();
    }

}
