package model;

import model.Pokemon;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Client {
    private final HttpClient httpClient;
    // CORREGIDO: Se añade "pokemon/" para apuntar al endpoint correcto
    private static final String BASE_URL = "https://pokeapi.co/api/v2/pokemon/";

    public Client (){
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public Pokemon obternerPokemonIdONombre(String idONombre){
        if(idONombre == null || idONombre.trim().isEmpty()){
            return null;
        }
        String targetUrl = BASE_URL + idONombre.trim().toLowerCase();
        try{
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() == 200){
                return parserPokemon(response.body());
            } else {
                System.err.println("Error http " + response.statusCode() + " al buscar: " + idONombre);
            }
        } catch (Exception e) {
            System.err.println("Error de red o petición al consultar la PokeApi: " + e.getMessage());
        }
        return null;
    }

    private Pokemon parserPokemon (String json){
        try {
            String finBuscado = "\"";
            String claveBuscar = "\"types\":[{\"slot\":1,\"type\":{\"name\":\"";

            // CORREGIDO: El ID en JSON es numérico, termina en coma "," no en comilla
            String id = extraerValorJson(json, "\"id\":", ",").replaceAll("[^0-9]", "");
            String nombre = extraerValorJson(json, "\"species\":{\"name\":\"", finBuscado);
            String tipo = extraerValorJson(json, claveBuscar, finBuscado);

            // Capitalizar la primera letra del nombre
            if (nombre.length() > 0) {
                nombre = nombre.substring(0, 1).toUpperCase() + nombre.substring(1);
            }

            // Extracción de Stats
            int hp = extraerStat(json, "hp");
            int ataque = extraerStat(json, "attack");
            int defensa = extraerStat(json, "defense");
            int speed = extraerStat(json, "speed");

            String spriteFrontUrl = "";
            String spriteBackUrl = "";

            // 1. Intentamos buscar la sección de los GIFs animados ("showdown")
            int inicioShowdown = json.indexOf("\"showdown\":");

            if (inicioShowdown != -1) {
                String jsonShowdown = json.substring(inicioShowdown);
                spriteFrontUrl = extraerValorJson(jsonShowdown, "\"front_default\":\"", finBuscado);
                spriteBackUrl = extraerValorJson(jsonShowdown, "\"back_default\":\"", finBuscado);
            }

            // 2. CONDICIÓN DE RESPALDO: Frontal PNG estático
            if (spriteFrontUrl.isEmpty() || spriteFrontUrl.equals("null")) {
                spriteFrontUrl = extraerValorJson(json, "\"front_default\":\"", finBuscado);
            }

            // 3. CONDICIÓN DE RESPALDO: Trasero PNG estático
            if (spriteBackUrl.isEmpty() || spriteBackUrl.equals("null")) {
                spriteBackUrl = extraerValorJson(json, "\"back_default\":\"", finBuscado);
            }

            return new Pokemon(nombre, id, tipo, hp, ataque, defensa, speed, spriteFrontUrl, spriteBackUrl);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String extraerValorJson(String json, String clave, String delimitadorFin){
        int posInicio = json.indexOf(clave);
        if (posInicio == -1) return "";
        posInicio += clave.length();
        int posFin = json.indexOf(delimitadorFin, posInicio);
        if (posFin == -1) return "";
        return json.substring(posInicio, posFin).trim();
    }

    private int extraerStat(String json, String nombreStat){
        String claveStat = "\"name\":\"" + nombreStat + "\"";
        int posStat = json.indexOf(claveStat);
        if (posStat == -1) return 50;

        int posBaseStat = json.lastIndexOf("\"base_stat\":", posStat);
        if (posBaseStat == -1) return 50;

        posBaseStat += "\"base_stat\":".length();
        int posComa = json.indexOf(",", posBaseStat);
        return Integer.parseInt(json.substring(posBaseStat, posComa).trim());
    }

    public List<Pokemon> traerPokeLote(int cantidad) {
        List<Pokemon> pokemonesCapturados = new ArrayList<>();
        if (cantidad > 151) cantidad = 151;

        for (int i = 0; i < cantidad; i++) {
            int idAzar = ThreadLocalRandom.current().nextInt(1, 152);

            try {
                Pokemon pokemonObtenido = obternerPokemonIdONombre(String.valueOf(idAzar));

                if (pokemonObtenido != null) {
                    pokemonesCapturados.add(pokemonObtenido);
                } else {
                    i--; // Reintenta si la respuesta fue nula
                }
            } catch (Exception e) {
                System.err.println("Error al procesar el lote en el ciclo: " + e.getMessage());
                i--;
            }
        }
        return pokemonesCapturados;
    }
}