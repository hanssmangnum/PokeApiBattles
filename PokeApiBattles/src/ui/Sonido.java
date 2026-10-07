
package ui;

import javax.sound.sampled.*;
import java.net.URL;

public class Sonido {
    private static Clip musica;


    private static void aplicarVolumen(Clip clip, float vol) {
        try {
            FloatControl g = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float db = (float) (20.0 * Math.log10(Math.max(vol, 0.0001f)));
            g.setValue(Math.max(g.getMinimum(), Math.min(g.getMaximum(), db)));
        } catch (Exception ignored) {}
    }

    public static void musica(String archivo, float volumen) {
        new Thread(() -> {
            try {
                detenerMusica();
                URL url = Sonido.class.getResource("/" + archivo);
                if (url == null) return;
                musica = AudioSystem.getClip();
                musica.open(AudioSystem.getAudioInputStream(url));
                aplicarVolumen(musica, volumen);          // <- nuevo
                musica.loop(Clip.LOOP_CONTINUOUSLY);
            } catch (Exception ignored) {}
        }).start();
    }

    public static void detenerMusica() {
        if (musica != null) { musica.stop(); musica.close(); musica = null; }
    }

    public static void play(String archivo, float volumen) {
        new Thread(() -> {
            try {
                URL url = Sonido.class.getResource("/" + archivo);
                if (url == null) return;
                Clip clip = AudioSystem.getClip();
                clip.open(AudioSystem.getAudioInputStream(url));
                aplicarVolumen(clip, volumen);            // <- nuevo
                clip.addLineListener(ev -> {
                    if (ev.getType() == LineEvent.Type.STOP) clip.close();
                });
                clip.start();
            } catch (Exception ignored) {}
        }).start();
    }

    // Atajos con volumen por defecto
    public static void play(String archivo) { play(archivo, 1.0f); }
    public static void musica(String archivo) { musica(archivo, 0.4f); }
}
