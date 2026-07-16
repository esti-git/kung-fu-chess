import input.GameFactory;

public class Main {
    public static void main(String[] args) {
        GameFactory factory = new GameFactory();
        factory.initializeStandardBoard();
        factory.getPrinter().printGUI();
    }
}
