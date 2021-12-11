package sample;

public class Main {

    public static void main(String[] args) {
        Grammar gr = new Grammar();
        gr.readGrammar("g3.txt");
        System.out.println(gr.getP());
        Parser parser = new Parser(gr);
    }
}