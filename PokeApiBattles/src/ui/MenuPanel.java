package ui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class MenuPanel extends JPanel {
    public MenuPanel(MainFrame frame) {
        setLayout(new GridLayout(1, 2, 10, 0));

        JLabel imagen = new JLabel("Imagen", SwingConstants.CENTER);
        URL url = getClass().getResource("/menu.png"); // pon menu.png en la carpeta resources
        if (url != null) imagen.setIcon(new ImageIcon(url));
        add(imagen);

        JPanel botones = new JPanel(new GridBagLayout());
        JPanel col = new JPanel(new GridLayout(0, 1, 10, 20));
        col.add(boton("PokeRancho", () -> frame.mostrar(MainFrame.RANCHO)));
        col.add(boton("Enfrentamiento", () -> frame.mostrar(MainFrame.ENFRENTAMIENTOS)));
        botones.add(col);
        add(botones);
    }

    private JButton boton(String t, Runnable r) {
        JButton b = new JButton(t);
        b.setPreferredSize(new Dimension(200, 50));
        b.addActionListener(e -> r.run());
        return b;
    }
}