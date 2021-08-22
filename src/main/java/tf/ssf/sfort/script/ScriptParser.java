package tf.ssf.sfort.script;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;

public class ScriptParser<T> {
    public static void main(String[] arg){
        new ScriptParser<>(Default.SERVER_PLAYER_ENTITY).parse("~hand:item:dirt");
    }
    //TODO maybe dedup / cache squish?
    public List<Predicate<T>> squish = new ArrayList<>();
    public PredicateProvider<T> make = null;
    public ScriptParser(PredicateProvider<T> predicate){
        make=predicate;
    }
    public Predicate<T> parse(String in){
        Deque<Integer> deque = new ArrayDeque<>();
        for (int i = 0; i<in.length(); i++) {
            char ch = in.charAt(i);
            switch (ch) {
                case '~' ->{
                    int i_split = in.indexOf(':', i);
                    if (i_split != -1) {
                        int i_end = i_split+1;
                        char start = in.charAt(i_end);
                        char end = start == '(' ? ')' : start == '[' ? ']' : start == '{' ? '}' : 0;
                        if (end != 0) {
                            int c = 1;
                            while (c != 0){
                                i_end++;
                                char cur = in.charAt(i_end);
                                if (cur == start) c++;
                                else if (cur == end) c--;
                            }
                            squish.add(predicateCheck(in.substring(i, i_end+1), make));
                            in = in.substring(0, i)+"\u0007"+(squish.size()-1)+in.substring(i_end+1);
                        }
                    }
                }
                case '{', '[', '(' -> deque.addFirst(i);
                case '}', ']', ')' -> {
                    int indx = deque.removeFirst();
                    String str = in.substring(indx, i + 1);
                    i = indx;
                    in = in.replace(str, "\u0007" + squish.size());
                    squish.add(getPredicates(str));
                }
            }
        }
        boolean negate = in.charAt(0) == '!';
        if (negate)
            in = in.replaceFirst("!", "");
        if (in.charAt(0) == '\u0007') {
            in = in.replaceFirst("\u0007", "");
            return negate ? squish.get(Integer.parseInt(in)).negate() : squish.get(Integer.parseInt(in));
        }else{
            return negate ? predicateCheck(in, make).negate() : predicateCheck(in, make);
        }
    }
    @ApiStatus.Internal
    public Predicate<T> getPredicates(String in){
        Predicate<T> out = null;
        char firstchar = in.charAt(0);
        boolean negate, negate_return = firstchar == '!';
        if(negate_return)
            in = in.replaceFirst("!", "");
        in = in.substring(1, in.length()-1);
        for (String predicateString : in.split(";")) {
            negate = predicateString.charAt(0) == '!';
            if (negate)
                predicateString = predicateString.replaceFirst("!", "");
            if (predicateString.charAt(0) == '\u0007') {
                predicateString = predicateString.replaceFirst("\u0007", "");
                out = BracketMerge(firstchar, out, squish.get(Integer.parseInt(predicateString)));
            }else{
                out = BracketMerge(firstchar, out, predicateCheck(predicateString, make));
            }
            if (negate && out != null)
                out = out.negate();
        }
        return negate_return && out != null? out.negate() : out;
    }
    @ApiStatus.Internal
    public static<T> Predicate<T> predicateCheck(String in, PredicateProvider<T> make){
        int colon = in.indexOf(':');
        return colon == -1 ? make.getPredicate(in): in.charAt(0) == '~'?
                make.getEmbed(in.substring(1, colon), in.substring(colon + 1)) :
                make.getPredicate(in.substring(0, colon), in.substring(colon + 1));
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

