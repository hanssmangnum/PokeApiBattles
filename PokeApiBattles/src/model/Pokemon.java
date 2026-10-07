package model;

import javax.swing.*;

public class Pokemon {

    private String nombre;
    private String tipo;

    //Estadisticas
    private int hpInicial;
    private int hp;
    private int attack;
    private int defense;
    private int speed;

    //Imagenes del pokebicho

    private String spritefrontal;
    private String spriteTrasero;

    //constructor
    public Pokemon(){}
    public Pokemon(String nombre,
                   String id,
                   String tipo,
                   int HP,
                   int attack,
                   int defense,
                   int speed,
                   String spritefrontal,
                   String spriteTrasero) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.hp = HP;
        this.hpInicial =HP;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.spritefrontal = spritefrontal;
        this.spriteTrasero = spriteTrasero;
    }

    //Metodos setters and getters

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getHp() {
        return hp;
    }
    public int getHpInicial(){
        return hpInicial;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getSpritefrontal() {
        return spritefrontal;
    }

    public void setSpritefrontal(String spritefrontal) {
        this.spritefrontal = spritefrontal;
    }

    public String getSpriteTrasero() {
        return spriteTrasero;
    }

    public void setSpriteTrasero(String spriteTrasero) {
        this.spriteTrasero = spriteTrasero;
    }

    public void recibirDaño(int daño) {
        this.hp -= daño;
        if (this.hp < 0) {
            this.hp = 0;
        }
    }

    public void restaurarHp() {
        this.hp = this.hpInicial;
    }

}
