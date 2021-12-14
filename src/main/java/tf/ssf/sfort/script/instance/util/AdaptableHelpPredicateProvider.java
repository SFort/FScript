package tf.ssf.sfort.script.instance.util;

import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class AdaptableHelpPredicateProvider<T, S> extends AdaptablePredicateProvider<T, S> implements Help {

    public <K extends Help & PredicateProvider<S>> AdaptableHelpPredicateProvider(K predicateProvider, Function<Predicate<S>, Predicate<T>> adapter) {
        super(predicateProvider, adapter);
    }

    @Override
    public List<Help> getImported() {
        return ((Help)predicateProvider).getImported();
    }

    @Override
    public Map<String, String> getHelp() {
        return ((Help)predicateProvider).getHelp();
    }

}
