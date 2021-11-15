package sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Grammar {
    private List<String> N; //non-terminals
    private List<String> T; // terminals
    private String S; //starting symbol
    private Map<String, List<String>> P; // productions

    public Grammar() {
//        this.N = new ArrayList<>();
//        this.T = new ArrayList<>();
        this.P = new HashMap<>();
        }


    public List<String> readLineToList(String line){
        String array[] = line.split(" ");
        return Arrays.asList(array.clone());
    }

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
            String left = line.get(0);
            String right = line.get(2);
            line = Arrays.asList(right.split("|").clone());
            if (P.containsKey(left)) {
                for (int i = 0; i < line.size(); i++) {
                    if (!P.get(left).contains(line.get(i)))
                        P.get(left).add(line.get(i));
                }
            } else {
                P.put(left, new ArrayList<>());
                for (int i = 0; i < line.size(); i++) {
                    P.get(left).add(line.get(i));
                }
            }
        }

    }

    private String PtoString() {
        String toDisplay = "Productions: \n";
        for(Map.Entry el : P.entrySet()) {
            toDisplay += el.getKey();
            toDisplay += " -> ";
            List<String> prod = (List<String>) el.getValue();
            for(String i : prod) {
                toDisplay += i;
                toDisplay += "|";
            }
            toDisplay.replaceAll("|$", "\n");
        }
        return toDisplay;
    }

    @Override
    public String toString() {
        return "Grammar{" +
                "N=" + N +
                ", T=" + T +
                ", S='" + S + '\'' +
                this.PtoString() +
                '}';
    }
}
