package jogo.obstaculos;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import jogo.base.ElementoJogo;
import jogo.jogadores.Jogador;

public class Plataforma extends ElementoJogo {

    private Color cor;

    public Plataforma(int x, int y, int largura, int altura, Color cor) {
        super(x, y, largura, altura);
        this.cor = cor;
    }

    @Override
    public void desenhar(Graphics g) {
        g.setColor(cor);
        g.fillRect(getX(), getY(), getLargura(), getAltura());
    }

    @Override
    public void aoColidir(Jogador jogador) {
        Rectangle rectJogador = jogador.getLimites();
        Rectangle rectPlataforma = this.getLimites();
        Rectangle intersecao = rectJogador.intersection(rectPlataforma);

        if (intersecao.width >= intersecao.height) {
            if (jogador.getY() < this.getY() && jogador.getVelocidadeY() >= 0) {
                jogador.setY(this.getY() - jogador.getAltura());
                jogador.setVelocidadeY(0);
                jogador.setNoChao(true);
            } else if (jogador.getY() > this.getY() && jogador.getVelocidadeY() < 0) {
                jogador.setY(this.getY() + this.getAltura());
                jogador.setVelocidadeY(0);
            }
        } else {
            if (jogador.getX() < this.getX()) {
                jogador.setX(this.getX() - jogador.getLargura());
            } else {
                jogador.setX(this.getX() + this.getLargura());
            }
        }
    }
}