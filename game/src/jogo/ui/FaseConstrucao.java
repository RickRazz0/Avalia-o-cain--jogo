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

    private final int TAM_GRADE = 40;
    private final int LARGURA_PERGAMINHO_MAX = 560;
    private final int ALTURA_PERGAMINHO = 340;

    private boolean ativa;
    private boolean menuPergaminhoAberto; // true = escolhendo no pergaminho 2x2 | false = posicionando no grid
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

    // Sorteia sempre 2 pisáveis e 2 letais e abre o pergaminho animado em 2x2
    public void iniciar() {
        this.ativa = true;
        this.menuPergaminhoAberto = true;
        this.larguraPergaminhoAtual = 20;
        this.opcoesSorteadas.clear();

        List<OpcaoPeca> pisaveis = new ArrayList<>();
        pisaveis.add(new OpcaoPeca("Tabua Madeira", false, 120, 20, "MADEIRA"));
        pisaveis.add(new OpcaoPeca("Degrau Curto", false, 80, 20, "DEGRAU"));
        Collections.shuffle(pisaveis);

        List<OpcaoPeca> letais = new ArrayList<>();
        letais.add(new OpcaoPeca("Espinhos", true, 80, 20, "ESPINHO"));
        letais.add(new OpcaoPeca("Flecha", true, 40, 20, "FLECHA_SOLTA"));
        Collections.shuffle(letais);

        opcoesSorteadas.add(pisaveis.get(0));
        opcoesSorteadas.add(pisaveis.get(1));
        opcoesSorteadas.add(letais.get(0));
        opcoesSorteadas.add(letais.get(1));
        Collections.shuffle(opcoesSorteadas);

        indiceP1 = 0;
        escolheuCartaP1 = false;
        cursorXP1 = 240;
        cursorYP1 = 280;
        confirmouPosicaoP1 = false;

        indiceP2 = 1;
        escolheuCartaP2 = false;
        cursorXP2 = 440;
        cursorYP2 = 280;
        confirmouPosicaoP2 = false;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void atualizarAnimacao() {
        if (!ativa) return;
        if (menuPergaminhoAberto && larguraPergaminhoAtual < LARGURA_PERGAMINHO_MAX) {
            larguraPergaminhoAtual += 32;
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
        if (x < 40 || y < 80 || x + opcao.largura > 760 || y + opcao.altura > 560) {
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

    // Controles Player 1:
    // - No pergaminho 2x2: A/D navega entre as 4 opções, N confirma a escolha da peça
    // - No quadriculado: W sobe, S desce, A esquerda, D direita, N confirma a posição
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
                cursorXP1 = ajustarNaGrade(cursorXP1, 40, 760 - op.largura);
                cursorYP1 = ajustarNaGrade(cursorYP1, 80, 560 - op.altura);
                verificarSaidaDoPergaminho();
            }
            return;
        }

        if (confirmouPosicaoP1) return;
        OpcaoPeca atual = opcoesSorteadas.get(indiceP1);

        switch (comando) {
            case "CIMA":
                cursorYP1 = ajustarNaGrade(cursorYP1 - TAM_GRADE, 80, 560 - atual.altura);
                break;
            case "BAIXO":
                cursorYP1 = ajustarNaGrade(cursorYP1 + TAM_GRADE, 80, 560 - atual.altura);
                break;
            case "ESQ":
                cursorXP1 = ajustarNaGrade(cursorXP1 - TAM_GRADE, 40, 760 - atual.largura);
                break;
            case "DIR":
                cursorXP1 = ajustarNaGrade(cursorXP1 + TAM_GRADE, 40, 760 - atual.largura);
                break;
            case "CONFIRMAR":
                if (posicaoValida(cursorXP1, cursorYP1, atual, pecasDoMapa)) {
                    confirmouPosicaoP1 = true;
                }
                break;
        }
    }

    // Controles Player 2:
    // - No pergaminho 2x2: Esquerda/Direita navega entre as 4 opções, M confirma a escolha da peça
    // - No quadriculado: Seta Cima sobe, Seta Baixo desce, Esquerda/Direita move, M confirma a posição
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
                cursorXP2 = ajustarNaGrade(cursorXP2, 40, 760 - op.largura);
                cursorYP2 = ajustarNaGrade(cursorYP2, 80, 560 - op.altura);
                verificarSaidaDoPergaminho();
            }
            return;
        }

        if (confirmouPosicaoP2) return;
        OpcaoPeca atual = opcoesSorteadas.get(indiceP2);

        switch (comando) {
            case "CIMA":
                cursorYP2 = ajustarNaGrade(cursorYP2 - TAM_GRADE, 80, 560 - atual.altura);
                break;
            case "BAIXO":
                cursorYP2 = ajustarNaGrade(cursorYP2 + TAM_GRADE, 80, 560 - atual.altura);
                break;
            case "ESQ":
                cursorXP2 = ajustarNaGrade(cursorXP2 - TAM_GRADE, 40, 760 - atual.largura);
                break;
            case "DIR":
                cursorXP2 = ajustarNaGrade(cursorXP2 + TAM_GRADE, 40, 760 - atual.largura);
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

        // 1. Fundo quadriculado na área livre
        g2d.setColor(new Color(70, 85, 105, 95));
        g2d.setStroke(new BasicStroke(1));
        for (int x = 40; x <= 760; x += TAM_GRADE) {
            g2d.drawLine(x, 80, x, 560);
        }
        for (int y = 80; y <= 560; y += TAM_GRADE) {
            g2d.drawLine(40, y, 760, y);
        }

        // 2. Previews das peças em posicionamento
        desenharPreview(g2d, cursorXP1, cursorYP1, opcoesSorteadas.get(indiceP1), Color.RED, confirmouPosicaoP1, pecasDoMapa, "P1 [N]");
        desenharPreview(g2d, cursorXP2, cursorYP2, opcoesSorteadas.get(indiceP2), Color.CYAN, confirmouPosicaoP2, pecasDoMapa, "P2 [M]");
    }

    private void desenharPergaminho2x2(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 165));
        g2d.fillRect(0, 0, 800, 600);

        int centroX = 400;
        int xEsquerda = centroX - (larguraPergaminhoAtual / 2);
        int yTopo = 125;

        // Corpo do pergaminho
        g2d.setColor(new Color(238, 214, 175));
        g2d.fillRoundRect(xEsquerda, yTopo, larguraPergaminhoAtual, ALTURA_PERGAMINHO, 15, 15);

        g2d.setColor(new Color(160, 110, 60));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(xEsquerda + 8, yTopo + 8, Math.max(1, larguraPergaminhoAtual - 16), ALTURA_PERGAMINHO - 16, 10, 10);

        // Rolos laterais de madeira
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
            g2d.setFont(new Font("SansSerif", Font.BOLD, 22));
            g2d.drawString("ESCOLHA SUA PECA (2x2)", 275, yTopo + 40);

            // Grid 2x2 de opções desenhadas
            int slotW = 210;
            int slotH = 92;
            int inicioX = 180;
            int inicioY = yTopo + 62;

            for (int i = 0; i < opcoesSorteadas.size(); i++) {
                int col = i % 2;
                int row = i / 2;
                int sx = inicioX + col * (slotW + 20);
                int sy = inicioY + row * (slotH + 14);

                OpcaoPeca op = opcoesSorteadas.get(i);

                g2d.setColor(new Color(218, 190, 148));
                g2d.fillRoundRect(sx, sy, slotW, slotH, 10, 10);

                g2d.setStroke(new BasicStroke(2));
                g2d.setColor(new Color(120, 80, 40));
                g2d.drawRoundRect(sx, sy, slotW, slotH, 10, 10);

                // Desenha a peça real centralizada dentro do quadrante 2x2
                ElementoJogo modeloDesenho = op.criarElemento(0, 0);
                int drawW = Math.min(modeloDesenho.getLargura(), 110);
                int drawH = Math.min(modeloDesenho.getAltura(), 24);
                int drawX = sx + (slotW - drawW) / 2;
                int drawY = sy + 28 + (28 - drawH) / 2;

                ElementoJogo visual = op.criarElemento(drawX, drawY);
                visual.desenhar(g2d);

                // Nome curto e indicador de seleção P1 / P2
                g2d.setColor(new Color(65, 35, 10));
                g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                g2d.drawString(op.nome, sx + 10, sy + 18);

                String sel = "";
                if (i == indiceP1) sel += escolheuCartaP1 ? "P1[OK] " : "P1 ";
                if (i == indiceP2) sel += escolheuCartaP2 ? "P2[OK]" : "P2";
                if (!sel.isEmpty()) {
                    g2d.setColor(op.letal ? new Color(175, 35, 35) : new Color(25, 110, 45));
                    g2d.drawString(sel.trim(), sx + 10, sy + slotH - 8);
                }
            }

            g2d.setColor(new Color(80, 45, 15));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2d.drawString("P1: A/D + [N] confirmar   |   P2: Esq/Dir + [M] confirmar", 205, yTopo + ALTURA_PERGAMINHO - 16);
        }
    }

    private void desenharPreview(Graphics2D g2d, int x, int y, OpcaoPeca op, Color corBorda,
                                 boolean confirmado, List<ElementoJogo> pecasDoMapa, String rotulo) {
        boolean valido = posicaoValida(x, y, op, pecasDoMapa);
        Color preenchimento = confirmado
            ? new Color(50, 200, 90, 130)
            : (valido ? new Color(255, 255, 255, 75) : new Color(220, 40, 40, 110));

        g2d.setColor(preenchimento);
        g2d.fillRect(x, y, op.largura, op.altura);

        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(valido ? corBorda : Color.RED);
        g2d.drawRect(x, y, op.largura, op.altura);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2d.drawString(rotulo + (confirmado ? " (OK)" : (valido ? "" : " [BLOQUEADO]")), x + 4, y - 4);
    }
}