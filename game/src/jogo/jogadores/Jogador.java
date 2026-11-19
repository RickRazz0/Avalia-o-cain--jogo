package jogo.jogadores;

import java.awt.Color;
import java.awt.Graphics;
import jogo.base.ElementoJogo;

public class Jogador extends ElementoJogo {

    private int velocidadeX;
    private int velocidadeY;
    private boolean noChao;
    private Color cor;

    private boolean morreuNoRound;
    private boolean venceuNoRound;

    public Jogador(int x, int y, Color cor) {
        super(x, y, 30, 30);
        this.velocidadeX = 4; // Reduzido de 6 para 4 (controle mais preciso!)
        this.velocidadeY = 0;
        this.noChao = false;
        this.cor = cor;
        this.morreuNoRound = false;
        this.venceuNoRound = false;
    }

    public void aplicarGravidade() {
        // Se já venceu ou morreu neste round, congela o jogador imediatamente
        if (venceuNoRound || morreuNoRound) return;

        if (velocidadeY < 10) { // Queda máxima um pouco mais suave (era 12)
            velocidadeY += 1;
        }
        setY(getY() + velocidadeY);
        noChao = false;

        if (getY() > 600) {
            morrer();
        }
    }

    public void pular() {
        if (noChao) {
            velocidadeY = -14; // Pulo ajustado para acompanhar a nova velocidade (era -16)
            noChao = false;
        }
    }

    public void moverEsquerda() { setX(getX() - velocidadeX); }
    public void moverDireita()  { setX(getX() + velocidadeX); }

    public void morrer() {
        // PROTEÇÃO: Se o jogador já tocou na bandeira, ele NÃO pode morrer depois!
        if (this.venceuNoRound) return;
        this.morreuNoRound = true;
    }

    public void vencer() {
        // Se ainda não morreu, garante a vitória!
        if (!this.morreuNoRound) {
            this.venceuNoRound = true;
        }
    }

    public void resetarParaNovoRound() {
        setX(80);
        setY(350);
        velocidadeY = 0;
        noChao = false;
        morreuNoRound = false;
        venceuNoRound = false;
    }

    public int getVelocidadeY() { return velocidadeY; }
    public void setVelocidadeY(int velocidadeY) { this.velocidadeY = velocidadeY; }
    public void setNoChao(boolean noChao) { this.noChao = noChao; }
    public boolean isMorreuNoRound() { return morreuNoRound; }
    public boolean isVenceuNoRound() { return venceuNoRound; }

    @Override
    public void desenhar(Graphics g) {
        g.setColor(cor);
        g.fillRect(getX(), getY(), getLargura(), getAltura());
    }

    @Override
    public void aoColidir(Jogador jogador) {}
}