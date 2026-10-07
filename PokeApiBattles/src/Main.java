import ui.MainFrame;
import model.GameController;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));

    }
}