package ui;

import model.GameController;
import model.Pokemon;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PokeRanchoPanel extends JPanel {
    private GameController controller;
    private final JPanel grid = new JPanel(new GridLayout(0, 5, 8, 8));
    private final JLabel estado = new JLabel(" ");

    public PokeRanchoPanel(MainFrame frame) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.add(grid, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(envoltorio);
        scroll.setBorder(BorderFactory.createTitledBorder("PokeRancho"));

        JPanel col = new JPanel(new GridLayout(0, 1, 10, 20));
        JButton porId = new JButton("Traer poke por id/nombre");
        JButton lote = new JButton("Traer lote de poke");
        JButton volver = new JButton("Volver");
        col.add(porId); col.add(lote); col.add(volver);
        JPanel derecha = new JPanel(new GridBagLayout());
        derecha.add(col);

        porId.addActionListener(e -> {
            String s = JOptionPane.showInputDialog(this, "ID o nombre del Pokémon:");
            if (s != null && !s.isBlank()) {
                mensaje("Buscando " + s + "...");
                controller.capturarPokesAsync(s);
            }
        });
        lote.addActionListener(e -> {
            mensaje("Capturando pokemones...");
            controller.capturarLotePoke();
        });
        volver.addActionListener(e -> frame.mostrar(MainFrame.MENU));

        add(scroll, BorderLayout.CENTER);
        add(derecha, BorderLayout.EAST);
        add(estado, BorderLayout.SOUTH);
    }

    public void setController(GameController c) { this.controller = c; }
    public void mensaje(String m) { estado.setText(m); }

    public void refrescar(List<Pokemon> rancho) {
        grid.removeAll();
        for (Pokemon p : rancho) grid.add(PokeCard.crear(p, false));
        grid.revalidate();
        grid.repaint();
    }
}