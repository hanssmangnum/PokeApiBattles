package ui;

import model.Pokemon;
import javax.swing.*;
import java.awt.*;

public class PokeCard {
    public static JPanel crear(Pokemon p, boolean resaltado) {
        JPanel c = new JPanel(new BorderLayout());
        c.setPreferredSize(new Dimension(95, 95));
        JLabel img = new JLabel("", SwingConstants.CENTER);
        SpriteCache.cargar(p.getSpritefrontal(), img);
        JLabel nombre = new JLabel(p.getNombre(), SwingConstants.CENTER);
        nombre.setFont(nombre.getFont().deriveFont(11f));
        c.add(img, BorderLayout.CENTER);
        c.add(nombre, BorderLayout.SOUTH);
        c.setBorder(BorderFactory.createLineBorder(resaltado ? Color.RED : Color.LIGHT_GRAY, resaltado ? 3 : 1));
        return c;
    }
}