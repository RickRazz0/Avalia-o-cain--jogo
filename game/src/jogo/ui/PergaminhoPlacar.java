package jogo.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class PergaminhoPlacar {

    private boolean aberto;
    private int larguraAtual;
    private final int LARGURA_MAX = 580;
    private final int ALTURA = 320;

    private int pontosP1;
    private int pontosP2;
    private int roundAtual;
    private boolean alguemVenceuRound;
    private int contadorPiscar;

    public PergaminhoPlacar() {
        this.aberto = false;
        this.larguraAtual = 0;
        this.pontosP1 = 0;
        this.pontosP2 = 0;
        this.roundAtual = 1;
        this.alguemVenceuRound = false;
        this.contadorPiscar = 0;
    }

    public void abrir(boolean p1Venceu, boolean p2Venceu) {
        this.aberto = true;
        this.larguraAtual = 20;
        this.alguemVenceuRound = (p1Venceu || p2Venceu);

        if (p1Venceu && pontosP1 < 4) {
            pontosP1++;
        }
        if (p2Venceu && pontosP2 < 4) {
            pontosP2++;
        }
    }

    public boolean partidaFinalizada() {
        return pontosP1 >= 4 || pontosP2 >= 4;
    }

    public void avancarRound() {
        if (partidaFinalizada()) {
            pontosP1 = 0;
            pontosP2 = 0;
            roundAtual = 1;
        } else {
            roundAtual++;
        }
        aberto = false;
        larguraAtual = 0;
    }

    public void atualizarAnimacao() {
        if (!aberto) return;

        if (larguraAtual < LARGURA_MAX) {
            larguraAtual += 30;
            if (larguraAtual > LARGURA_MAX) {
                larguraAtual = LARGURA_MAX;
            }
        }
        contadorPiscar++;
    }

    private void desenharBarraJogador(Graphics2D g2d, String rotulo, int pontos, Color corBarra, int x, int y) {
        int larguraSlot = 85; // 4 slots * 85 = 340px

        g2d.setColor(new Color(70, 40, 15));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2d.drawString(rotulo + " (" + pontos + "/4)", x, y - 8);

        g2d.setColor(new Color(180, 155, 120));
        g2d.fillRoundRect(x, y, larguraSlot * 4, 28, 10, 10);

        if (pontos > 0) {
            g2d.setColor(corBarra);
            g2d.fillRoundRect(x, y, larguraSlot * pontos, 28, 10, 10);
        }

        g2d.setColor(new Color(90, 55, 25));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        for (int i = 1; i <= 4; i++) {
            int divisorX = x + (i * larguraSlot);
            g2d.drawLine(divisorX, y, divisorX, y + 28);
            String marcador = (i == 4) ? "4 ★" : String.valueOf(i);
            g2d.drawString(marcador, divisorX - 26, y + 19);
        }

        g2d.drawRoundRect(x, y, larguraSlot * 4, 28, 10, 10);
    }

    public void desenhar(Graphics2D g2d) {
        if (!aberto) return;

        g2d.setColor(new Color(0, 0, 0, 170));
        g2d.fillRect(0, 0, 800, 600);

        int centroX = 400;
        int xEsquerda = centroX - (larguraAtual / 2);
        int yTopo = 130;

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
            g2d.setFont(new Font("SansSerif", Font.BOLD, 24));

            String titulo = "RESULTADO - ROUND " + roundAtual;
            if (pontosP1 >= 4 && pontosP2 >= 4) {
                titulo = "🏆 EMPATE DE CAMPEÕES! 🏆";
            } else if (pontosP1 >= 4) {
                titulo = "🏆 PLAYER 1 (VERMELHO) VENCEU! 🏆";
            } else if (pontosP2 >= 4) {
                titulo = "🏆 PLAYER 2 (AZUL) VENCEU! 🏆";
            }
            g2d.drawString(titulo, 195, yTopo + 45);

            // Escala separada do Player 1 e do Player 2
            desenharBarraJogador(g2d, "Player 1 (Vermelho)", pontosP1, new Color(210, 50, 50), 170, yTopo + 95);
            desenharBarraJogador(g2d, "Player 2 (Azul)", pontosP2, new Color(45, 110, 220), 170, yTopo + 165);

            // Se ninguém pontuou neste round, exibe o carimbo RIP
            if (!alguemVenceuRound) {
                int ripX = 530;
                int ripY = yTopo + 120;
                g2d.setColor(new Color(180, 30, 30));
                g2d.fillRoundRect(ripX, ripY, 76, 48, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(ripX, ripY, 76, 48, 10, 10);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 19));
                g2d.drawString("☠ RIP", ripX + 9, ripY + 30);
            }

            if ((contadorPiscar / 20) % 2 == 0) {
                g2d.setColor(new Color(90, 50, 20));
                g2d.setFont(new Font("SansSerif", Font.BOLD, 15));
                String instrucao = partidaFinalizada()
                    ? "Pressione [ESPAÇO] ou [ENTER] para nova partida"
                    : "Pressione [ESPAÇO] ou [ENTER] para o próximo round";
                g2d.drawString(instrucao, 200, yTopo + 265);
            }
        }
    }

    public boolean isAberto() { return aberto; }
    public int getPontosP1() { return pontosP1; }
    public int getPontosP2() { return pontosP2; }
    public int getRoundAtual() { return roundAtual; }
}