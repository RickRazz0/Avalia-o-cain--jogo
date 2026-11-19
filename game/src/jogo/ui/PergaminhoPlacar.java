package jogo.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class PergaminhoPlacar {

    private boolean aberto;
    private int larguraAtual;
    private final int LARGURA_MAX = 560;
    private final int ALTURA = 280;

    private int pontos;
    private int roundAtual;
    private boolean venceuRound;
    private int contadorPiscar;

    public PergaminhoPlacar() {
        this.aberto = false;
        this.larguraAtual = 0;
        this.pontos = 0;
        this.roundAtual = 1;
        this.venceuRound = false;
        this.contadorPiscar = 0;
    }

    public void abrir(boolean venceu) {
        this.aberto = true;
        this.larguraAtual = 20;
        this.venceuRound = venceu;

        if (venceu) {
            this.pontos++;
        }
    }

    public void avancarRound() {
        if (this.pontos >= 4) {
            this.pontos = 0;
            this.roundAtual = 1;
        } else {
            this.roundAtual++;
        }
        this.aberto = false;
        this.larguraAtual = 0;
    }

    public void atualizarAnimacao() {
        if (!aberto) return;

        if (larguraAtual < LARGURA_MAX) {
            larguraAtual += 28;
            if (larguraAtual > LARGURA_MAX) {
                larguraAtual = LARGURA_MAX;
            }
        }
        contadorPiscar++;
    }

    public void desenhar(Graphics2D g2d) {
        if (!aberto) return;

        g2d.setColor(new Color(0, 0, 0, 170));
        g2d.fillRect(0, 0, 800, 600);

        int centroX = 400;
        int xEsquerda = centroX - (larguraAtual / 2);
        int yTopo = 150;

        g2d.setColor(new Color(238, 214, 175));
        g2d.fillRoundRect(xEsquerda, yTopo, larguraAtual, ALTURA, 15, 15);

        g2d.setColor(new Color(160, 110, 60));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(xEsquerda + 8, yTopo + 8, Math.max(1, larguraAtual - 16), ALTURA - 16, 10, 10);

        g2d.setColor(new Color(101, 55, 24));
        g2d.fillRoundRect(xEsquerda - 12, yTopo - 15, 22, ALTURA + 30, 10, 10);
        g2d.fillRoundRect(xEsquerda + larguraAtual - 10, yTopo - 15, 22, ALTURA + 30, 10, 10);

        g2d.setColor(new Color(218, 165, 32));
        g2d.fillRect(xEsquerda - 12, yTopo - 15, 22, 8);
        g2d.fillRect(xEsquerda - 12, yTopo + ALTURA + 7, 22, 8);
        g2d.fillRect(xEsquerda + larguraAtual - 10, yTopo - 15, 22, 8);
        g2d.fillRect(xEsquerda + larguraAtual - 10, yTopo + ALTURA + 7, 22, 8);

        if (larguraAtual >= LARGURA_MAX - 60) {
            g2d.setColor(new Color(70, 40, 15));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 26));
            String titulo = (pontos >= 4) ? "🏆 VITÓRIA SUPREMA! 🏆" : "RESULTADO - ROUND " + roundAtual;
            g2d.drawString(titulo, 245, yTopo + 50);

            int barraX = 180;
            int barraY = yTopo + 115;
            int larguraSlot = 95;

            g2d.setColor(new Color(180, 155, 120));
            g2d.fillRoundRect(barraX, barraY, larguraSlot * 4, 36, 12, 12);

            if (pontos > 0) {
                g2d.setColor(new Color(46, 160, 67));
                g2d.fillRoundRect(barraX, barraY, larguraSlot * pontos, 36, 12, 12);
            }

            g2d.setColor(new Color(90, 55, 25));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
            for (int i = 1; i <= 4; i++) {
                int divisorX = barraX + (i * larguraSlot);
                g2d.drawLine(divisorX, barraY, divisorX, barraY + 36);
                String marcador = (i == 4) ? "4 ★" : String.valueOf(i);
                g2d.drawString(marcador, divisorX - 28, barraY + 24);
            }

            g2d.drawRoundRect(barraX, barraY, larguraSlot * 4, 36, 12, 12);

            int pontaDaBarraX = barraX + (larguraSlot * pontos);
            if (venceuRound) {
                g2d.setColor(new Color(20, 120, 40));
                g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
                g2d.drawString("+1 PONTO! (" + pontos + "/4)", pontaDaBarraX - 40, barraY - 12);
            } else {
                int ripX = Math.min(pontaDaBarraX + 10, barraX + (larguraSlot * 4) - 65);
                g2d.setColor(new Color(180, 30, 30));
                g2d.fillRoundRect(ripX, barraY - 6, 72, 48, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(ripX, barraY - 6, 72, 48, 10, 10);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
                g2d.drawString("☠ RIP", ripX + 8, barraY + 25);
            }

            if ((contadorPiscar / 20) % 2 == 0) {
                g2d.setColor(new Color(90, 50, 20));
                g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
                String instrucao = (pontos >= 4)
                    ? "Pressione [ESPAÇO] ou [ENTER] para jogar uma nova partida"
                    : "Pressione [ESPAÇO] ou [ENTER] para o próximo round";
                g2d.drawString(instrucao, 195, yTopo + 235);
            }
        }
    }

    public boolean isAberto() { return aberto; }
    public int getPontos() { return pontos; }
    public int getRoundAtual() { return roundAtual; }
}