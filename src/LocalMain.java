import local.LocalGameFactory;

public class LocalMain {
    public static void main(String[] args) {
        LocalGameFactory factory = new LocalGameFactory();
        factory.initializeStandardBoard();
        factory.getPrinter().printGUI();
    }
}
