package jogo.base;
import java.awt.Graphics;
import java.awt.Rectangle;

import jogo.jogadores.Jogador;

// ABSTRAÇÃO (Aula 05 / ScreenSaver): Molde genérico para qualquer peça do jogo
public abstract class ElementoJogo {

    // ENCAPSULAMENTO (Aula 02): Atributos privados em lowerCamelCase
    private int x;
    private int y;
    private int largura;
    private int altura;
    private int rotacao;

    public ElementoJogo(int x, int y, int largura, int altura) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.rotacao = 0;
    }

    // Métodos abstratos que obrigam as classes filhas a implementarem (Polimorfismo)
    public abstract void desenhar(Graphics g);
    public abstract void aoColidir(Jogador jogador);

    // Hitbox invisível para detecção de colisão
    public Rectangle getLimites() {
        return new Rectangle(x, y, largura, altura);
    }

    // Getters e Setters
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getLargura() { return largura; }
    public int getAltura() { return altura; }

    public int getRotacao() { return rotacao; }

    /** Gira 90 graus no sentido horário e atualiza a hitbox da peça. */
    public void girarHorario() {
        rotacao = (rotacao + 1) % 4;
        int dimensaoAnterior = largura;
        largura = altura;
        altura = dimensaoAnterior;
    }
}
