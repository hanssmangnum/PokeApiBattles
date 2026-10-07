package model;

import events.BattleListener;
import events.GameControlleListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class BattleEngine {

    private BattleListener listener;
    private GameControlleListener listenerGamecontroller;
    private Random random = new Random();

    private Player jugador1;
    private Player jugador2;

    private Pokemon pokemonEnCombate1;
    private Pokemon pokemonEnCombate2;

    private List<Pokemon> pokemonesDevolucion = new ArrayList<>();
    private Map<Player, Pokemon> pokemonEnCombate = new HashMap<>();

    private Boolean batallaActiva;
    private boolean ultimoCritico = false;
    private int delayEntreTurnosMs = 1000; // 1 segundo de pausa entre acciones para la GUI

    public BattleEngine(BattleListener listener) {
        this.listener = listener;
    }

    //controlla el tiempo de espera con una variable externa a la clase
    public BattleEngine(BattleListener listener, int delayEntreTurnosMs) {
        this.listener = listener;
        this.delayEntreTurnosMs = delayEntreTurnosMs;
    }

    // --- GETTERS PARA CONSULTA DIRECTA DESDE LA GUI ---
    public Player getJugador1() {
        return jugador1;
    }

    public Player getJugador2() {
        return jugador2;
    }

    public Pokemon getPokemonEnCombate1() {
        return pokemonEnCombate.get(jugador1);
    }

    public Pokemon getPokemonEnCombate2() {
        return pokemonEnCombate.get(jugador2);
    }

    public Pokemon calculoIniciativa(Pokemon pokemonEnCombate1, Pokemon pokemonEnCombate2) {
        if (pokemonEnCombate1.getSpeed() > pokemonEnCombate2.getSpeed()) {
            return pokemonEnCombate1;
        } else if (pokemonEnCombate1.getSpeed() < pokemonEnCombate2.getSpeed()) {
            return pokemonEnCombate2;
        } else {
            int moneda = ThreadLocalRandom.current().nextInt(2);
            Pokemon primero = (moneda == 1) ? pokemonEnCombate1 : pokemonEnCombate2;

            if (this.listener != null) {
                listener.mensajeBattle(primero.getNombre() + " iniciará el combate (desempate por velocidad)");
            }
            return primero;
        }
    }

    private double obtenerEfectividadTipo(String tipoAtacante, String tipoDefensor) {
        if (tipoAtacante == null || tipoDefensor == null) return 1.0;

        String atk = tipoAtacante.toUpperCase();
        String def = tipoDefensor.toUpperCase();

        if ((atk.equals("WATER") && def.equals("FIRE")) ||
                (atk.equals("FIRE") && def.equals("GRASS")) ||
                (atk.equals("GRASS") && def.equals("WATER"))) {
            return 1.3;
        }

        if ((atk.equals("FUEGO") && def.equals("AGUA")) ||
                (atk.equals("PLANTA") && def.equals("FUEGO")) ||
                (atk.equals("AGUA") && def.equals("PLANTA"))) {
            return 0.7;
        }

        return 1.0;
    }

    public int calculoDaño(Pokemon pokeAtacante, Pokemon pokeDefensor) {
        int atkRand = (pokeAtacante.getAttack() > 0) ? random.nextInt(pokeAtacante.getAttack() + 1) : 0;
        int defRand = (pokeDefensor.getDefense() > 0) ? random.nextInt(pokeDefensor.getDefense() + 1) : 0;

        int danoBase = atkRand - defRand;
        if (danoBase < 1) {
            danoBase = 1;
        }

        boolean esCritico = random.nextDouble() < 0.10;
        ultimoCritico = esCritico;
        double multiplicadorCritico = esCritico ? 1.5 : 1.0;

        double multiplicadorTipo = obtenerEfectividadTipo(pokeAtacante.getTipo(), pokeDefensor.getTipo());

        int danoFinal = (int) Math.round(danoBase * multiplicadorCritico * multiplicadorTipo);
        return Math.max(1, danoFinal);
    }

    public void actualizacion(Player jugadorAtacante, Pokemon atacante, Player jugadorDefensor, Pokemon defensor, int daño) {
        defensor.recibirDaño(daño);

        boolean esCritico = ultimoCritico;
        double multiplicadorTipo = obtenerEfectividadTipo(atacante.getTipo(), defensor.getTipo());

        if (listener != null) {
            // Notifica el turno pasando jugadores y pokémones completos
            listener.onTurn(
                    jugadorAtacante,
                    atacante,
                    jugadorDefensor,
                    defensor,
                    daño,
                    esCritico,
                    multiplicadorTipo
            );

            // Notifica la actualización de la barra de vida enviando al dueño y su Pokémon
            listener.onHpChanged(jugadorDefensor, defensor, defensor.getHp());
        }
    }

    public String condicionVictoria(Player jugador1, Player jugador2) {
        boolean j1SinPuchamones = jugador1.getPokebichos().isEmpty();
        boolean j2SinPuchamones = jugador2.getPokebichos().isEmpty();

        if (j1SinPuchamones && j2SinPuchamones) {
            return "EMPATE";
        }

        if (j1SinPuchamones) {
            return jugador2.getNombre();
        }

        if (j2SinPuchamones) {
            return jugador1.getNombre();
        }

        return null; // Continúa la batalla
    }

    public Player obtenerDuenio(Pokemon pokemon) {
        if (pokemonEnCombate.get(jugador1) == pokemon) return jugador1;
        if (pokemonEnCombate.get(jugador2) == pokemon) return jugador2;
        return null;
    }

    private void aplicarPausa() {
        if (delayEntreTurnosMs > 0) {
            try {
                Thread.sleep(delayEntreTurnosMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void battle(Player jugador1, Player jugador2) {
        this.batallaActiva = true;
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
        this.pokemonesDevolucion.clear();

        if (!jugador1.getPokebichos().isEmpty() && !jugador2.getPokebichos().isEmpty()) {

            // Selección inicial
            pokemonEnCombate1 = jugador1.seleccionarPokemon();
            pokemonEnCombate2 = jugador2.seleccionarPokemon();

            pokemonEnCombate.put(jugador1, pokemonEnCombate1);
            pokemonEnCombate.put(jugador2, pokemonEnCombate2);

            // Determinación de iniciativa
            Pokemon atacante = calculoIniciativa(pokemonEnCombate1, pokemonEnCombate2);
            Pokemon defensor = (atacante == pokemonEnCombate1) ? pokemonEnCombate2 : pokemonEnCombate1;

            Player jugadorAtacante = obtenerDuenio(atacante);
            Player jugadorDefensor = obtenerDuenio(defensor);

            if (listener != null) {
                listener.mensajeBattle("¡Comienza la batalla entre " + jugador1.getNombre() + " y " + jugador2.getNombre() + "!");
                listener.mensajeBattle("¡" + atacante.getNombre() + " de " + jugadorAtacante.getNombre() + " toma la iniciativa!");
            }

            aplicarPausa();

            int ronda = 1;

            while (batallaActiva) {
                if (listener != null) {
                    listener.mensajeBattle("--- RONDA " + ronda + " ---");
                }

                // 1. Ejecución del turno
                int dañoRealizado = calculoDaño(atacante, defensor);
                actualizacion(jugadorAtacante, atacante, jugadorDefensor, defensor, dañoRealizado);

                aplicarPausa();

                // 2. Control de debilidad (HP <= 0)
                if (defensor.getHp() <= 0) {
                    if (listener != null) {
                        listener.mensajeBattle("¡" + defensor.getNombre() + " de " + jugadorDefensor.getNombre() + " se ha debilitado!");
                    }

                    pokemonesDevolucion.add(defensor);
                    jugadorDefensor.getPokebichos().remove(defensor);

                    // Relevo de Pokémon si al jugador le quedan más criaturas
                    if (!jugadorDefensor.getPokebichos().isEmpty()) {
                        Pokemon nuevoPokemon = jugadorDefensor.seleccionarPokemon();
                        pokemonEnCombate.put(jugadorDefensor, nuevoPokemon);

                        if (listener != null) {
                            listener.onPokemonChanged(jugadorDefensor, nuevoPokemon);
                            listener.mensajeBattle("¡" + jugadorDefensor.getNombre() + " envía a " + nuevoPokemon.getNombre() + " al combate!");
                        }

                        // El nuevo Pokémon pasa a ser el defensor listo para la siguiente ronda
                        defensor = nuevoPokemon;
                    }
                }

                // 3. Evaluar condición de victoria después de los relevos
                String resultado = condicionVictoria(jugador1, jugador2);
                if (resultado != null) {
                    this.batallaActiva = false;

                    // Añadir sobrevivientes finales a la lista de devolución
                    pokemonesDevolucion.addAll(jugador1.getPokebichos());
                    pokemonesDevolucion.addAll(jugador2.getPokebichos());

                    // Restaurar la vida completa de todos (vivos y debilitados) usando su HP base original
                    for (Pokemon p : pokemonesDevolucion) {
                        p.setHp(p.getHpInicial());
                    }

                    // Notificaciones de cierre a la UI
                    if (listenerGamecontroller != null) {
                        listenerGamecontroller.devolverPokesAlrancho(pokemonesDevolucion);
                    }

                    if (listener != null) {
                        Player ganador = resultado.equals("EMPATE") ? null : (resultado.equals(jugador1.getNombre()) ? jugador1 : jugador2);
                        listener.onBattleEnded(ganador, "La batalla ha concluido. Resultado: " + resultado);
                    }
                    break;
                }

                // Intercambio de roles para el siguiente turno si nadie ha ganado
                Pokemon auxiliar = atacante;
                atacante = defensor;
                defensor = auxiliar;

                jugadorAtacante = obtenerDuenio(atacante);
                jugadorDefensor = obtenerDuenio(defensor);

                ronda++;
                aplicarPausa();
            }
        }
    }

    public List<Pokemon> getPokemonesDevolucion() {
        return pokemonesDevolucion;
    }
}
//donde se devuelve el pokemon con la vida que le correspondia??