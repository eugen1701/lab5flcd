package sample;

import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Grammar gr = new Grammar();
        gr.readGrammar("g2.txt");
        Parser parser = new Parser(gr);
        parser.doTheParseTable();
        //System.out.println(parser.parseTableToString());
        List<String> sequence = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("seq.txt"));

            String line = reader.readLine();
            while (line != null) {
                var symbols = List.of(line.split(" "));
                sequence.addAll(symbols);
                line = reader.readLine();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        System.out.println(parser.getParseTable());

        System.out.println(parser.parseSequence(sequence));
    }
}