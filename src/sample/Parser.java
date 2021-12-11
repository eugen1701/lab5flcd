package sample;

import javafx.util.Pair;

import java.util.*;

public class Parser {
    private Grammar grammar;
    private Map<String, Set<String>> firstSet;
    private Map<String, Set<String>> follow;
    private Map<Pair, Pair> parseTable;

    public Parser(Grammar grammar) {
        this.grammar = grammar;
        firstSet = new HashMap<>();
        follow = new HashMap<>();
        parseTable = new HashMap<>();

        doFirst();
        doFollow();
        System.out.println(follow);
    }

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

    public void doTheParseTable() {

    }
}
