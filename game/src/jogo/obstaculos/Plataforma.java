package jogo.obstaculos;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import jogo.base.ElementoJogo;
import jogo.jogadores.Jogador;

public class Plataforma extends ElementoJogo {

    private Color cor;
    private String tipo; // Ex.: "CHAO", "MADEIRA", "DEGRAU"

    public Plataforma(int x, int y, int largura, int altura, Color cor) {
        this(x, y, largura, altura, cor, "MADEIRA");
    }

    public Plataforma(int x, int y, int largura, int altura, Color cor, String tipo) {
        super(x, y, largura, altura);
        this.cor = cor;
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }

    @Override
    public void desenhar(Graphics g) {
        g.setColor(cor);
        g.fillRect(getX(), getY(), getLargura(), getAltura());

        // Detalhe visual simples para diferenciar degrau/madeira
        g.setColor(cor.darker());
        g.drawRect(getX(), getY(), getLargura(), getAltura());
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