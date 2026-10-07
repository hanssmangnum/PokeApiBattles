package ui;

import model.GameController;
import model.Player;
import model.Pokemon;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EnfrentamientosPanel extends JPanel {
    private final MainFrame frame;
    private GameController controller;
    private final JPanel equipo1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JPanel equipo2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JSpinner spinner = new JSpinner(new SpinnerNumberModel(3, 1, 6, 1));

    public EnfrentamientosPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new GridLayout(1, 3, 10, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(columnaJugador("Jugador 1 pokemones:", equipo1, 1));
        add(columnaJugador("Jugador 2 pokemones:", equipo2, 2));

        // Columna de configuración
        JPanel config = new JPanel(new GridLayout(0, 1, 10, 10));
        config.setBorder(BorderFactory.createTitledBorder("Enfrentamiento"));
        config.add(new JLabel("Enfrentamiento de a (Pokémon por jugador):"));
        config.add(spinner);
        JButton empezar = new JButton("Empezar");
        JButton volver = new JButton("Volver");
        config.add(empezar);
        config.add(volver);
        add(config);

        spinner.addChangeListener(e -> controller.configurarLmiteEquipo((Integer) spinner.getValue()));
        empezar.addActionListener(e -> empezar());
        volver.addActionListener(e -> frame.mostrar(MainFrame.MENU));
    }

    private JPanel columnaJugador(String titulo, JPanel panelEquipo, int num) {
        JPanel col = new JPanel(new BorderLayout(5, 5));
        col.setBorder(BorderFactory.createTitledBorder(titulo));
        col.add(new JScrollPane(panelEquipo), BorderLayout.CENTER);
        JButton elegir = new JButton("Elegir");
        elegir.addActionListener(e -> elegir(num == 1 ? controller.getJugador1() : controller.getJugador2(), num));
        col.add(elegir, BorderLayout.SOUTH);
        return col;
    }

    public void setController(GameController c) { this.controller = c; }

    // ---------- Diálogo de selección ----------
    private void elegir(Player jugador, int num) {
        int cupo = controller.getMaxPokesPorJugador() - jugador.getPokebichos().size();
        if (cupo <= 0) {
            JOptionPane.showMessageDialog(this, "El equipo ya está completo");
            return;
        }
        List<Pokemon> disponibles = new ArrayList<>(controller.getPokeRancho());
        if (disponibles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El rancho está vacío. Captura Pokémon primero.");
            return;
        }

        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Jugador " + num + " elige pokes", Dialog.ModalityType.APPLICATION_MODAL);
        DefaultListModel<Pokemon> modelo = new DefaultListModel<>();
        disponibles.forEach(modelo::addElement);
        JList<Pokemon> lista = new JList<>(modelo);
        lista.setCellRenderer((l, p, i, sel, foc) -> {
            JLabel lb = new JLabel(p.getNombre() + "  [" + p.getTipo() + "]  HP " + p.getHpInicial()
                    + "  Atk " + p.getAttack() + "  Def " + p.getDefense() + "  Spd " + p.getSpeed());
            lb.setOpaque(true);
            lb.setBackground(sel ? l.getSelectionBackground() : l.getBackground());
            return lb;
        });

        JLabel contador = new JLabel("Seleccionados: 0 / " + cupo);
        lista.addListSelectionListener(e -> contador.setText("Seleccionados: " + lista.getSelectedValuesList().size() + " / " + cupo));

        JButton aceptar = new JButton("Aceptar");
        aceptar.addActionListener(e -> {
            List<Pokemon> sel = lista.getSelectedValuesList();
            if (sel.isEmpty() || sel.size() > cupo) {
                JOptionPane.showMessageDialog(d, "Elige entre 1 y " + cupo + " Pokémon");
                return;
            }
            sel.forEach(p -> controller.equiparPokemon(jugador, p));
            d.dispose();
        });

        d.setLayout(new BorderLayout(5, 5));
        d.add(new JLabel("  Jugador " + num + " elige pokes (Ctrl+clic para varios)"), BorderLayout.NORTH);
        d.add(new JScrollPane(lista), BorderLayout.CENTER);
        JPanel sur = new JPanel(new BorderLayout());
        sur.add(contador, BorderLayout.WEST);
        sur.add(aceptar, BorderLayout.EAST);
        d.add(sur, BorderLayout.SOUTH);
        d.setSize(450, 350);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void empezar() {
        int max = controller.getMaxPokesPorJugador();
        if (controller.getJugador1().getPokebichos().size() != max
                || controller.getJugador2().getPokebichos().size() != max) {
            JOptionPane.showMessageDialog(this, "Cada jugador debe tener " + max + " Pokémon");
            return;
        }
        frame.prepararBatalla();
        frame.mostrar(MainFrame.BATALLA);
        controller.iniciarGuerra(1700);
    }

    /** Lo llama MainFrame al mostrar la pantalla y cuando cambian los equipos. */
    public void refrescarEquipos() {
        pintar(equipo1, controller.getJugador1().getPokebichos());
        pintar(equipo2, controller.getJugador2().getPokebichos());
        boolean vacios = controller.getJugador1().getPokebichos().isEmpty()
                && controller.getJugador2().getPokebichos().isEmpty();
        spinner.setEnabled(vacios); // no cambiar el límite con equipos armados
    }

    private void pintar(JPanel panel, List<Pokemon> equipo) {
        panel.removeAll();
        for (Pokemon p : new ArrayList<>(equipo)) panel.add(PokeCard.crear(p, false));
        panel.revalidate();
        panel.repaint();
    }
}