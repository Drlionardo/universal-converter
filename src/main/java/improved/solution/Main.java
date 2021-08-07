package improved.solution;

public class Main {
    public static void main(String[] args) {
        Algorithm algorithm = new Algorithm("/Users/macbook/Documents/GitHub/universal-converter/src/test/resources/INPUT.scv");
        System.out.println(algorithm.convert("м/с", "км/час"));
        System.out.println(algorithm.convert("м/км", "км/час"));
    }
}
