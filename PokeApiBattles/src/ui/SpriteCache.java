package ui;

import javax.swing.*;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpriteCache {
    private static final Map<String, ImageIcon> cache = new ConcurrentHashMap<>();

    /** Pone el icono en el label cuando termina de descargar (no bloquea el EDT). */
    public static void cargar(String url, JLabel destino) {
        if (url == null || url.isBlank()) return;

        ImageIcon cacheado = cache.get(url);
        if (cacheado != null) {
            destino.setIcon(cacheado);
            return;
        }

        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                return new ImageIcon(new URL(url));
            }
            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    cache.put(url, icon);
                    destino.setIcon(icon);
                } catch (Exception ignored) {
                    destino.setText("?");
                }
            }
        }.execute();
    }
}