package improved.solution;

public class Main {
    public static void main(String[] args) {
        Algorithm algorithm = new Algorithm("/Users/macbook/Documents/GitHub/universal-converter/src/test/resources/INPUT.scv");
        algorithm.convert("м/с", "км/час");
        algorithm.convert("км*м*м/час*с*с", "см*км*км/мин*мин*мин");
    }
}
