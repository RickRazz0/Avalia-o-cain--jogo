package jogo.obstaculos;
import java.awt.Color;
import java.awt.Graphics;

import jogo.base.ElementoJogo;
import jogo.jogadores.Jogador;

public class Armadilha extends ElementoJogo {

    private int velocidadeX; // Se for 0, é espinho parado; se for negativo, voa como flecha!
    private int xInicial;    // Guarda onde a flecha nasceu para ela reaparecer no canhão

    public Armadilha(int x, int y, int largura, int altura, int velocidadeX) {
        super(x, y, largura, altura);
        this.velocidadeX = velocidadeX;
        this.xInicial = x;
    }

    // Método para movimentar armadilhas móveis (como as flechas do desenho!)
    public void atualizar() {
        if (velocidadeX != 0) {
            setX(getX() + velocidadeX);
            // Se a flecha sair pela esquerda da tela, volta para o canhão!
            if (getX() < 200) {
                setX(xInicial);
            }
        }
    }

    @Override
    public void desenhar(Graphics g) {
        g.setColor(Color.ORANGE);
        g.fillRect(getX(), getY(), getLargura(), getAltura());
    }

    // POLIMORFISMO: Encostou na armadilha ou flecha = MORTE instantânea!
    @Override
    public void aoColidir(Jogador jogador) {
        System.out.println("Acertado por uma armadilha! Morreu!");
        jogador.morrer();
    }
}