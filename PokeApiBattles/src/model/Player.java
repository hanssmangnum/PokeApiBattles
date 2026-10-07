package model;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String nombre;
    private List<Pokemon> pokebichos;


    public Player(String nombre, List<Pokemon> pokebichos) {
        this.nombre = nombre;
        this.pokebichos = pokebichos;
    }

    public Player(String nombre) {
        this.nombre = nombre;
        this.pokebichos = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Pokemon> getPokebichos() {
        return pokebichos;
    }

    public void setPokebichos(List<Pokemon> pokebichos) {
        this.pokebichos = pokebichos;
    }

    public Pokemon seleccionarPokemon(){
        return pokebichos.get(0);
    }
}
