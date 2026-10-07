
package events;
import model.Player;
import model.Pokemon;
import java.util.List;

public interface GameControlleListener {
    //esto lo devuelve el motor engine que quita los pokemones de los jugadores y los devuelve
    void devolverPokesAlrancho(List<Pokemon> devolucionPokebichos);
    /**
     * Se dispara cuando un Pokémon regresa individualmente al Rancho
     * (por ejemplo, tras sobrevivir a una batalla o ser desequipado).
     */
    /**
     * Se dispara cuando un Pokémon ingresa por primera vez desde PokéAPI.
     */
    void onPokemonAgregado(Pokemon pokemon, List<Pokemon> ranchoActualizado);

    /**
     * Se dispara cuando el Pokémon ya existía en el Rancho y fue liberado.
     * aqui deberia de ir un if al evaluar de que este en el rancho
     */
    void onPokemonDuplicadoLiberado(String nombrePokemon);

    /**
     * Se dispara al finalizar la descarga masiva por lotes.
     */
    void onCargaLoteCompletada(int agregados, int duplicadosLiberados);

    void onPokemonEquipado(Player jugador, Pokemon pokemon, int posicion);

    //void onPokemonDesequipado(Player jugador, Pokemon pokemon);
}