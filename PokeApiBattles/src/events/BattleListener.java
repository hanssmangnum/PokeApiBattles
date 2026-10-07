package events;

import model.Player;
import model.Pokemon;

public interface BattleListener {
    // Envía los objetos completos de jugadores y Pokémon involucrados en el turno
    void onTurn(Player jugadorAtacante, Pokemon atacante, Player jugadorDefensor, Pokemon defensor, int dano, boolean esCritico, double multiplicador);

    // Envía el jugador y su Pokémon actualizado para refrescar las barras de HP
    void onHpChanged(Player jugador, Pokemon pokemon, int hpActual);

    // Evento para cuando un jugador debe cambiar de Pokémon (Relevo)
    void onPokemonChanged(Player jugador, Pokemon nuevoPokemon);

    // Mensajes generales del log
    void mensajeBattle(String mensaje);

    // Notificación del fin de batalla con el jugador ganador (o null si es empate)
    void onBattleEnded(Player ganador, String mensajeResultado);
}