package model;

import events.GameControlleListener;
import events.BattleListener;
import model.BattleEngine;
import model.Client;
import model.Pokemon;
import model.Player;

import javax.swing.SwingWorker;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GameController {

    private final List<Pokemon> pokeRancho = new ArrayList<>();
    private final Set<String> nombresEnRancho = new HashSet<>(); // Control estricto de duplicados

    private final Player jugador1 = new Player("Jugador 1");
    private final Player jugador2 = new Player("Jugador 2");

    private final Client apiService = new Client();

    private GameControlleListener controllerListener;
    private final BattleListener battleListener;
    private BattleEngine battleEngine;

    // Variables de configuración de partida
    private int maxPokesPorJugador = 3; // Valor por defecto ajustable desde la UI
    private Player jugadorEnTurnoActual = null; // Para controlar el POV en la UI

    public GameController(BattleListener battleListener) {
        this.battleListener = battleListener;
    }

    public void setGameControllerListener(GameControlleListener listener) {
        this.controllerListener = listener;
    }

    // --- CONFIGURACIÓN DE PARTIDA ---
    public void configurarLmiteEquipo(int maxPokes) {
        this.maxPokesPorJugador = maxPokes;
    }

    // --- ACCIONES ASÍNCRONAS (CONECTADAS A LOS BOTONES DE LA UI) ---

    /**
     * Trae un Pokémon único por ID o Nombre desde la UI sin congelar la pantalla.
     */
    public void capturarPokesAsync(String idONombre) {
        new SwingWorker<Pokemon, Void>() {
            @Override
            protected Pokemon doInBackground() {
                return apiService.obternerPokemonIdONombre(idONombre);
            }

            @Override
            protected void done() {
                try {
                    Pokemon pokemon = get();
                    if (pokemon != null) {
                        agregarAlRancho(pokemon);
                    } else {
                        if (controllerListener != null) {
                            controllerListener.onPokemonDuplicadoLiberado("No encontrado / Error de red");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error en la descarga asíncrona: " + e.getMessage());
                }
            }
        }.execute();
    }

    /**
     * Descarga un lote de 10 Pokémon aleatorios usando hilos secundarios.
     */
    public void capturarLotePoke() {
        new SwingWorker<List<Pokemon>, Void>() {
            @Override
            protected List<Pokemon> doInBackground() {
                return apiService.traerPokeLote(10);
            }

            @Override
            protected void done() {
                try {
                    List<Pokemon> lote = get();
                    int agregados = 0;
                    int duplicados = 0;

                    for (Pokemon p : lote) {
                        // Usamos la variante silenciosa para no saturar la UI con alertas individuales
                        if (agregarAlRanchoSilent(p)) {
                            agregados++;
                        } else {
                            duplicados++;
                        }
                    }

                    // Al terminar el lote, avisamos a la UI para que refresque todo de golpe
                    if (controllerListener != null) {
                        controllerListener.onCargaLoteCompletada(agregados, duplicados);
                    }
                } catch (Exception e) {
                    System.err.println("Error al procesar el lote: " + e.getMessage());
                }
            }
        }.execute();
    }

    // --- GESTIÓN INTERNA DEL RANCHO (REGLAS DE NEGOCIO) ---

    /**
     * Agrega un Pokémon informando inmediatamente a la UI (útil para búsquedas individuales).
     */
    private synchronized boolean agregarAlRancho(Pokemon pokemon) {
        String nombreClave = pokemon.getNombre().toLowerCase().trim();

        if (nombresEnRancho.contains(nombreClave)) {
            if (controllerListener != null) {
                controllerListener.onPokemonDuplicadoLiberado(pokemon.getNombre());
            }
            return false;
        }

        pokeRancho.add(pokemon);
        nombresEnRancho.add(nombreClave);

        if (controllerListener != null) {
            controllerListener.onPokemonAgregado(pokemon, new ArrayList<>(pokeRancho));
        }
        return true;
    }

    /**
     * Agrega un Pokémon sin disparar alertas individuales (ideal para cargas masivas por lotes).
     */
    private synchronized boolean agregarAlRanchoSilent(Pokemon pokemon) {
        String nombreClave = pokemon.getNombre().toLowerCase().trim();

        if (nombresEnRancho.contains(nombreClave)) {
            return false;
        }

        pokeRancho.add(pokemon);
        nombresEnRancho.add(nombreClave);
        return true;
    }

    /**
     * Método invocado automáticamente cuando el BattleEngine termina la pelea.
     * Recibe la lista de Pokémon con su HP ya restablecido por el motor.
     */
    public void devolverPokesAlrancho(List<Pokemon> devolucionPokebichos) {
        // El motor ya hizo el trabajo sucio, solo los reinsertamos en el Rancho
        for (Pokemon p : devolucionPokebichos) {
            String nombreClave = p.getNombre().toLowerCase().trim();
            if (!nombresEnRancho.contains(nombreClave)) {
                pokeRancho.add(p);
                nombresEnRancho.add(nombreClave);
            }
        }

        // Vaciamos los equipos de combate para la próxima partida
        jugador1.getPokebichos().clear();
        jugador2.getPokebichos().clear();
        this.jugadorEnTurnoActual = null;

        // Le avisamos a la UI para que redibuje el Rancho con todos sus integrantes de vuelta
        if (controllerListener != null) {
            controllerListener.devolverPokesAlrancho(new ArrayList<>(pokeRancho));
        }
    }

    // --- GESTIÓN DE EQUIPOS (DESDE LA UI DE SELECCIÓN) ---

    /**
     * Añade un Pokémon del rancho al equipo de un jugador específico validando los límites.
     */
    public boolean equiparPokemon(Player jugador, Pokemon pokemon) {
        if (jugador.getPokebichos().size() >= maxPokesPorJugador) {
            return false; // Equipo lleno según la configuración de la partida
        }

        if (!pokeRancho.contains(pokemon)) {
            return false; // Ya no está disponible en el rancho
        }

        jugador.getPokebichos().add(pokemon);
        pokeRancho.remove(pokemon);
        nombresEnRancho.remove(pokemon.getNombre().toLowerCase().trim());
        int posicion = jugador.getPokebichos().size();
        controllerListener.onPokemonEquipado(jugador,pokemon,posicion);
        return true;
    }

    // --- CONTROL DE INICIO DE BATALLA Y CONTROL DE POV ---

    /**
     * Prepara el motor de batalla e inicia el combate en un hilo separado.
     */
    public void iniciarGuerra(int delayMs) {
        if (jugador1.getPokebichos().isEmpty() || jugador2.getPokebichos().isEmpty()) {
            return;
        }

        // 1. Instanciamos el motor normalmente con su listener visual
        this.battleEngine = new BattleEngine(battleListener, delayMs);

        // 2. Arrancamos el hilo secundario del combate
        new Thread(() -> {
            // El motor ejecuta todo el bucle de la pelea aquí adentro (Se detiene hasta que alguien gane)
            battleEngine.battle(jugador1, jugador2);

            // =========================================================================
            // ¡LA SOLUCIÓN! Cuando la función 'battle' de arriba termina, significa que la pelea acabó.
            // En lugar de que el motor nos avise, el controlador va y toma la lista directamente.
            // =========================================================================
            List<Pokemon> pokesRestaurados = battleEngine.getPokemonesDevolucion();

            // Llamamos a tu método de devolución pasándole la lista limpia
            devolverPokesAlrancho(pokesRestaurados);

        }).start();
    }

    /**
     * Actualiza el jugador que tiene la iniciativa o el turno activo en este instante.
     * La UI llamará a esto dentro de `onTurn` para decidir si renderiza el POV del Jugador 1 o del Jugador 2.
     */
    public void actualizarJugadorEnTurno(Player jugadorAtacante) {
        this.jugadorEnTurnoActual = jugadorAtacante;
    }

    /**
     * Le dice a la UI qué jugador está atacando para alternar los sprites traseros/frontales.
     */
    public Player getJugadorEnTurnoActual() {
        return jugadorEnTurnoActual;
    }

    public int getMaxPokesPorJugador() { return maxPokesPorJugador; }
    public List<Pokemon> getPokeRancho() { return pokeRancho; }
    public Player getJugador1() { return jugador1; }
    public Player getJugador2() { return jugador2; }
}