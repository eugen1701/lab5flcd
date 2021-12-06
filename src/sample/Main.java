package sample;

public class Main {

    public static void main(String[] args) {
        Grammar gr = new Grammar();
        gr.readGrammar("g3.txt");
        Parser parser = new Parser(gr);
    }
}