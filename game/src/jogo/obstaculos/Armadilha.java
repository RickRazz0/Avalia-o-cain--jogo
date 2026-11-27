package jogo.obstaculos;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;
import jogo.base.ElementoJogo;
import jogo.jogadores.Jogador;

public class Armadilha extends ElementoJogo {

    private final int velocidadeX;
    private final int velocidadeY;
    private final int xInicial;
    private final String tipo; // "ESPINHO", "FLECHA", "SERRA_TRILHO", "SERRA_FLUTUANTE", "LANCA_CHAMAS"

    private int serraOffsetX = 0;
    private int direcaoSerra = 1;

    // Controle do ciclo e alcance horizontal do Lança-chamas
    private int cicloFogoFrames = 0;
    private int direcaoFogo = 1; // 1 = direita, -1 = esquerda
    private int comprimentoJatoAtual = 0;
    private int rotacao = 0;
    private int contadorDisparoBesta = 0;
    private static final int INTERVALO_DISPARO_BESTA = 75;

    public Armadilha(int x, int y, int largura, int altura, int velocidadeX) {
        this(x, y, largura, altura, velocidadeX, velocidadeX != 0 ? "FLECHA" : "ESPINHO");
    }

    public Armadilha(int x, int y, int largura, int altura, int velocidadeX, String tipo) {
        this(x, y, largura, altura, velocidadeX, 0, tipo);
    }

    public Armadilha(int x, int y, int largura, int altura, int velocidadeX, int velocidadeY) {
        this(x, y, largura, altura, velocidadeX, velocidadeY, "FLECHA");
    }

    private Armadilha(int x, int y, int largura, int altura, int velocidadeX, int velocidadeY, String tipo) {
        super(x, y, largura, altura);
        this.velocidadeX = velocidadeX;
        this.velocidadeY = velocidadeY;
        this.xInicial = x;
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }

    public void girarHorario() {
        super.girarHorario();
        rotacao = (rotacao + 1) % 4;
    }

    private int direcaoX() {
        return switch (rotacao) { case 0 -> 1; case 2 -> -1; default -> 0; };
    }

    private int direcaoY() {
        return switch (rotacao) { case 1 -> 1; case 3 -> -1; default -> 0; };
    }

    private Rectangle getHitboxSerraTrilho() {
        int raio = Math.min(getAltura(), 28);
        int sx = getX() + serraOffsetX;
        int sy = getY() + (getAltura() - raio) / 2;
        return new Rectangle(sx, sy, raio, raio);
    }

    private boolean jatoFogoAtivo() {
        // Ciclo de 150 frames (~2.5s): 0..59 carregando/pausa, 60..149 disparando fogo
        return (cicloFogoFrames % 150) >= 60;
    }

    private Rectangle getHitboxJatoFogo() {
        if (!"LANCA_CHAMAS".equals(tipo) || !jatoFogoAtivo() || comprimentoJatoAtual <= 0) {
            return new Rectangle(0, 0, 0, 0);
        }
        int jatoAltura = 18;
        int jatoY = getY() + (getAltura() - jatoAltura) / 2;
        if (direcaoFogo >= 0) {
            int jatoX = getX() + getLargura();
            return new Rectangle(jatoX, jatoY, comprimentoJatoAtual, jatoAltura);
        } else {
            int jatoX = getX() - comprimentoJatoAtual;
            return new Rectangle(jatoX, jatoY, comprimentoJatoAtual, jatoAltura);
        }
    }

    /**
     * Verifica o fogo separadamente do bloco do lança-chamas. O jato pode
     * alcançar o jogador sem que ele encoste no corpo sólido da armadilha.
     */
    public boolean causarDanoSeAtingirPeloJato(Jogador jogador) {
        if (!"LANCA_CHAMAS".equals(tipo)) return false;

        Rectangle jato = getHitboxJatoFogo();
        if (!jato.isEmpty() && jogador.getLimites().intersects(jato)) {
            jogador.morrer();
            return true;
        }
        return false;
    }

    public void atualizar(List<ElementoJogo> pecasDoMapa) {
        if ("BESTA".equals(tipo)) {
            contadorDisparoBesta++;
            if (contadorDisparoBesta >= INTERVALO_DISPARO_BESTA) {
                contadorDisparoBesta = 0;
                int dx = direcaoX();
                int dy = direcaoY();
                int flechaW = dx == 0 ? 8 : 20;
                int flechaH = dy == 0 ? 8 : 20;
                int flechaX = getX() + getLargura() / 2 - flechaW / 2;
                int flechaY = getY() + getAltura() / 2 - flechaH / 2;
                if (dx > 0) flechaX = getX() + getLargura();
                if (dx < 0) flechaX = getX() - flechaW;
                if (dy > 0) flechaY = getY() + getAltura();
                if (dy < 0) flechaY = getY() - flechaH;
                pecasDoMapa.add(new Armadilha(flechaX, flechaY, flechaW, flechaH, 5 * dx, 5 * dy));
            }
            return;
        }
        if ("LANCA_CHAMAS".equals(tipo)) {
            cicloFogoFrames++;
            if (cicloFogoFrames % 150 == 0) {
                direcaoFogo *= -1;
            }

            if (!jatoFogoAtivo()) {
                comprimentoJatoAtual = 0;
                return;
            }

            int alcanceMaximo = 130;
            comprimentoJatoAtual = Math.min(alcanceMaximo, comprimentoJatoAtual + 10);

            // Limita o jato se encontrar uma plataforma no caminho
            if (pecasDoMapa != null) {
                for (int dist = 10; dist <= comprimentoJatoAtual; dist += 10) {
                    int testeX = (direcaoFogo >= 0) ? (getX() + getLargura() + dist - 10) : (getX() - dist);
                    Rectangle pontoTeste = new Rectangle(testeX, getY() + getAltura() / 2 - 4, 10, 8);
                    boolean bloqueado = false;
                    for (ElementoJogo peca : pecasDoMapa) {
                        if (peca != this && peca instanceof Plataforma && pontoTeste.intersects(peca.getLimites())) {
                            bloqueado = true;
                            break;
                        }
                    }
                    if (bloqueado) {
                        comprimentoJatoAtual = Math.max(0, dist - 10);
                        break;
                    }
                }
            }
            return;
        }

        if ("SERRA_TRILHO".equals(tipo)) {
            int raio = Math.min(getAltura(), 28);
            int maxDeslocamento = Math.max(0, getLargura() - raio);
            serraOffsetX += direcaoSerra * 2;
            if (serraOffsetX >= maxDeslocamento) {
                serraOffsetX = maxDeslocamento;
                direcaoSerra = -1;
            } else if (serraOffsetX <= 0) {
                serraOffsetX = 0;
                direcaoSerra = 1;
            }
            return;
        }

        if (velocidadeX == 0 && velocidadeY == 0) return;

        setX(getX() + velocidadeX);
        setY(getY() + velocidadeY);

        boolean saiuDoMapa = (getX() + getLargura() < 0) || (getX() > 1280)
                || (getY() + getAltura() < 0) || (getY() > 720);
        boolean bateuEmSolido = false;

        if (pecasDoMapa != null) {
            for (ElementoJogo peca : pecasDoMapa) {
                if (peca == this) continue;
                if (this.getLimites().intersects(peca.getLimites())) {
                    bateuEmSolido = true;
                    break;
                }
            }
        }

        if (saiuDoMapa || bateuEmSolido) {
            pecasDoMapa.remove(this);
        }
    }

    private void desenharSerraCircular(Graphics g, int cx, int cy, int raio) {
        int r = raio / 2;
        g.setColor(new Color(195, 195, 210));
        g.fillOval(cx - r, cy - r, raio, raio);

        g.setColor(new Color(130, 130, 150));
        for (int i = 0; i < 8; i++) {
            double ang = i * (Math.PI / 4.0);
            int dx = (int) (Math.cos(ang) * (r + 3));
            int dy = (int) (Math.sin(ang) * (r + 3));
            g.fillOval(cx + dx - 3, cy + dy - 3, 6, 6);
        }

        g.setColor(new Color(80, 80, 95));
        g.fillOval(cx - 4, cy - 4, 8, 8);
    }

    private void desenharLancaChamas(Graphics g) {
        int x = getX();
        int y = getY();
        int w = getLargura();
        int h = getAltura();

        // Bloco quadrado pisavel metalico
        g.setColor(new Color(75, 82, 92));
        g.fillRoundRect(x, y, w, h, 6, 6);
        g.setColor(new Color(40, 45, 52));
        g.drawRoundRect(x, y, w, h, 6, 6);

        // Topo pisavel reforçado
        g.setColor(new Color(130, 140, 155));
        g.fillRect(x + 2, y + 2, Math.max(4, w - 4), 6);

        // Detalhe de tanque/chama no centro do bloco
        g.setColor(jatoFogoAtivo() ? new Color(255, 115, 20) : new Color(175, 65, 35));
        g.fillOval(x + w / 2 - 7, y + h / 2 - 4, 14, 14);

        // Bico horizontal lateral
        int bicoY = y + h / 2 - 4;
        if (direcaoFogo >= 0) {
            g.setColor(new Color(105, 112, 125));
            g.fillRect(x + w - 2, bicoY, 8, 8);
        } else {
            g.setColor(new Color(105, 112, 125));
            g.fillRect(x - 6, bicoY, 8, 8);
        }

        // Jato de fogo horizontal quando ativo
        Rectangle jato = getHitboxJatoFogo();
        if (!jato.isEmpty()) {
            g.setColor(new Color(255, 80, 15, 210));
            g.fillRoundRect(jato.x, jato.y, jato.width, jato.height, 10, 10);

            g.setColor(new Color(255, 175, 30, 230));
            g.fillRoundRect(jato.x, jato.y + 3, jato.width, Math.max(4, jato.height - 6), 8, 8);

            g.setColor(new Color(255, 245, 150));
            int nucleoW = Math.max(8, jato.width / 2);
            int nucleoX = (direcaoFogo >= 0) ? jato.x : (jato.x + jato.width - nucleoW);
            g.fillRoundRect(nucleoX, jato.y + 6, nucleoW, Math.max(3, jato.height - 12), 6, 6);
        }
    }

    @Override
    public void desenhar(Graphics g) {
        if ("BESTA".equals(tipo)) {
            desenharBesta(g);
            return;
        }
        if ("LANCA_CHAMAS".equals(tipo)) {
            desenharLancaChamas(g);
            return;
        }

        if ("SERRA_TRILHO".equals(tipo)) {
            g.setColor(new Color(135, 115, 145));
            g.fillRoundRect(getX(), getY() + 4, getLargura(), Math.max(12, getAltura() - 8), 8, 8);

            g.setColor(new Color(235, 145, 35));
            g.fillRect(getX() + 4, getY() + 4, Math.max(1, getLargura() - 8), 5);
            g.setColor(new Color(95, 80, 105));
            g.fillRoundRect(getX() + 6, getY() + getAltura() / 2 - 2, Math.max(1, getLargura() - 12), 5, 4, 4);

            Rectangle serraBox = getHitboxSerraTrilho();
            desenharSerraCircular(g, serraBox.x + serraBox.width / 2, serraBox.y + serraBox.height / 2, serraBox.width);
            return;
        }

        if ("SERRA_FLUTUANTE".equals(tipo)) {
            int raio = Math.min(getLargura(), getAltura());
            desenharSerraCircular(g, getX() + getLargura() / 2, getY() + getAltura() / 2, raio);
            return;
        }

        if ("FLECHA".equals(tipo)) {
            g.setColor(Color.ORANGE);
            g.fillRect(getX(), getY() + 2, getLargura() - 6, Math.max(4, getAltura() - 4));
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

    private void desenharBesta(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        int cx = getX() + getLargura() / 2;
        int cy = getY() + getAltura() / 2;
        g2d.rotate(rotacao * Math.PI / 2, cx, cy);
        g2d.setColor(new Color(75, 55, 35));
        g2d.fillRoundRect(getX() + 4, getY() + 14, 32, 12, 4, 4);
        g2d.setColor(new Color(195, 160, 90));
        g2d.drawLine(getX() + 8, getY() + 7, getX() + 8, getY() + 33);
        g2d.drawLine(getX() + 8, getY() + 7, getX() + 32, getY() + 20);
        g2d.drawLine(getX() + 8, getY() + 33, getX() + 32, getY() + 20);
        g2d.setColor(new Color(210, 215, 220));
        g2d.fillRect(getX() + 20, getY() + 18, 18, 4);
        g2d.dispose();
    }

    @Override
    public void aoColidir(Jogador jogador) {
        if ("LANCA_CHAMAS".equals(tipo)) {
            // 1. Se encostar no jato de fogo horizontal, morre
            if (causarDanoSeAtingirPeloJato(jogador)) {
                return;
            }

            // 2. O corpo do lança-chamas e pisavel como plataforma solida
            Rectangle rectJogador = jogador.getLimites();
            Rectangle rectBloco = this.getLimites();
            Rectangle intersecao = rectJogador.intersection(rectBloco);
            if (intersecao.isEmpty()) return;

            if (intersecao.width >= intersecao.height) {
                if (jogador.getY() < rectBloco.y && jogador.getVelocidadeY() >= 0) {
                    jogador.setY(rectBloco.y - jogador.getAltura());
                    jogador.setVelocidadeY(0);
                    jogador.setNoChao(true);
                } else if (jogador.getY() > rectBloco.y && jogador.getVelocidadeY() < 0) {
                    jogador.setY(rectBloco.y + rectBloco.height);
                    jogador.setVelocidadeY(0);
                }
            } else {
                if (jogador.getX() < rectBloco.x) {
                    jogador.setX(rectBloco.x - jogador.getLargura());
                } else {
                    jogador.setX(rectBloco.x + rectBloco.width);
                }
            }
            return;
        }

        if ("SERRA_TRILHO".equals(tipo)) {
            Rectangle serraBox = getHitboxSerraTrilho();
            if (jogador.getLimites().intersects(serraBox)) {
                jogador.morrer();
                return;
            }
            Rectangle rectJogador = jogador.getLimites();
            Rectangle rectBarra = this.getLimites();
            Rectangle intersecao = rectJogador.intersection(rectBarra);
            if (!intersecao.isEmpty() && intersecao.width >= intersecao.height) {
                if (jogador.getY() < this.getY() && jogador.getVelocidadeY() >= 0) {
                    jogador.setY(this.getY() - jogador.getAltura());
                    jogador.setVelocidadeY(0);
                    jogador.setNoChao(true);
                }
            }
            return;
        }

        jogador.morrer();
    }
}
