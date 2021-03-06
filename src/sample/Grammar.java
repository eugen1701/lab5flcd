package sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Grammar {
    private List<String> N; //non-terminals
    private List<String> T; // terminals
    private String S; //starting symbol
    private Map<List<String>, List<List<String>>> P; // productions

    public Grammar() {
        this.P = new HashMap<>();
    }

    public Map<List<String>, List<List<String>>> getP() {
        return P;
    }

    public List<String> getN() { return N; }

    public List<String> getT() { return T; }

    public String getS() { return S; }

    public List<String> readLineToList(String line){
        String array[] = line.split(" ");
        return Arrays.asList(array.clone());
    }

    /**
     * @param fileName The name of the file from where the grammar is read.
     * It reads line by line until it gets to the production and after it takes the line and split by "->"
     *                 the left part is the non-terminal and the right part are the transitions separated and
     *                 split by "|"
     * */
    public void readGrammar(String fileName) {
        File file = new File(fileName);
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        N = readLineToList(scanner.nextLine());
        T = readLineToList(scanner.nextLine());
        S = scanner.nextLine();

        while (scanner.hasNextLine()) {
            List<String> line = readLineToList(scanner.nextLine());
            int i = 0;
            ArrayList<String> key = new ArrayList<>();
            while(!line.get(i).contains("->")) {
                List<String> token = Arrays.asList(line.get(i).split("\\|").clone());
                for(String j : token) {
                    if(!key.contains(j) && !j.equals("|")) {
                        key.add(j);
                    }
                }
                i++;
            }
            //System.out.println(key);
            P.put(key, new ArrayList<>());
            i++;
            ArrayList<String> value = new ArrayList<>();
            while(i<line.size()) {
                if(line.get(i).equals("|")) {
                    P.get(key).add(value);
                    value = new ArrayList<>();
                } else {
                    value.add(line.get(i));
                }
                i++;
            }
            P.get(key).add(value);

//            System.out.println(key);
//            System.out.println(value);
        }

    }

    /**
     * It check if the grammar is Context Free by checking the number of the  non-terminals on the
     * left part of the production and also check that in the left part are only non-terminals
     * */
    public boolean checkCfg() {
        if (!N.contains(S)) {
            System.out.println("S is not in N");
            return false;
        }
        for(Map.Entry el : P.entrySet()) {
            List<String> key = (List<String>) el.getKey();
            if(key.size() != 1) {
                System.out.println("One key has more than one element");
                return false;
            }
            for(String str : key) {
                if(!N.contains(str)) {
                    System.out.println(str + " is not in N");
                    return false;
                }
            }
            List<List<String>> value = (List<List<String>>) el.getValue();
            for(List l : value) {
                for(Object str: l) {
                    String s_str = (String) str;
                    if(!N.contains(s_str) && !T.contains(s_str) && !s_str.equals("epsilon")) {
                        System.out.println(s_str + " is bad");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public Set<List<String>> getNonterminalProd(String nonterminal) {
        for(List<String> lhs : P.keySet()) {
            if( lhs.contains(nonterminal)) {
                return new HashSet<>( P.get(lhs));
            }
        }
        return new HashSet<>();
    }

    private String PtoString() {
        String toDisplay = "\nProductions: \nKeys:\n";
        for(Map.Entry el : P.entrySet()) {
            List<String> key = (List<String>) el.getKey();
            toDisplay += key;
            toDisplay += " --> ";
            List<List<String>> value = (List<List<String>>) el.getValue();
            for(List l : value) {
                toDisplay += l;
                toDisplay += " | ";
            }
            toDisplay += "\n";
        }
        return toDisplay;
    }

    @Override
    public String toString() {
        return "Grammar{\n" +
                "N=" + N +
                ",\n T=" + T +
                ",\n S='" + S + '\'' +
                this.PtoString() +
                '}';
    }
}
