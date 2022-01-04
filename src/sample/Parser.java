package sample;

import javafx.util.Pair;

import java.util.*;

public class Parser {
    private Grammar grammar;
    private Map<String, Set<String>> firstSet;
    private Map<String, Set<String>> follow;
    private Map<Pair, Pair> parseTable;
    private List<List<String>> rhsProd;

    public Parser(Grammar grammar) {
        this.grammar = grammar;
        firstSet = new HashMap<>();
        follow = new HashMap<>();
        parseTable = new HashMap<>();

        doFirst();
        doFollow();
        System.out.println(follow);
    }

    /**
     * It generate the first set of each non-terminal or transition of the production. It tries to find the first terminals
     * which can be obtained by doing all the possible transitions.
     * */
    public void doFirst() {
        boolean setChanged = true;

        List<String> nonterminals = this.grammar.getN();
        for (String i : nonterminals) {
            firstSet.put(i, new HashSet<>());
            Set<List<String>> nonterminalProd = grammar.getNonterminalProd(i);
            for (List<String> prod : nonterminalProd) {
                if (grammar.getT().contains(prod.get(0)) || prod.get(0).equals("epsilon")) {
                    this.firstSet.get(i).add(prod.get(0));
                }
            }
        }

        while (setChanged) {
            setChanged = false;
            Map<String, Set<String>> column = new HashMap<>();

            for (String nont : grammar.getN()) {
                Set<List<String>> nonterminalProd = grammar.getNonterminalProd(nont);
                Set<String> toAdd = new HashSet<>(this.firstSet.get(nont));
                for (List<String> prod : nonterminalProd) {
                    List<String> rhsNonterminal = new ArrayList<>();
                    String rhsTerminal = null;
                    for (String symbol : prod) {
                        if (this.grammar.getN().contains(symbol)) {
                            rhsNonterminal.add(symbol);
                        } else {
                            rhsTerminal = symbol;
                            break;
                        }
                    }
                    toAdd.addAll(innerLoop(firstSet, rhsNonterminal, rhsTerminal));
                }
                if(!toAdd.equals(firstSet.get(nont))) {
                    setChanged = true;
                }
                column.put(nont, toAdd);
            }
            firstSet = column;
        }
        System.out.println(firstSet);
    }

    /**
     * This function is in addition for the doFirst function.
     * @param initialSet represents the initial first set which will be concatenated
     * @param nonTerminals represents the set of the non-terminals which will be added to the initial first set
     * @param terminal represents the terminals which will be added to the initial first set
     * */
    public Set<String> innerLoop(Map<String, Set<String>> initialSet, List<String> nonTerminals, String terminal) {
        if (nonTerminals.size() == 0) {
            return new HashSet<>();
        }
        if (nonTerminals.size() == 1) {
            return initialSet.get(nonTerminals.iterator().next());
        }

        Set<String> concatenation = new HashSet<>();
        int step = 0;
        boolean onlyEpsilon = false;

        for (String nonT : nonTerminals) {
            if (!initialSet.get(nonT).contains("epsilon")) {
                onlyEpsilon = false;
            }
        }

        if (onlyEpsilon) {
            concatenation.add(Objects.requireNonNullElse(terminal, "epsilon"));
        }

        while(step < nonTerminals.size()) {
            boolean isEpsilon = false;
            for(String s : initialSet.get(nonTerminals.get(step))) {
                if (s.equals("epsilon")) {
                    isEpsilon = true;
                } else {
                    concatenation.add(s);
                }
            }
                if(isEpsilon) step++;
                else break;

        }

        return concatenation;
    }

    /**
     * It generates the follow set for the productions. This function is used in case there is "epsilon" in the grammar.
     * In case the transitions end by epsilon, it will search for the next terminal which can be find.
     * */
    public void doFollow() {
        for (String nonT : grammar.getN()) {
            follow.put(nonT, new HashSet<>());
        }
        follow.get(grammar.getS()).add("epsilon");

        boolean setChanged = true;
        while(setChanged) {
            setChanged = false;
            Map<String, Set<String>> column = new HashMap<>();

            for (String nonT : grammar.getN()) {
                column.put(nonT, new HashSet<>());
                HashMap<String, Set<List<String>>> prodWithNonTerminalsInRhs = new HashMap<>();
                HashMap<List<String>, List<List<String>>> P = (HashMap<List<String>, List<List<String>>>) grammar.getP();

                P.forEach((k, v) -> {
                    for(var eachProd : v) {
                        if(eachProd.contains(nonT)) {
                            var key = k.iterator().next();
                            if(!prodWithNonTerminalsInRhs.containsKey(key)) {
                                prodWithNonTerminalsInRhs.put(key, new HashSet<>());
                            }
                            prodWithNonTerminalsInRhs.get(key).add(eachProd);
                        }
                    }
                });

                Set<String> toAdd = new HashSet<>(follow.get(nonT));
                prodWithNonTerminalsInRhs.forEach((k, v) -> {
                    for(var prod : v) {
                        var prodList = new ArrayList<>(prod);
                        var indexOfNonterminal = prodList.indexOf(nonT);
                        if(indexOfNonterminal + 1 == prodList.size()) {
                            toAdd.addAll(follow.get(k));
                        } else {
                            var followSymbol = prodList.get(indexOfNonterminal + 1);
                            if(grammar.getT().contains(followSymbol)) {
                                toAdd.add(followSymbol);
                            }
                            else {
                                toAdd.addAll(firstSet.get(followSymbol));
                                toAdd.addAll(follow.get(k));
                            }
                        }

                    }
                });
                if(!toAdd.equals(follow.get(nonT))){
                    setChanged = true;
                }
                column.put(nonT, toAdd);
            }

            follow = column;
        }


    }

    /**
     * It generates the Parse Table. The parse table is represented by a Map (dictionary) in which the value is a Pair
     * representing the coordinates of the table(the first value is the first colon of the table with all the non-terminals
     * and terminals and the second value represents the first line of the table representing the terminals) and the value of the
     * Map is again a Pair(where the first element are the elements from the grammar and the second value is the number of the
     * transition in the production). The second Pair is the value situated in the table at the coordinates of the first pair.
     * */
    public void doTheParseTable() {
        List<String> rows = new ArrayList<>();
        rows.addAll(grammar.getN());
        rows.addAll(grammar.getT());
        rows.add("$");

        List<String> columns = new ArrayList<>();
        columns.addAll(grammar.getT());
        columns.add("$");

        for(var i : rows) {
            for(var j : columns)
                parseTable.put(new Pair<>(i, j), new Pair<>("err", -1));
        }

        for(var j : columns) {
            parseTable.put(new Pair<>(j, j), new Pair<>("pop", -1));
        }

        parseTable.put(new Pair<>("$", "$"), new Pair<>("acc", -1));

        var productions = grammar.getP();
        this.rhsProd = new ArrayList<>();
        productions.forEach((k, v) -> {
            var nonT = k.iterator().next();
            for(var prod : v) {
                if(!prod.get(0).equals("epsilon")) rhsProd.add(prod);
                else rhsProd.add(new ArrayList<>(List.of("epsilon", nonT)));
            }
        });

        System.out.println(rhsProd);

        productions.forEach((k, v) -> {
            var key = k.get(0);
            for(var prod : v) {
                String symbol1 = prod.get(0);
                if(grammar.getT().contains(symbol1))
                    if(parseTable.get(new Pair(key, symbol1)).getKey().equals("err"))
                        parseTable.put(new Pair(key, symbol1), new Pair(String.join(" ", prod), rhsProd.indexOf(prod) + 1));
                    else {
                        System.out.println("There is a conflict with " + key + " and " + symbol1);
                    }
                else if(grammar.getN().contains(symbol1)) {
                    if(prod.size() == 1)
                        for(var symbol : firstSet.get(symbol1)) {
                            if(parseTable.get(new Pair(key, symbol)).getKey().equals("err"))
                                parseTable.put(new Pair(key, symbol), new Pair(String.join(" ", prod), rhsProd.indexOf(prod)+1));
                            else {
                                System.out.println("There is a conflict with " + key + " and " + symbol);
                            }
                        }
                    else {
                        int i = 1;
                        String symbol2 = prod.get(1);
                        var firstSetFirstSymbol = firstSet.get(symbol1);

                        while(i < prod.size() && grammar.getN().contains(symbol2)) {
                            var firstSetSecondSymbol = firstSet.get(symbol2);
                            if(firstSetFirstSymbol.contains("epsilon")) {
                                firstSetFirstSymbol.remove("epsilon");
                                firstSetFirstSymbol.addAll(firstSetSecondSymbol);
                            }

                            i++;
                            if(i < prod.size()) {
                                symbol2 = prod.get(i);
                            }
                        }

                        for(var symbol : firstSetFirstSymbol) {
                            if(symbol.equals("epsilon")) symbol = "$";
                            if(parseTable.get(new Pair(key, symbol)).getKey().equals("err"))
                                parseTable.put(new Pair(key, symbol), new Pair(String.join(" ", prod), rhsProd.indexOf(prod)+1));
                            else {
                                System.out.println("There is a conflict with " + key + " and " + symbol);
                            }
                        }
                    }

                }
                else {
                    var foll = follow.get(key);
                    for(var symbol : foll) {
                        if(symbol.equals("epsilon")) {
                            if(parseTable.get(new Pair(key, "$")).getKey().equals("err")) {
                                var product = new ArrayList<>(List.of("epsilon", key));
                                parseTable.put(new Pair(key, "$"), new Pair("epsilon", rhsProd.indexOf(product) + 1));
                            }
                            else {
                                System.out.println("There is a conflict with " + key + " and " + symbol);
                            }
                        }
                        else if(parseTable.get(new Pair(key, symbol)).getKey().equals("err")) {
                            var product = new ArrayList<>(List.of("epsilon", key));
                            parseTable.put(new Pair(key, symbol), new Pair("epsilon", rhsProd.indexOf(product) + 1));
                        }
                        else {
                            System.out.println("There is a conflict with " + key + " and " + symbol);
                        }
                    }
                }
            }
        });

    }

    public Map<Pair, Pair> getParseTable() {
        return this.parseTable;
    }

    public String parseTableToString() {
        StringBuilder builder = new StringBuilder();
        parseTable.forEach((k, v) -> {

            builder.append(k).append("  ->  ").append(v).append("\n");
        });
        return builder.toString();
    }

    /**
     * It parse the sequence and return the list of numbers representing the number of the transitions
     * */
    public List<Integer> parseSequence(List<String> seq) {
        Stack<String> alpha = new Stack<>();
        Stack<String> beta = new Stack<>();
        List<Integer> result = new ArrayList<>();

        //initialization
        alpha.push("$");
        for(var i=seq.size()-1;i>=0;--i)
            alpha.push(seq.get(i));

        beta.push("$");
        beta.push(grammar.getS());

        while(!(alpha.peek().equals("$") && beta.peek().equals("$"))) {
            String peekAlpha = alpha.peek();
            String peekBeta = beta.peek();

            Pair key = new Pair(peekBeta, peekAlpha);
            Pair value = parseTable.get(key);

            if(!value.getKey().equals("err")) {
                if(value.getKey().equals("pop")) {
                    alpha.pop();
                    beta.pop();
                } else {
                    beta.pop();
                    if(!value.getKey().equals("epsilon")) {
                        String first = (String)value.getKey();
                        String[] val = first.split(" ");
                        for(var i = val.length - 1; i>=0;i--) {
                            beta.push(val[i]);
                        }
                    }
                    result.add((Integer) value.getValue());

                }
            } else {
                System.out.println("Syntax error for key "+key);
                System.out.println("Current alpha and beta for sequence parsing:");
                System.out.println(alpha);
                System.out.println(beta);
                result = new ArrayList<>(List.of(-1));
                return result;
            }
        }
        return result;
    }

}
