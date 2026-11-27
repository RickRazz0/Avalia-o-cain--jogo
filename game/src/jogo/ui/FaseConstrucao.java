package jogo.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import jogo.base.ElementoJogo;
import jogo.obstaculos.Armadilha;
import jogo.obstaculos.Plataforma;

// [COLLECTIONS]: coordena a lista de peças já colocadas e as opções sorteadas.
public class FaseConstrucao {

    // [ABSTRAÇÃO]: descreve uma carta do catálogo sem depender da classe concreta criada depois.
    public static class OpcaoPeca {
        public final String nome;
        public final boolean letal;
        public final boolean bomba;
        public final int largura;
        public final int altura;
        public final String chaveTipo;

        public OpcaoPeca(String nome, boolean letal, boolean bomba, int largura, int altura, String chaveTipo) {
            this.nome = nome;
            this.letal = letal;
            this.bomba = bomba;
            this.largura = largura;
            this.altura = altura;
            this.chaveTipo = chaveTipo;
        }

        public ElementoJogo criarElemento(int x, int y) {
            return switch (chaveTipo) {
                case "MADEIRINHA" -> new Plataforma(x, y, largura, altura, new Color(165, 92, 35), "MADEIRINHA");
                case "ESCADARIA" -> new Plataforma(x, y, largura, altura, Color.GRAY, "ESCADARIA");
                case "DEGRAU" -> new Plataforma(x, y, largura, altura, Color.GRAY, "DEGRAU");
                case "MOLA" -> new Plataforma(x, y, largura, altura, new Color(225, 65, 45), "MOLA");
                case "QUEBRADICA" -> new Plataforma(x, y, largura, altura, new Color(120, 126, 138), "QUEBRADICA");
                case "SERRA_TRILHO" -> new Armadilha(x, y, largura, altura, 0, "SERRA_TRILHO");
                case "SERRA_FLUTUANTE" -> new Armadilha(x, y, largura, altura, 0, "SERRA_FLUTUANTE");
                case "ESPINHO" -> new Armadilha(x, y, largura, altura, 0, "ESPINHO");
                case "LANCA_CHAMAS" -> new Armadilha(x, y, largura, altura, 0, "LANCA_CHAMAS");
                case "BESTA" -> new Armadilha(x, y, largura, altura, 0, "BESTA");
                default -> new Plataforma(x, y, largura, altura, new Color(139, 69, 19), "MADEIRA");
            };
        }

    }

    private static class ExplosaoAnimacao {
        final int centroX;
        final int centroY;
        final Rectangle area3x3;
        final int duracaoTotal = 28;
        int framesRestantes = 28;

        ExplosaoAnimacao(Rectangle area3x3) {
            this.area3x3 = area3x3;
            this.centroX = area3x3.x + area3x3.width / 2;
            this.centroY = area3x3.y + area3x3.height / 2;
        }

        void atualizar() {
            if (framesRestantes > 0) {
                framesRestantes--;
            }
        }

        boolean terminou() {
            return framesRestantes <= 0;
        }

        void desenhar(Graphics2D g2d) {
            if (framesRestantes <= 0) return;

            float progresso = 1.0f - ((float) framesRestantes / duracaoTotal);
            int alphaBase = Math.max(0, Math.min(220, (int) ((1.0f - progresso) * 220)));

            g2d.setColor(new Color(255, 110, 20, alphaBase / 3));
            g2d.fillRect(area3x3.x, area3x3.y, area3x3.width, area3x3.height);

            int raioExterno = 16 + (int) (progresso * 44);
            int raioMedio = 10 + (int) (progresso * 30);
            int raioInterno = Math.max(4, (int) ((1.0f - progresso * 0.7f) * 18));

            g2d.setColor(new Color(255, 85, 15, alphaBase));
            g2d.fillOval(centroX - raioExterno, centroY - raioExterno, raioExterno * 2, raioExterno * 2);

            g2d.setColor(new Color(255, 185, 30, alphaBase));
            g2d.fillOval(centroX - raioMedio, centroY - raioMedio, raioMedio * 2, raioMedio * 2);

            g2d.setColor(new Color(255, 245, 160, Math.min(255, alphaBase + 35)));
            g2d.fillOval(centroX - raioInterno, centroY - raioInterno, raioInterno * 2, raioInterno * 2);

            g2d.setColor(new Color(255, 210, 60, alphaBase));
            int distFagulha = 12 + (int) (progresso * 34);
            for (int i = 0; i < 8; i++) {
                double ang = i * (Math.PI / 4.0) + (progresso * 0.6);
                int fx = centroX + (int) (Math.cos(ang) * distFagulha);
                int fy = centroY + (int) (Math.sin(ang) * distFagulha);
                g2d.fillOval(fx - 4, fy - 4, 8, 8);
            }
        }
    }

    private final int TAM_GRADE = 20;
    private final int LARGURA_PERGAMINHO_MAX = 640;
    private final int ALTURA_PERGAMINHO = 360;

    private final int MIN_X = 40;
    private final int MAX_X = 1240;
    private final int MIN_Y = 80;
    private final int MAX_Y = 660;

    // Áreas protegidas: nascimento (esquerda) e chegada (direita).
    // Qualquer sobreposição, mesmo parcial, bloqueia a colocação.
    private final Rectangle areaNascimento = new Rectangle(40, 440, 160, 80);
    private final Rectangle areaChegada = new Rectangle(1080, 440, 160, 80);

    private boolean ativa;
    private boolean menuPergaminhoAberto;
    private int larguraPergaminhoAtual;

    // [COLLECTIONS]: listas dinâmicas guardam cartas disponíveis e explosões em andamento.
    private final List<OpcaoPeca> opcoesSorteadas;
    private final List<ExplosaoAnimacao> explosoesAtivas;

    private int indiceP1;
    private boolean escolheuCartaP1;
    private int cursorXP1;
    private int cursorYP1;
    private boolean confirmouPosicaoP1;
    private int rotacaoP1;

    private int indiceP2;
    private boolean escolheuCartaP2;
    private int cursorXP2;
    private int cursorYP2;
    private boolean confirmouPosicaoP2;
    private int rotacaoP2;

    public FaseConstrucao() {
        this.ativa = false;
        this.menuPergaminhoAberto = false;
        this.larguraPergaminhoAtual = 0;
        this.opcoesSorteadas = new ArrayList<>();
        this.explosoesAtivas = new ArrayList<>();
    }

    // [COLLECTIONS]: monta, embaralha e seleciona quatro opções do catálogo.
    public void iniciar() {
        this.ativa = true;
        this.menuPergaminhoAberto = true;
        this.larguraPergaminhoAtual = 20;
        this.opcoesSorteadas.clear();

        List<OpcaoPeca> catalogo = new ArrayList<>();
        catalogo.add(new OpcaoPeca("Tabua Madeira", false, false, 120, 20, "MADEIRA"));
        catalogo.add(new OpcaoPeca("Madeirinha", false, false, 60, 20, "MADEIRINHA"));
        catalogo.add(new OpcaoPeca("Escadaria", false, false, 60, 60, "ESCADARIA"));
        catalogo.add(new OpcaoPeca("Degrau", false, false, 20, 20, "DEGRAU"));
        catalogo.add(new OpcaoPeca("Plataforma Mola", false, false, 40, 20, "MOLA"));
        catalogo.add(new OpcaoPeca("Plataforma Quebradica", false, false, 100, 20, "QUEBRADICA"));
        catalogo.add(new OpcaoPeca("Plataforma Serra", true, false, 120, 20, "SERRA_TRILHO"));
        catalogo.add(new OpcaoPeca("Serrinha", true, false, 40, 40, "SERRA_FLUTUANTE"));
        catalogo.add(new OpcaoPeca("Espinhos", true, false, 80, 20, "ESPINHO"));
        catalogo.add(new OpcaoPeca("Lanca-chamas", true, false, 40, 40, "LANCA_CHAMAS"));
        catalogo.add(new OpcaoPeca("Besta", true, false, 40, 40, "BESTA"));
        catalogo.add(new OpcaoPeca("Bomba 3x3", false, true, 20, 20, "BOMBA"));

        Collections.shuffle(catalogo);
        for (int i = 0; i < 4; i++) {
            opcoesSorteadas.add(catalogo.get(i));
        }

        indiceP1 = 0;
        escolheuCartaP1 = false;
        cursorXP1 = 440;
        cursorYP1 = 360;
        confirmouPosicaoP1 = false;
        rotacaoP1 = 0;

        indiceP2 = 1;
        escolheuCartaP2 = false;
        cursorXP2 = 720;
        cursorYP2 = 360;
        confirmouPosicaoP2 = false;
        rotacaoP2 = 0;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public boolean temExplosaoAtiva() {
        return !explosoesAtivas.isEmpty();
    }

    public void atualizarExplosoes() {
        Iterator<ExplosaoAnimacao> it = explosoesAtivas.iterator();
        while (it.hasNext()) {
            ExplosaoAnimacao exp = it.next();
            exp.atualizar();
            if (exp.terminou()) {
                it.remove();
            }
        }
    }

    public void desenharExplosoes(Graphics2D g2d) {
        for (ExplosaoAnimacao exp : explosoesAtivas) {
            exp.desenhar(g2d);
        }
    }

    public void atualizarAnimacao() {
        if (ativa && menuPergaminhoAberto && larguraPergaminhoAtual < LARGURA_PERGAMINHO_MAX) {
            larguraPergaminhoAtual += 36;
            if (larguraPergaminhoAtual > LARGURA_PERGAMINHO_MAX) {
                larguraPergaminhoAtual = LARGURA_PERGAMINHO_MAX;
            }
        }
        atualizarExplosoes();
    }

    public boolean ambosConfirmaram() {
        return confirmouPosicaoP1 && confirmouPosicaoP2;
    }

    private Rectangle getAreaExplosao3x3(int x, int y, int largura, int altura) {
        int raio = TAM_GRADE;
        return new Rectangle(x - raio, y - raio, largura + raio * 2, altura + raio * 2);
    }

    private void aplicarEfeitoPeca(OpcaoPeca op, int x, int y, int rotacao, List<ElementoJogo> pecasDoMapa) {
        if (op.bomba) {
            Rectangle areaExplosao = getAreaExplosao3x3(x, y, op.largura, op.altura);
            pecasDoMapa.removeIf(peca -> podeSerDestruidaPelaBomba(peca)
                    && areaExplosao.intersects(peca.getLimites()));
            explosoesAtivas.add(new ExplosaoAnimacao(areaExplosao));
        } else {
            ElementoJogo elemento = op.criarElemento(x, y);
            for (int i = 0; i < rotacao; i++) elemento.girarHorario();
            pecasDoMapa.add(elemento);
        }
    }

    public void encerrarSePronto(List<ElementoJogo> pecasDoMapa) {
        if (ambosConfirmaram()) {
            OpcaoPeca p1 = opcoesSorteadas.get(indiceP1);
            OpcaoPeca p2 = opcoesSorteadas.get(indiceP2);

            // Primeiro entram todas as peças; depois as bombas explodem. Assim,
            // uma bomba também afeta uma peça confirmada no mesmo turno.
            if (!p1.bomba) aplicarEfeitoPeca(p1, cursorXP1, cursorYP1, rotacaoP1, pecasDoMapa);
            if (!p2.bomba) aplicarEfeitoPeca(p2, cursorXP2, cursorYP2, rotacaoP2, pecasDoMapa);
            if (p1.bomba) aplicarEfeitoPeca(p1, cursorXP1, cursorYP1, rotacaoP1, pecasDoMapa);
            if (p2.bomba) aplicarEfeitoPeca(p2, cursorXP2, cursorYP2, rotacaoP2, pecasDoMapa);
            ativa = false;
        }
    }

    private boolean podeSerDestruidaPelaBomba(ElementoJogo peca) {
        if (peca instanceof Armadilha) return true;
        return peca instanceof Plataforma plataforma && !"CHAO".equals(plataforma.getTipo());
    }

    private int ajustarNaGrade(int valor, int min, int max) {
        int alinhado = (valor / TAM_GRADE) * TAM_GRADE;
        if (alinhado < min) alinhado = min;
        if (alinhado > max) alinhado = max;
        return alinhado;
    }

    private int larguraRotacionada(OpcaoPeca opcao, int rotacao) {
        return rotacao % 2 == 0 ? opcao.largura : opcao.altura;
    }

    private int alturaRotacionada(OpcaoPeca opcao, int rotacao) {
        return rotacao % 2 == 0 ? opcao.altura : opcao.largura;
    }

    // [ENCAPSULAMENTO]: centraliza as regras de limites, áreas protegidas e sobreposição.
    public boolean posicaoValida(int x, int y, OpcaoPeca opcao, int rotacao, List<ElementoJogo> pecasDoMapa) {
        int largura = larguraRotacionada(opcao, rotacao);
        int altura = alturaRotacionada(opcao, rotacao);
        if (x < MIN_X || y < MIN_Y || x + largura > MAX_X || y + altura > MAX_Y) {
            return false;
        }
        Rectangle candidato = new Rectangle(x, y, largura, altura);
        if (candidato.intersects(areaNascimento) || candidato.intersects(areaChegada)) {
            return false;
        }
        if (opcao.bomba) {
            return true;
        }
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
            case "CIMA" -> cursorYP1 = ajustarNaGrade(cursorYP1 - TAM_GRADE, MIN_Y, MAX_Y - alturaRotacionada(atual, rotacaoP1));
            case "BAIXO" -> cursorYP1 = ajustarNaGrade(cursorYP1 + TAM_GRADE, MIN_Y, MAX_Y - alturaRotacionada(atual, rotacaoP1));
            case "ESQ" -> cursorXP1 = ajustarNaGrade(cursorXP1 - TAM_GRADE, MIN_X, MAX_X - larguraRotacionada(atual, rotacaoP1));
            case "DIR" -> cursorXP1 = ajustarNaGrade(cursorXP1 + TAM_GRADE, MIN_X, MAX_X - larguraRotacionada(atual, rotacaoP1));
            case "GIRAR" -> rotacaoP1 = (rotacaoP1 + 1) % 4;
            case "CONFIRMAR" -> {
                if (posicaoValida(cursorXP1, cursorYP1, atual, rotacaoP1, pecasDoMapa)) {
                    confirmouPosicaoP1 = true;
                }
            }
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
            case "CIMA" -> cursorYP2 = ajustarNaGrade(cursorYP2 - TAM_GRADE, MIN_Y, MAX_Y - alturaRotacionada(atual, rotacaoP2));
            case "BAIXO" -> cursorYP2 = ajustarNaGrade(cursorYP2 + TAM_GRADE, MIN_Y, MAX_Y - alturaRotacionada(atual, rotacaoP2));
            case "ESQ" -> cursorXP2 = ajustarNaGrade(cursorXP2 - TAM_GRADE, MIN_X, MAX_X - larguraRotacionada(atual, rotacaoP2));
            case "DIR" -> cursorXP2 = ajustarNaGrade(cursorXP2 + TAM_GRADE, MIN_X, MAX_X - larguraRotacionada(atual, rotacaoP2));
            case "GIRAR" -> rotacaoP2 = (rotacaoP2 + 1) % 4;
            case "CONFIRMAR" -> {
                if (posicaoValida(cursorXP2, cursorYP2, atual, rotacaoP2, pecasDoMapa)) {
                    confirmouPosicaoP2 = true;
                }
            }
        }
    }

    // [POLIMORFISMO]: desenha previews sem conhecer a subclasse final de cada peça.
    public void desenhar(Graphics2D g2d, List<ElementoJogo> pecasDoMapa) {
        if (!ativa) return;

        if (menuPergaminhoAberto) {
            desenharPergaminho2x2(g2d);
            return;
        }

        g2d.setColor(new Color(85, 105, 130, 85));
        g2d.setStroke(new BasicStroke(1));
        for (int x = MIN_X; x <= MAX_X; x += TAM_GRADE) {
            g2d.drawLine(x, MIN_Y, x, MAX_Y);
        }
        for (int y = MIN_Y; y <= MAX_Y; y += TAM_GRADE) {
            g2d.drawLine(MIN_X, y, MAX_X, y);
        }

        desenharAreaProtegida(g2d, areaNascimento);
        desenharAreaProtegida(g2d, areaChegada);

        desenharPreview(g2d, cursorXP1, cursorYP1, opcoesSorteadas.get(indiceP1), rotacaoP1, Color.RED, confirmouPosicaoP1, pecasDoMapa, "P1 [N]");
        desenharPreview(g2d, cursorXP2, cursorYP2, opcoesSorteadas.get(indiceP2), rotacaoP2, Color.CYAN, confirmouPosicaoP2, pecasDoMapa, "P2 [M]");
    }

    private void desenharBombaVisual(Graphics2D g2d, int x, int y, int w, int h) {
        int tam = Math.min(w, h);
        int bx = x + (w - tam) / 2;
        int by = y + (h - tam) / 2;
        g2d.setColor(new Color(45, 45, 55));
        g2d.fillOval(bx, by, tam, tam);
        g2d.setColor(Color.RED);
        g2d.fillOval(bx + tam / 2 - 2, by - 3, 5, 5);
    }

    private void desenharAreaProtegida(Graphics2D g2d, Rectangle area) {
        g2d.setColor(new Color(255, 35, 35, 38));
        g2d.fillRect(area.x, area.y, area.width, area.height);
        g2d.setColor(new Color(255, 45, 45));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(area.x, area.y, area.width, area.height);
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

                int drawW = Math.min(op.largura, 110);
                int drawH = Math.min(op.altura, 34);
                int drawX = sx + (slotW - drawW) / 2;
                int drawY = sy + 28 + (36 - drawH) / 2;

                if (op.bomba) {
                    desenharBombaVisual(g2d, drawX, drawY, drawW, drawH);
                } else {
                    ElementoJogo visual = op.criarElemento(drawX, drawY);
                    visual.desenhar(g2d);
                }

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

    private void desenharPreview(Graphics2D g2d, int x, int y, OpcaoPeca op, int rotacao, Color corBorda,
                                 boolean confirmado, List<ElementoJogo> pecasDoMapa, String rotulo) {
        int largura = larguraRotacionada(op, rotacao);
        int altura = alturaRotacionada(op, rotacao);
        boolean valido = posicaoValida(x, y, op, rotacao, pecasDoMapa);
        Color preenchimento = confirmado
            ? new Color(50, 200, 90, 135)
            : (valido ? new Color(255, 255, 255, 85) : new Color(220, 40, 40, 120));

        g2d.setColor(preenchimento);
        g2d.fillRect(x, y, largura, altura);

        if (op.bomba) {
            Rectangle area3x3 = getAreaExplosao3x3(x, y, largura, altura);
            g2d.setColor(new Color(255, 90, 40, 65));
            g2d.fillRect(area3x3.x, area3x3.y, area3x3.width, area3x3.height);
            g2d.setColor(Color.ORANGE);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawRect(area3x3.x, area3x3.y, area3x3.width, area3x3.height);
        }

        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(valido ? corBorda : Color.RED);
        g2d.drawRect(x, y, largura, altura);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2d.drawString(rotulo + " " + (rotacao * 90) + "°" + (confirmado ? " (OK)" : (valido ? "" : " [BLOQUEADO]")), x + 2, y - 4);
    }
}
