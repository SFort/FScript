package tf.ssf.sfort.script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

//TODO should allow duplicates of the same predicate provider
public class StitchedPredicateProvider implements PredicateProvider<List<Object>>, Help {
    public final List<PredicateProvider> predicateProviders;

    public StitchedPredicateProvider(PredicateProvider... predicateProviders) {
        this.predicateProviders = Arrays.asList(predicateProviders);
        for (PredicateProvider predicate : predicateProviders) {
            if (predicate instanceof Help){
                extend_help.add((Help)predicate);
            }
        }
    }

    @Override
    public Predicate<List<Object>> getPredicate(String key, Set<String> dejavu) {
        for (int i = 0; i<predicateProviders.size(); i++){
            PredicateProvider provider = predicateProviders.get(i);
            if (dejavu.add(provider.toString())) {
                final Predicate ret = provider.getPredicate(key, dejavu);
                if (ret == null) continue;
                final int varIndex = i;
                return p->ret.test(p.get(varIndex));
            }
        }
        return null;
    }

    @Override
    public Predicate<List<Object>> getPredicate(String key, String arg, Set<String> dejavu) {
        for (int i = 0; i<predicateProviders.size(); i++){
            PredicateProvider provider = predicateProviders.get(i);
            if (dejavu.add(provider.toString())) {
                final Predicate ret = provider.getPredicate(key, arg, dejavu);
                if (ret == null) continue;
                final int varIndex = i;
                return p->ret.test(p.get(varIndex));
            }
        }
        return null;
    }

    @Override
    public Predicate<List<Object>> getEmbed(String key, String script, Set<String> dejavu) {
        for (int i = 0; i<predicateProviders.size(); i++){
            PredicateProvider provider = predicateProviders.get(i);
            if (dejavu.add(provider.toString())) {
                final Predicate ret = provider.getEmbed(key, script, dejavu);
                if (ret == null) continue;
                final int varIndex = i;
                return p->ret.test(p.get(varIndex));
            }
        }
        return null;
    }

    @Override
    public Predicate<List<Object>> getEmbed(String key, String arg, String script, Set<String> dejavu) {
        for (int i = 0; i<predicateProviders.size(); i++){
            PredicateProvider provider = predicateProviders.get(i);
            if (dejavu.add(provider.toString())) {
                final Predicate ret = provider.getEmbed(key, arg, script, dejavu);
                if (ret == null) continue;
                final int varIndex = i;
                return p->ret.test(p.get(varIndex));
            }
        }
        return null;
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

}
