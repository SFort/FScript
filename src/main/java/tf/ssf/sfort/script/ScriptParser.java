package tf.ssf.sfort.script;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;

public class ScriptParser<T> {
    //TODO maybe dedup / cache squish?
    public List<Predicate<T>> squish = new ArrayList<>();
    public PredicateProvider<T> make = null;
    //TODO upper to under
    public boolean upperToUnder = false;

    public ScriptParser(PredicateProvider<T> predicate){
        make=predicate;
    }
    public Predicate<T> parse(String in){
        Deque<Integer> deque = new ArrayDeque<>();
        for (int i = 0; i<in.length(); i++) {
            char ch = in.charAt(i);
            switch (ch) {
                case '{', '[', '(' -> deque.addFirst(i);
                case '}', ']', ')' -> {
                    int indx = deque.removeFirst();
                    squish.add(getPredicates(in.substring(indx, i + 1)));
                    in = in.substring(0, indx) + "\u0007"+ (squish.size() - 1) + in.substring(i+1);
                    i = indx;
                }
                case '~' -> {
                    int ii = i+1;
                    int c = 0;
                    char end = ';';
                    char start = ';';
                    boolean eIndexed = false;
                    while (ii<in.length()) {
                        char cur = in.charAt(ii);
                        if (eIndexed){
                            if (cur == ':') eIndexed = false;
                        }else if (c == 0){
                            if (cur == '~'){
                                eIndexed = true;
                                ii++;
                                continue;
                            }
                            if (cur == ';') break;
                            if (cur == '(' || cur == '[' || cur == '{') {
                                c++;
                                start = cur;
                                end = start == '(' ? ')' : start == '[' ? ']' : '}';
                            }
                        }else{
                            if (cur == start) c++;
                            else if (cur == end){
                                if (c == 1){
                                    ii++;
                                    break;
                                }
                                c--;
                            }
                        }
                        ii++;
                    }
                    squish.add(predicateCheck(in.substring(i, ii), make));
                    in = in.substring(0, i) + "\u0007" + (squish.size() - 1) + in.substring(ii);
                }
            }
        }
        boolean negate = in.charAt(0) == '!';
        if (negate)
            in = in.substring(1);
        if (in.charAt(0) == '\u0007') {
            in = in.substring(1);
            return negate ? squish.get(Integer.parseInt(in)).negate() : squish.get(Integer.parseInt(in));
        }else{
            return negate ? predicateCheck(in, make).negate() : predicateCheck(in, make);
        }
    }
    @ApiStatus.Internal
    public Predicate<T> getPredicates(String in){
        Predicate<T> out = null;
        char firstchar = in.charAt(0);

        in = in.substring(1, in.length()-1);
        for (String predicateString : in.split(";")) {
            boolean negate = predicateString.charAt(0) == '!';
            if (negate) predicateString = predicateString.substring(1);
            Predicate<T> predicate = predicateString.charAt(0) == '\u0007' ? squish.get(Integer.parseInt(predicateString.substring(1))) : predicateCheck(predicateString, make);
            if (negate && predicate != null) predicate = predicate.negate();
            out = BracketMerge(firstchar, out, predicate);
        }
        return out;
    }
    @ApiStatus.Internal
    public static<T> Predicate<T> predicateCheck(String in, PredicateProvider<T> make){
        final int colon = in.indexOf(':');
        //TODO if uppercase_to_underscore do "D" to "_d" only on key
        if (colon == -1) return make.getPredicate(in);
        if (in.charAt(0) == '~'){
            final int delim = in.indexOf('~',1);
            if (delim == -1 || delim>=colon) return make.getEmbed(in.substring(1,colon), in.substring(colon+1));
            return make.getEmbed(in.substring(1,delim), in.substring(delim+1, colon).replaceAll(";", ":"), in.substring(colon+1));
        }
        return make.getPredicate(in.substring(0, colon), in.substring(colon + 1));
    }

    @ApiStatus.Internal
    public static<T> Predicate<T> BracketMerge(char in, Predicate<T> p1, Predicate<T> p2){
        if(p1 == null) return p2;
        return switch (in){
            case '[',']'->p1.and(p2);
            case '{','}'->(player)->p1.test(player) ^ p2.test(player);
            case '(',')'->p1.or(p2);
            default -> throw new IllegalStateException("Unexpected value while flipping brackets: " + in);
        };
    }

    public static String getHelp(){
        return String.format("\t%-60s%s%n", "!Condition:value", "- NOT") +
                String.format("\t%-60s%s%n", "(Condition; Condition:value; ..)", "- OR") +
                String.format("\t%-60s%s%n", "[Condition; Condition:value; ..]", "- AND") +
                String.format("\t%-60s%s%n", "{Condition; Condition:value; ..}", "- XOR");
    }
}

