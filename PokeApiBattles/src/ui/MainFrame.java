package ui;

import events.GameControlleListener;
import model.GameController;
import model.Player;
import model.Pokemon;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame implements GameControlleListener {
    public static final String MENU = "menu", BATALLA = "batalla",
            ENFRENTAMIENTOS = "enfrentamientos", RANCHO = "rancho";

    private final CardLayout cards = new CardLayout();
    private final JPanel container = new JPanel(cards);

    private final MenuPanel menuPanel;
    private final BattlePanel battlePanel;
    private final EnfrentamientosPanel enfrentamientosPanel;
    private final PokeRanchoPanel ranchoPanel;
    private final GameController controller;

    public MainFrame() {
        super("PokeApi Battles");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // 1) paneles primero (BattlePanel es el BattleListener)
        menuPanel = new MenuPanel(this);
        battlePanel = new BattlePanel(this);
        enfrentamientosPanel = new EnfrentamientosPanel(this);
        ranchoPanel = new PokeRanchoPanel(this);

        // 2) controller después, con el BattlePanel como listener
        controller = new GameController(battlePanel);
        controller.setGameControllerListener(this);

        // 3) se lo damos a quien lo necesita
        battlePanel.setController(controller);
        enfrentamientosPanel.setController(controller);
        ranchoPanel.setController(controller);

        container.add(menuPanel, MENU);
        container.add(battlePanel, BATALLA);
        container.add(enfrentamientosPanel, ENFRENTAMIENTOS);
        container.add(ranchoPanel, RANCHO);
        setContentPane(container);
        mostrar(MENU);
    }

    public void mostrar(String nombre) {
        if (nombre.equals(RANCHO)) ranchoPanel.refrescar(controller.getPokeRancho());
        if (nombre.equals(ENFRENTAMIENTOS)) enfrentamientosPanel.refrescarEquipos();

        switch (nombre) {
            case MENU:            Sonido.musica("assets/fondo.wav"); break;
            case RANCHO:          Sonido.musica("assets/rancho.wav"); break;
            case ENFRENTAMIENTOS: Sonido.musica("assets/fondo.wav"); break;
            case BATALLA:         Sonido.musica("assets/duelo.wav",0.8f); break;
            default:              Sonido.detenerMusica();
        }

        cards.show(container, nombre);
    }

    // ---------- GameControlleListener: todo en el EDT ----------
    @Override
    public void onPokemonAgregado(Pokemon p, List<Pokemon> rancho) {
        SwingUtilities.invokeLater(() -> {
            ranchoPanel.refrescar(rancho);
            ranchoPanel.mensaje("Capturado: " + p.getNombre());
        });
    }

    @Override
    public void onPokemonDuplicadoLiberado(String nombre) {
        SwingUtilities.invokeLater(() -> ranchoPanel.mensaje(nombre + " ya estaba en el rancho (liberado)"));
    }

    @Override
    public void onCargaLoteCompletada(int agregados, int duplicados) {
        SwingUtilities.invokeLater(() -> {
            ranchoPanel.refrescar(controller.getPokeRancho());
            ranchoPanel.mensaje("Lote: " + agregados + " nuevos, " + duplicados + " duplicados");
        });
    }

    @Override
    public void onPokemonEquipado(Player jugador, Pokemon p, int posicion) {
        SwingUtilities.invokeLater(() -> {
            ranchoPanel.refrescar(controller.getPokeRancho());
            enfrentamientosPanel.refrescarEquipos();
        });
    }

    @Override
    public void devolverPokesAlrancho(List<Pokemon> rancho) {
        SwingUtilities.invokeLater(() -> {
            ranchoPanel.refrescar(rancho);
            enfrentamientosPanel.refrescarEquipos();
        });
    }

    public void prepararBatalla() { battlePanel.reset(); }
}