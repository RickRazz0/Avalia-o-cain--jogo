package jogo.obstaculos;

import java.awt.Color;
import java.awt.Graphics;
import jogo.base.ElementoJogo;
import jogo.jogadores.Jogador;

public class Chegada extends ElementoJogo {

    public Chegada(int x, int y) {
        super(x, y - 10, 40, 60); // Hitbox levemente mais alta e larga para facilitar o toque!
    }

    @Override
    public void desenhar(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(getX(), getY(), 5, getAltura());
        g.setColor(Color.GREEN);
        g.fillRect(getX() + 5, getY(), 32, 28);
    }

    @Override
    public void aoColidir(Jogador jogador) {
        jogador.vencer();
    }
}