package jogo.obstaculos;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;
import jogo.base.ElementoJogo;
import jogo.jogadores.Jogador;

public class Armadilha extends ElementoJogo {

    private int velocidadeX;
    private int xInicial;
    private String tipo; // "ESPINHO" ou "FLECHA"

    public Armadilha(int x, int y, int largura, int altura, int velocidadeX) {
        this(x, y, largura, altura, velocidadeX, velocidadeX != 0 ? "FLECHA" : "ESPINHO");
    }

    public Armadilha(int x, int y, int largura, int altura, int velocidadeX, String tipo) {
        super(x, y, largura, altura);
        this.velocidadeX = velocidadeX;
        this.xInicial = x;
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }

    // Move a flecha até bater em uma plataforma/arco sólido ou sair das bordas do mapa
    public void atualizar(List<ElementoJogo> pecasDoMapa) {
        if (velocidadeX == 0) return;

        setX(getX() + velocidadeX);

        boolean saiuDoMapa = (getX() + getLargura() < 0) || (getX() > 800);
        boolean bateuEmSolido = false;

        if (pecasDoMapa != null) {
            for (ElementoJogo peca : pecasDoMapa) {
                if (peca == this) continue;
                if (peca instanceof Plataforma || peca instanceof Arco) {
                    if (this.getLimites().intersects(peca.getLimites())) {
                        bateuEmSolido = true;
                        break;
                    }
                }
            }
        }

        if (saiuDoMapa || bateuEmSolido) {
            setX(xInicial);
        }
    }

    @Override
    public void desenhar(Graphics g) {
        if ("FLECHA".equals(tipo)) {
            g.setColor(Color.ORANGE);
            g.fillRect(getX(), getY() + 2, getLargura() - 6, Math.max(4, getAltura() - 4));
            // Ponta da flecha apontando para a esquerda ou direita conforme velocidadeX
            if (velocidadeX < 0) {
                int[] px = {getX(), getX() + 8, getX() + 8};
                int[] py = {getY() + getAltura() / 2, getY(), getY() + getAltura()};
                g.fillPolygon(px, py, 3);
            } else {
                int[] px = {getX() + getLargura(), getX() + getLargura() - 8, getX() + getLargura() - 8};
                int[] py = {getY() + getAltura() / 2, getY(), getY() + getAltura()};
                g.fillPolygon(px, py, 3);
            }
        } else {
            // Espinho letal
            g.setColor(new Color(235, 110, 0));
            g.fillRect(getX(), getY(), getLargura(), getAltura());
            g.setColor(Color.YELLOW);
            int passo = 10;
            for (int i = getX(); i < getX() + getLargura() - 4; i += passo) {
                int[] px = {i, i + passo / 2, i + passo};
                int[] py = {getY() + getAltura(), getY(), getY() + getAltura()};
                g.fillPolygon(px, py, 3);
            }
        }
    }

    @Override
    public void aoColidir(Jogador jogador) {
        jogador.morrer();
    }
}