package jogo.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jogo.base.ElementoJogo;
import jogo.obstaculos.Armadilha;
import jogo.obstaculos.Plataforma;

public class FaseConstrucao {

    public static class OpcaoPeca {
        public final String nome;
        public final boolean letal;
        public final int largura;
        public final int altura;
        public final String chaveTipo;

        public OpcaoPeca(String nome, boolean letal, int largura, int altura, String chaveTipo) {
            this.nome = nome;
            this.letal = letal;
            this.largura = largura;
            this.altura = altura;
            this.chaveTipo = chaveTipo;
        }

        public ElementoJogo criarElemento(int x, int y) {
            if (!letal) {
                if ("DEGRAU".equals(chaveTipo)) {
                    return new Plataforma(x, y, largura, altura, Color.GRAY, "DEGRAU");
                }
                return new Plataforma(x, y, largura, altura, new Color(139, 69, 19), "MADEIRA");
            } else {
                if ("FLECHA_SOLTA".equals(chaveTipo)) {
                    return new Armadilha(x, y, largura, altura, -4, "FLECHA");
                }
                return new Armadilha(x, y, largura, altura, 0, "ESPINHO");
            }
        }
    }

    private final int TAM_GRADE = 20;
    private final int LARGURA_PERGAMINHO_MAX = 640;
    private final int ALTURA_PERGAMINHO = 360;

    // Area livre do quadriculado no palco 1280x720 (multiplos de 20)
    private final int MIN_X = 40;
    private final int MAX_X = 1240;
    private final int MIN_Y = 80;
    private final int MAX_Y = 660;

    private boolean ativa;
    private boolean menuPergaminhoAberto;
    private int larguraPergaminhoAtual;

    private List<OpcaoPeca> opcoesSorteadas;

    // Player 1
    private int indiceP1;
    private boolean escolheuCartaP1;
    private int cursorXP1;
    private int cursorYP1;
    private boolean confirmouPosicaoP1;

    // Player 2
    private int indiceP2;
    private boolean escolheuCartaP2;
    private int cursorXP2;
    private int cursorYP2;
    private boolean confirmouPosicaoP2;

    public FaseConstrucao() {
        this.ativa = false;
        this.menuPergaminhoAberto = false;
        this.larguraPergaminhoAtual = 0;
        this.opcoesSorteadas = new ArrayList<>();
    }

    // Sorteia sempre 2 pisaveis e 2 letais (dimensoes multiplas exatas de 20px)
    public void iniciar() {
        this.ativa = true;
        this.menuPergaminhoAberto = true;
        this.larguraPergaminhoAtual = 20;
        this.opcoesSorteadas.clear();

        List<OpcaoPeca> pisaveis = new ArrayList<>();
        pisaveis.add(new OpcaoPeca("Tabua Madeira", false, 120, 20, "MADEIRA")); // 6x1 quadradinhos
        pisaveis.add(new OpcaoPeca("Degrau Curto", false, 80, 20, "DEGRAU"));    // 4x1 quadradinhos
        Collections.shuffle(pisaveis);

        List<OpcaoPeca> letais = new ArrayList<>();
        letais.add(new OpcaoPeca("Espinhos", true, 80, 20, "ESPINHO"));         // 4x1 quadradinhos
        letais.add(new OpcaoPeca("Flecha", true, 40, 20, "FLECHA_SOLTA"));     // 2x1 quadradinhos
        Collections.shuffle(letais);

        opcoesSorteadas.add(pisaveis.get(0));
        opcoesSorteadas.add(pisaveis.get(1));
        opcoesSorteadas.add(letais.get(0));
        opcoesSorteadas.add(letais.get(1));
        Collections.shuffle(opcoesSorteadas);

        indiceP1 = 0;
        escolheuCartaP1 = false;
        cursorXP1 = 440;
        cursorYP1 = 360;
        confirmouPosicaoP1 = false;

        indiceP2 = 1;
        escolheuCartaP2 = false;
        cursorXP2 = 720;
        cursorYP2 = 360;
        confirmouPosicaoP2 = false;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void atualizarAnimacao() {
        if (!ativa) return;
        if (menuPergaminhoAberto && larguraPergaminhoAtual < LARGURA_PERGAMINHO_MAX) {
            larguraPergaminhoAtual += 36;
            if (larguraPergaminhoAtual > LARGURA_PERGAMINHO_MAX) {
                larguraPergaminhoAtual = LARGURA_PERGAMINHO_MAX;
            }
        }
    }

    public boolean ambosConfirmaram() {
        return confirmouPosicaoP1 && confirmouPosicaoP2;
    }

    public void encerrarSePronto(List<ElementoJogo> pecasDoMapa) {
        if (ambosConfirmaram()) {
            ElementoJogo p1 = opcoesSorteadas.get(indiceP1).criarElemento(cursorXP1, cursorYP1);
            ElementoJogo p2 = opcoesSorteadas.get(indiceP2).criarElemento(cursorXP2, cursorYP2);
            pecasDoMapa.add(p1);
            pecasDoMapa.add(p2);
            ativa = false;
        }
    }

    private int ajustarNaGrade(int valor, int min, int max) {
        int alinhado = (valor / TAM_GRADE) * TAM_GRADE;
        if (alinhado < min) alinhado = min;
        if (alinhado > max) alinhado = max;
        return alinhado;
    }

    public boolean posicaoValida(int x, int y, OpcaoPeca opcao, List<ElementoJogo> pecasDoMapa) {
        if (x < MIN_X || y < MIN_Y || x + opcao.largura > MAX_X || y + opcao.altura > MAX_Y) {
            return false;
        }
        Rectangle candidato = new Rectangle(x, y, opcao.largura, opcao.altura);
        for (ElementoJogo peca : pecasDoMapa) {
            if (candidato.intersects(peca.getLimites())) {
                return false;
            }
        }
        return true;
    }

    private void verificarSaidaDoPergaminho() {
        if (menuPergaminhoAberto && escolheuCartaP1 && escolheuCartaP2) {
            menuPergaminhoAberto = false;
        }
    }

    public void acaoP1(String comando, List<ElementoJogo> pecasDoMapa) {
        if (!ativa) return;

        if (menuPergaminhoAberto) {
            if (escolheuCartaP1) return;
            if ("ESQ".equals(comando)) {
                indiceP1 = (indiceP1 - 1 + opcoesSorteadas.size()) % opcoesSorteadas.size();
            } else if ("DIR".equals(comando)) {
                indiceP1 = (indiceP1 + 1) % opcoesSorteadas.size();
            } else if ("CONFIRMAR".equals(comando)) {
                escolheuCartaP1 = true;
                OpcaoPeca op = opcoesSorteadas.get(indiceP1);
                cursorXP1 = ajustarNaGrade(cursorXP1, MIN_X, MAX_X - op.largura);
                cursorYP1 = ajustarNaGrade(cursorYP1, MIN_Y, MAX_Y - op.altura);
                verificarSaidaDoPergaminho();
            }
            return;
        }

        if (confirmouPosicaoP1) return;
        OpcaoPeca atual = opcoesSorteadas.get(indiceP1);

        switch (comando) {
            case "CIMA":
                cursorYP1 = ajustarNaGrade(cursorYP1 - TAM_GRADE, MIN_Y, MAX_Y - atual.altura);
                break;
            case "BAIXO":
                cursorYP1 = ajustarNaGrade(cursorYP1 + TAM_GRADE, MIN_Y, MAX_Y - atual.altura);
                break;
            case "ESQ":
                cursorXP1 = ajustarNaGrade(cursorXP1 - TAM_GRADE, MIN_X, MAX_X - atual.largura);
                break;
            case "DIR":
                cursorXP1 = ajustarNaGrade(cursorXP1 + TAM_GRADE, MIN_X, MAX_X - atual.largura);
                break;
            case "CONFIRMAR":
                if (posicaoValida(cursorXP1, cursorYP1, atual, pecasDoMapa)) {
                    confirmouPosicaoP1 = true;
                }
                break;
        }
    }

    public void acaoP2(String comando, List<ElementoJogo> pecasDoMapa) {
        if (!ativa) return;

        if (menuPergaminhoAberto) {
            if (escolheuCartaP2) return;
            if ("ESQ".equals(comando)) {
                indiceP2 = (indiceP2 - 1 + opcoesSorteadas.size()) % opcoesSorteadas.size();
            } else if ("DIR".equals(comando)) {
                indiceP2 = (indiceP2 + 1) % opcoesSorteadas.size();
            } else if ("CONFIRMAR".equals(comando)) {
                escolheuCartaP2 = true;
                OpcaoPeca op = opcoesSorteadas.get(indiceP2);
                cursorXP2 = ajustarNaGrade(cursorXP2, MIN_X, MAX_X - op.largura);
                cursorYP2 = ajustarNaGrade(cursorYP2, MIN_Y, MAX_Y - op.altura);
                verificarSaidaDoPergaminho();
            }
            return;
        }

        if (confirmouPosicaoP2) return;
        OpcaoPeca atual = opcoesSorteadas.get(indiceP2);

        switch (comando) {
            case "CIMA":
                cursorYP2 = ajustarNaGrade(cursorYP2 - TAM_GRADE, MIN_Y, MAX_Y - atual.altura);
                break;
            case "BAIXO":
                cursorYP2 = ajustarNaGrade(cursorYP2 + TAM_GRADE, MIN_Y, MAX_Y - atual.altura);
                break;
            case "ESQ":
                cursorXP2 = ajustarNaGrade(cursorXP2 - TAM_GRADE, MIN_X, MAX_X - atual.largura);
                break;
            case "DIR":
                cursorXP2 = ajustarNaGrade(cursorXP2 + TAM_GRADE, MIN_X, MAX_X - atual.largura);
                break;
            case "CONFIRMAR":
                if (posicaoValida(cursorXP2, cursorYP2, atual, pecasDoMapa)) {
                    confirmouPosicaoP2 = true;
                }
                break;
        }
    }

    public void desenhar(Graphics2D g2d, List<ElementoJogo> pecasDoMapa) {
        if (!ativa) return;

        if (menuPergaminhoAberto) {
            desenharPergaminho2x2(g2d);
            return;
        }

        // 1. Quadriculado pequeno (20x20) preenchendo a area de construcao
        g2d.setColor(new Color(85, 105, 130, 85));
        g2d.setStroke(new BasicStroke(1));
        for (int x = MIN_X; x <= MAX_X; x += TAM_GRADE) {
            g2d.drawLine(x, MIN_Y, x, MAX_Y);
        }
        for (int y = MIN_Y; y <= MAX_Y; y += TAM_GRADE) {
            g2d.drawLine(MIN_X, y, MAX_X, y);
        }

        // 2. Previews cobrindo os quadradinhos exatos
        desenharPreview(g2d, cursorXP1, cursorYP1, opcoesSorteadas.get(indiceP1), Color.RED, confirmouPosicaoP1, pecasDoMapa, "P1 [N]");
        desenharPreview(g2d, cursorXP2, cursorYP2, opcoesSorteadas.get(indiceP2), Color.CYAN, confirmouPosicaoP2, pecasDoMapa, "P2 [M]");
    }

    private void desenharPergaminho2x2(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 165));
        g2d.fillRect(0, 0, 1280, 720);

        int centroX = 640;
        int xEsquerda = centroX - (larguraPergaminhoAtual / 2);
        int yTopo = 160;

        g2d.setColor(new Color(238, 214, 175));
        g2d.fillRoundRect(xEsquerda, yTopo, larguraPergaminhoAtual, ALTURA_PERGAMINHO, 15, 15);

        g2d.setColor(new Color(160, 110, 60));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(xEsquerda + 8, yTopo + 8, Math.max(1, larguraPergaminhoAtual - 16), ALTURA_PERGAMINHO - 16, 10, 10);

        g2d.setColor(new Color(101, 55, 24));
        g2d.fillRoundRect(xEsquerda - 12, yTopo - 15, 22, ALTURA_PERGAMINHO + 30, 10, 10);
        g2d.fillRoundRect(xEsquerda + larguraPergaminhoAtual - 10, yTopo - 15, 22, ALTURA_PERGAMINHO + 30, 10, 10);

        g2d.setColor(new Color(218, 165, 32));
        g2d.fillRect(xEsquerda - 12, yTopo - 15, 22, 8);
        g2d.fillRect(xEsquerda - 12, yTopo + ALTURA_PERGAMINHO + 7, 22, 8);
        g2d.fillRect(xEsquerda + larguraPergaminhoAtual - 10, yTopo - 15, 22, 8);
        g2d.fillRect(xEsquerda + larguraPergaminhoAtual - 10, yTopo + ALTURA_PERGAMINHO + 7, 22, 8);

        if (larguraPergaminhoAtual >= LARGURA_PERGAMINHO_MAX - 50) {
            g2d.setColor(new Color(70, 40, 15));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
            g2d.drawString("ESCOLHA SUA PECA (2x2)", 490, yTopo + 42);

            int slotW = 240;
            int slotH = 98;
            int inicioX = 390;
            int inicioY = yTopo + 66;

            for (int i = 0; i < opcoesSorteadas.size(); i++) {
                int col = i % 2;
                int row = i / 2;
                int sx = inicioX + col * (slotW + 20);
                int sy = inicioY + row * (slotH + 16);

                OpcaoPeca op = opcoesSorteadas.get(i);

                g2d.setColor(new Color(218, 190, 148));
                g2d.fillRoundRect(sx, sy, slotW, slotH, 10, 10);

                g2d.setStroke(new BasicStroke(2));
                g2d.setColor(new Color(120, 80, 40));
                g2d.drawRoundRect(sx, sy, slotW, slotH, 10, 10);

                int drawW = Math.min(op.largura, 120);
                int drawH = Math.min(op.altura, 20);
                int drawX = sx + (slotW - drawW) / 2;
                int drawY = sy + 42;

                ElementoJogo visual = op.criarElemento(drawX, drawY);
                visual.desenhar(g2d);

                g2d.setColor(new Color(65, 35, 10));
                g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                g2d.drawString(op.nome, sx + 10, sy + 20);

                String sel = "";
                if (i == indiceP1) sel += escolheuCartaP1 ? "P1[OK] " : "P1 ";
                if (i == indiceP2) sel += escolheuCartaP2 ? "P2[OK]" : "P2";
                if (!sel.isEmpty()) {
                    g2d.setColor(op.letal ? new Color(175, 35, 35) : new Color(25, 110, 45));
                    g2d.drawString(sel.trim(), sx + 10, sy + slotH - 8);
                }
            }

            g2d.setColor(new Color(80, 45, 15));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2d.drawString("P1: A/D + [N] confirmar   |   P2: Esq/Dir + [M] confirmar", 425, yTopo + ALTURA_PERGAMINHO - 16);
        }
    }

    private void desenharPreview(Graphics2D g2d, int x, int y, OpcaoPeca op, Color corBorda,
                                 boolean confirmado, List<ElementoJogo> pecasDoMapa, String rotulo) {
        boolean valido = posicaoValida(x, y, op, pecasDoMapa);
        Color preenchimento = confirmado
            ? new Color(50, 200, 90, 135)
            : (valido ? new Color(255, 255, 255, 85) : new Color(220, 40, 40, 120));

        g2d.setColor(preenchimento);
        g2d.fillRect(x, y, op.largura, op.altura);

        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(valido ? corBorda : Color.RED);
        g2d.drawRect(x, y, op.largura, op.altura);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2d.drawString(rotulo + (confirmado ? " (OK)" : (valido ? "" : " [BLOQUEADO]")), x + 2, y - 4);
    }
}