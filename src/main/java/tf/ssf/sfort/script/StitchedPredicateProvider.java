package tf.ssf.sfort.script;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class StitchedPredicateProvider implements PredicateProvider<List<Object>>, Help {
    public final PredicateProvider predicateProvider;
    public final Map<String, String> help;
    protected final Map<String, Map.Entry<Integer, PredicateProvider>> predicateProviders = new HashMap<>();
    protected int size = 0;

    public StitchedPredicateProvider(PredicateProvider predicateProvider) {
        this.predicateProvider = predicateProvider;
        help = predicateProvider instanceof Help ? ((Help)predicateProvider).getHelp() : new HashMap<>();
    }

    public StitchedPredicateProvider addEmbed(PredicateProvider predicateProvider, String help, String helpDesc) {
        size+=1;
        this.help.put(help, helpDesc);
        for (String key : Help.dismantle(help).b) {
            predicateProviders.put(key, new AbstractMap.SimpleEntry<>(size, predicateProvider));
        }
        return this;
    }

    @Override
    public Predicate<List<Object>> getPredicate(String key, Set<String> dejavu) {
        if (dejavu.add(predicateProvider.toString())){
            Predicate p = predicateProvider.getPredicate(key, dejavu);
            if (p != null) {
                return o -> p.test(o.get(0));
            }
        }
        Map.Entry<Integer, PredicateProvider> entry = predicateProviders.get(key);
        if (entry != null) {
            PredicateProvider provider = entry.getValue();
            if (dejavu.add(provider.toString())) {
                final Predicate ret = provider.getPredicate(key, dejavu);
                if (ret != null) {
                    final int varIndex = entry.getKey();
                    return p -> ret.test(p.get(varIndex));
                }
            }
        }
        return null;
    }

    @Override
    public Predicate<List<Object>> getPredicate(String key, String arg, Set<String> dejavu) {
        if (dejavu.add(predicateProvider.toString())){
            Predicate p = predicateProvider.getPredicate(key, arg, dejavu);
            if (p != null) {
                return o -> p.test(o.get(0));
            }
        }
        Map.Entry<Integer, PredicateProvider> entry = predicateProviders.get(key);
        if (entry != null) {
            PredicateProvider provider = entry.getValue();
            if (dejavu.add(provider.toString())) {
                final Predicate ret = provider.getPredicate(key, arg, dejavu);
                if (ret != null) {
                    final int varIndex = entry.getKey();
                    return p -> ret.test(p.get(varIndex));
                }
            }
        }
        return null;
    }

    @Override
    public Predicate<List<Object>> getEmbed(String key, String script, Set<String> dejavu) {
        if (dejavu.add(predicateProvider.toString())){
            Predicate p = predicateProvider.getEmbed(key, script, dejavu);
            if (p != null) {
                return o -> p.test(o.get(0));
            }
        }
        Map.Entry<Integer, PredicateProvider> entry = predicateProviders.get(key);
        if (entry != null) {
            PredicateProvider provider = entry.getValue();
            if (dejavu.add(provider.toString())) {
                final Predicate ret = provider.getEmbed(key, script, dejavu);
                if (ret != null) {
                    final int varIndex = entry.getKey();
                    return p -> ret.test(p.get(varIndex));
                }
            }
        }
        return null;
    }

    @Override
    public Predicate<List<Object>> getEmbed(String key, String arg, String script, Set<String> dejavu) {
        if (dejavu.add(predicateProvider.toString())){
            Predicate p = predicateProvider.getEmbed(key, arg, script, dejavu);
            if (p != null) {
                return o -> p.test(o.get(0));
            }
        }
        Map.Entry<Integer, PredicateProvider> entry = predicateProviders.get(key);
        if (entry != null) {
            PredicateProvider provider = entry.getValue();
            if (dejavu.add(provider.toString())) {
                final Predicate ret = provider.getEmbed(key, arg, script, dejavu);
                if (ret != null) {
                    final int varIndex = entry.getKey();
                    return p -> ret.test(p.get(varIndex));
                }
            }
        }
        return null;
    }

    @Override
    public Map<String, String> getHelp(){
        return help;
    }
}
