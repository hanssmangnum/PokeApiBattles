package ui;

import events.BattleListener;
import model.GameController;
import model.Player;
import model.Pokemon;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BattlePanel extends JPanel implements BattleListener {
    private final MainFrame frame;
    private GameController controller;


    private final JLabel imgDef = new JLabel("", SwingConstants.CENTER);
    private final JLabel imgAtk = new JLabel("", SwingConstants.CENTER);
    private final JProgressBar hpDef = new JProgressBar(0, 100);
    private final JProgressBar hpAtk = new JProgressBar(0, 100);
    private final JLabel statDef = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel statAtk = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel lblTurno = new JLabel("Jugador en turno: ---", SwingConstants.CENTER);
    private final JTextArea log = new JTextArea();
    private final JPanel franjaEquipo = new JPanel(new FlowLayout(FlowLayout.LEFT));

    private final Deslizable arriba = new Deslizable();
    private final Deslizable abajo = new Deslizable();

    private Pokemon pDef, pAtk; // solo se tocan en el EDT

    public BattlePanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new GridLayout(1, 2, 10, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        hpDef.setStringPainted(true);
        hpAtk.setStringPainted(true);

        // Izquierda: defensor (frente) arriba, atacante (espaldas) abajo

        //arriba.add(statDef, BorderLayout.NORTH);
        JPanel datosDef = new JPanel(new GridLayout(0, 1));
        datosDef.add(statDef);
        datosDef.add(hpDef);
        arriba.add(datosDef, BorderLayout.NORTH);
        arriba.add(imgDef, BorderLayout.CENTER);
        arriba.setBorder(BorderFactory.createLineBorder(Color.GRAY));


        abajo.add(imgAtk, BorderLayout.CENTER);
        JPanel info = new JPanel(new GridLayout(0, 1));
        info.add(statAtk);
        info.add(hpAtk);
        info.add(lblTurno);
        abajo.add(info, BorderLayout.SOUTH);
        abajo.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JPanel izquierda = new JPanel(new GridLayout(2, 1, 0, 10));
        izquierda.add(arriba);
        izquierda.add(abajo);

        // Derecha: log arriba, equipo abajo
        log.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(log);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Log"));
        JScrollPane scrollEquipo = new JScrollPane(franjaEquipo);
        scrollEquipo.setBorder(BorderFactory.createTitledBorder("Pokemones del jugador"));

        JPanel derecha = new JPanel(new GridLayout(2, 1, 0, 10));
        derecha.add(scrollLog);
        derecha.add(scrollEquipo);

        add(izquierda);
        add(derecha);
    }

    public void setController(GameController c) { this.controller = c; }

    public void reset() {
        log.setText("");
        imgDef.setIcon(null); imgAtk.setIcon(null);
        statDef.setText(" "); statAtk.setText(" ");
        lblTurno.setText("Jugador en turno: ---");
        franjaEquipo.removeAll();
        franjaEquipo.repaint();
        pDef = null; pAtk = null;
        hpDef.setValue(0); hpAtk.setValue(0);
        hpDef.setString(""); hpAtk.setString("");
    }

    private String stats(Pokemon p, int hp) {
        return "<html><center><b>" + p.getNombre() + "</b><br>HP " + Math.max(0, hp) + "/" + p.getHpInicial()
                + " | Def " + p.getDefense() + " | Atk " + p.getAttack() + " | Speed " + p.getSpeed()
                + "</center></html>";
    }

    private void barra(JProgressBar b, Pokemon p, int hp){
        int max = Math.max(1,p.getHpInicial());
        int v = Math.max(0, hp);
        b.setMaximum(max);
        b.setValue(v);
        b.setString(v + "/" + max);
        double pct = (double) v / max;
        b.setForeground(pct > 0.5 ? new Color(60, 179,75):pct > 0.2 ? Color.ORANGE : Color.red);

    }

    private void animarBarra(JProgressBar b, Pokemon p, int hpNuevo) {
        int max = Math.max(1, p.getHpInicial());
        int destino = Math.max(0, hpNuevo);
        b.setMaximum(max);

        // si ya hay una animación corriendo en esta barra, la cancelamos
        Object previo = b.getClientProperty("anim");
        if (previo instanceof javax.swing.Timer) ((javax.swing.Timer) previo).stop();

        javax.swing.Timer t = new javax.swing.Timer(20, null);
        t.addActionListener(e -> {
            int actual = b.getValue();
            if (actual == destino) { t.stop(); return; }
            int paso = Math.max(1, Math.abs(actual - destino) / 12); // más rápido si la caída es grande
            int nuevo = actual > destino ? Math.max(destino, actual - paso)
                    : Math.min(destino, actual + paso);
            b.setValue(nuevo);
            b.setString(nuevo + "/" + max);
            double pct = (double) nuevo / max;
            b.setForeground(pct > 0.5 ? new Color(60, 179, 75) : pct > 0.2 ? Color.ORANGE : Color.RED);
        });
        b.putClientProperty("anim", t);
        t.start();
    }

    // ---------- BattleListener (llegan desde el hilo de la batalla) ----------
    @Override
    public void onTurn(Player ja, Pokemon atk, Player jd, Pokemon def, int dano, boolean crit, double mult) {
        List<Pokemon> equipo = new ArrayList<>(ja.getPokebichos());
        SwingUtilities.invokeLater(() -> {
            final int w = Math.max(arriba.getWidth(), 300);

            Runnable entrar = () -> {
                controller.actualizarJugadorEnTurno(ja);
                pDef = def; pAtk = atk;

                // contenido nuevo, mientras los paneles están fuera de vista
                arriba.setOffsetX(w);     // el defensor entra por la derecha
                abajo.setOffsetX(-w);     // el atacante entra por la izquierda

                imgDef.setIcon(null); imgAtk.setIcon(null);
                SpriteCache.cargar(def.getSpritefrontal(), imgDef);
                SpriteCache.cargar(atk.getSpriteTrasero(), imgAtk);
                statDef.setText(stats(def, def.getHp()));
                statAtk.setText(stats(atk, atk.getHp()));
                barra(hpDef, def, Math.min(def.getHpInicial(), def.getHp() + dano));
                barra(hpAtk, atk, atk.getHp());
                lblTurno.setText("Jugador en turno: " + ja.getNombre());

                franjaEquipo.removeAll();
                for (Pokemon p : equipo) franjaEquipo.add(PokeCard.crear(p, p == atk));
                franjaEquipo.revalidate();
                franjaEquipo.repaint();

                arriba.deslizar(w, 0, 350, () -> animarBarra(hpDef, def, def.getHp()));
                abajo.deslizar(-w, 0, 350, null);

                String extra = (crit ? " ¡CRÍTICO!" : "")
                        + (mult > 1 ? " Súper efectivo" : mult < 1 ? " Poco efectivo" : "");
                final String linea = atk.getNombre() + " hace " + dano + " de daño a " + def.getNombre() + "." + extra;
                javax.swing.Timer t = new javax.swing.Timer(1000, e -> {
                    log.append(linea + "\n");
                    log.setCaretPosition(log.getDocument().getLength());
                });
                t.setRepeats(false);
                t.start();
            };

            if (pDef == null) {            // primer turno: solo entrada
                entrar.run();
            } else {                       // salida y luego entrada
                arriba.deslizar(0, w, 300, null);     // el de arriba sale por la derecha
                abajo.deslizar(0, -w, 300, entrar);   // el de abajo sale por la izquierda
            }
        });
    }

    @Override
    public void onHpChanged(Player j, Pokemon p, int hpActual) {
        SwingUtilities.invokeLater(() -> {
            if (p == pDef) statDef.setText(stats(p, hpActual));   // sin barra: la anima onTurn
            else if (p == pAtk) statAtk.setText(stats(p, hpActual));
        });
    }

    @Override
    public void onPokemonChanged(Player j, Pokemon nuevo) {
        SwingUtilities.invokeLater(() -> {
            pDef = nuevo; // el que entra es el nuevo defensor
            imgDef.setIcon(null);
            SpriteCache.cargar(nuevo.getSpritefrontal(), imgDef);
            statDef.setText(stats(nuevo, nuevo.getHp()));
            barra(hpDef, nuevo, nuevo.getHp());
        });
    }

    @Override
    public void mensajeBattle(String msg) {
        SwingUtilities.invokeLater(() -> {
            log.append(msg + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        });
    }

    @Override
    public void onBattleEnded(Player ganador, String msg) {
        SwingUtilities.invokeLater(() -> {
            Sonido.detenerMusica();
            String texto = ganador == null ? "EMPATE" : "¡Ganó " + ganador.getNombre() + "!";
            Sonido.play("assets/sonidoVictoria.wav",2.0f);
            JOptionPane.showMessageDialog(this, texto + "\n" + msg);
            frame.mostrar(MainFrame.ENFRENTAMIENTOS);
        });
    }

}