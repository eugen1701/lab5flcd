package sample;

public class Main {

    public static void main(String[] args) {
        Grammar gr = new Grammar();
        gr.readGrammar("g2.txt");
        System.out.println(gr.checkCfg());
    }
}