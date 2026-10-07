package ui;

import javax.swing.*;
import java.awt.*;

public class Deslizable extends JPanel {
    private int offsetX = 0;

    public Deslizable() { super(new BorderLayout()); }

    @Override
    protected void paintChildren(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(offsetX, 0);
        super.paintChildren(g2);
        g2.dispose();
    }

    public void setOffsetX(int x) { offsetX = x; repaint(); }

    public void deslizar(int desde, int hasta, int ms, Runnable alTerminar) {
        final long ini = System.currentTimeMillis();
        javax.swing.Timer t = new javax.swing.Timer(15, null);
        t.addActionListener(e -> {
            double p = Math.min(1.0, (System.currentTimeMillis() - ini) / (double) ms);
            double suave = p * p * (3 - 2 * p); // arranca y frena suave
            setOffsetX((int) (desde + (hasta - desde) * suave));
            if (p >= 1.0) {
                t.stop();
                repaint();
                if (alTerminar != null) alTerminar.run();
            }
        });
        t.start();
    }
}