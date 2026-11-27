package jogo.jogadores;

import java.awt.Color;
import java.awt.Graphics;
import jogo.base.ElementoJogo;

// [HERANÇA]: jogador é um ElementoJogo com física, entrada e estado de round.
public class Jogador extends ElementoJogo {

    // [ENCAPSULAMENTO]: estado privado evita que outras classes alterem a física sem regras.
    private final int velocidadeX;
    private int velocidadeY;
    private boolean noChao;
    private boolean solicitouPulo;
    private final Color cor;

    private boolean morreuNoRound;
    private boolean venceuNoRound;

    public Jogador(int x, int y, Color cor) {
        super(x, y, 30, 30);
        this.velocidadeX = 4;
        this.velocidadeY = 0;
        this.noChao = false;
        this.solicitouPulo = false;
        this.cor = cor;
        this.morreuNoRound = false;
        this.venceuNoRound = false;
    }

    // [ENCAPSULAMENTO]: registra a intenção; o salto é aplicado somente após as colisões.
    public void solicitarPulo() {
        this.solicitouPulo = true;
    }

    public void consumirPulo() {
        if (solicitouPulo) {
            if (noChao) {
                velocidadeY = -14;
                noChao = false;
            }
            solicitouPulo = false;
        }
    }

    // [ENCAPSULAMENTO]: integra a velocidade vertical e limita a queda máxima por frame.
    public void aplicarGravidade() {
        if (venceuNoRound || morreuNoRound) return;

        if (velocidadeY < 10) {
            velocidadeY += 1;
        }
        setY(getY() + velocidadeY);
        noChao = false;

        if (getY() > 720) {
            morrer();
        }
    }

    public void pular() {
        if (noChao) {
            velocidadeY = -14;
            noChao = false;
        }
    }

    public void moverEsquerda() { setX(getX() - velocidadeX); }
    public void moverDireita()  { setX(getX() + velocidadeX); }

    public void morrer() {
        if (this.venceuNoRound) return;
        this.morreuNoRound = true;
    }

    public void vencer() {
        if (!this.morreuNoRound) {
            this.venceuNoRound = true;
        }
    }

    public void resetarParaNovoRound(int xInicial) {
        setX(xInicial);
        setY(480);
        velocidadeY = 0;
        noChao = false;
        solicitouPulo = false;
        morreuNoRound = false;
        venceuNoRound = false;
    }

    public int getVelocidadeY() { return velocidadeY; }
    public void setVelocidadeY(int velocidadeY) { this.velocidadeY = velocidadeY; }
    public void setNoChao(boolean noChao) { this.noChao = noChao; }
    public boolean isMorreuNoRound() { return morreuNoRound; }
    public boolean isVenceuNoRound() { return venceuNoRound; }

    // [POLIMORFISMO]: implementação visual concreta do contrato de ElementoJogo.
    @Override
    public void desenhar(Graphics g) {
        g.setColor(cor);
        g.fillRect(getX(), getY(), getLargura(), getAltura());
    }

    @Override
    public void aoColidir(Jogador jogador) {}
}
