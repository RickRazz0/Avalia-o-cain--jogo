package jogo.obstaculos;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import jogo.base.ElementoJogo;
import jogo.jogadores.Jogador;

public class Plataforma extends ElementoJogo {

    private final Color cor;
    private final String tipo; // "CHAO", "MADEIRA", "MADEIRINHA", "DEGRAU", "ESCADARIA", "MOLA"

    // Animacao da mola ao receber contato por cima
    private int animacaoMolaFrames = 0;
    private final int DURACAO_ANIM_MOLA = 18;

    public Plataforma(int x, int y, int largura, int altura, Color cor) {
        this(x, y, largura, altura, cor, "MADEIRA");
    }

    public Plataforma(int x, int y, int largura, int altura, Color cor, String tipo) {
        super(x, y, largura, altura);
        this.cor = cor;
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }

    public void atualizar() {
        if ("MOLA".equals(tipo) && animacaoMolaFrames > 0) {
            animacaoMolaFrames--;
        }
    }

    private void desenharMola(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Base inferior fixa da mola
        int baseAltura = 6;
        int baseY = getY() + getAltura() - baseAltura;
        g2d.setColor(new Color(90, 95, 110));
        g2d.fillRect(getX(), baseY, getLargura(), baseAltura);
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRect(getX(), baseY, getLargura(), baseAltura);

        // Calcula o quanto a placa superior sobe/comprime durante a animacao
        double deslocamentoTopo = 0;
        if (animacaoMolaFrames > 0) {
            double fase = 1.0 - ((double) animacaoMolaFrames / DURACAO_ANIM_MOLA);
            // Comprime rapido no inicio e depois estica para cima (-10px)
            deslocamentoTopo = -Math.sin(fase * Math.PI) * 10.0;
        }

        int topoY = (int) Math.round(getY() + deslocamentoTopo);
        int topoAltura = 6;

        // Espiral / zigue-zague da mola entre a placa superior e a base
        int molaTopoY = topoY + topoAltura;
        int molaBaseY = baseY;
        int espiras = 4;
        int xInicio = getX() + 10;
        int xFim = getX() + getLargura() - 10;
        int larguraUtil = Math.max(10, xFim - xInicio);

        g2d.setColor(new Color(220, 225, 235));
        g2d.setStroke(new BasicStroke(2.5f));

        int prevX = xInicio + larguraUtil / 2;
        int prevY = molaTopoY;
        for (int i = 1; i <= espiras; i++) {
            double frac = (double) i / espiras;
            int proxY = (int) Math.round(molaTopoY + frac * (molaBaseY - molaTopoY));
            int proxX = (i % 2 == 1) ? xInicio : xFim;
            if (i == espiras) {
                proxX = xInicio + larguraUtil / 2;
            }
            g2d.drawLine(prevX, prevY, proxX, proxY);
            prevX = proxX;
            prevY = proxY;
        }

        // Placa pisavel superior da mola (vermelho/laranja vibrante)
        g2d.setColor(new Color(225, 65, 45));
        g2d.fillRect(getX(), topoY, getLargura(), topoAltura);
        g2d.setColor(new Color(140, 25, 20));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRect(getX(), topoY, getLargura(), topoAltura);
    }

    @Override
    public void desenhar(Graphics g) {
        if ("MOLA".equals(tipo)) {
            desenharMola(g);
            return;
        }

        if ("ESCADARIA".equals(tipo)) {
            int degrauW = Math.max(20, getLargura() / 3);
            int degrauH = Math.max(20, getAltura() / 3);
            for (int i = 0; i < 3; i++) {
                int dx = getX() + i * degrauW;
                int dy = getY() + getAltura() - (i + 1) * degrauH;
                g.setColor(cor);
                g.fillRect(dx, dy, degrauW, degrauH);
                g.setColor(cor.darker());
                g.drawRect(dx, dy, degrauW, degrauH);
            }
            return;
        }

        g.setColor(cor);
        g.fillRect(getX(), getY(), getLargura(), getAltura());

        g.setColor(cor.darker());
        g.drawRect(getX(), getY(), getLargura(), getAltura());
    }

    @Override
    public void aoColidir(Jogador jogador) {
        if ("MOLA".equals(tipo)) {
            Rectangle rectJogador = jogador.getLimites();
            Rectangle rectPlataforma = this.getLimites();
            Rectangle intersecao = rectJogador.intersection(rectPlataforma);
            if (intersecao.isEmpty()) return;

            // Contato por cima (subindo ou caindo sobre o topo da mola)
            boolean porCima = (jogador.getY() + jogador.getAltura() <= this.getY() + 14)
                    && jogador.getVelocidadeY() >= 0;

            if (porCima || (intersecao.width > intersecao.height && jogador.getY() < this.getY())) {
                jogador.setY(this.getY() - jogador.getAltura());
                jogador.setVelocidadeY(-28); // Dobro da altura do pulo normal (-14)
                jogador.setNoChao(false);
                animacaoMolaFrames = DURACAO_ANIM_MOLA;
                return;
            }

            // Contato lateral ou por baixo: estatico como objeto solido (sem impulsionar para cima)
            resolverColisaoRetangulo(jogador, rectPlataforma);
            return;
        }

        if ("ESCADARIA".equals(tipo)) {
            int degrauW = Math.max(20, getLargura() / 3);
            int degrauH = Math.max(20, getAltura() / 3);
            for (int i = 0; i < 3; i++) {
                int dx = getX() + i * degrauW;
                int dy = getY() + getAltura() - (i + 1) * degrauH;
                Rectangle degrauRect = new Rectangle(dx, dy, degrauW, degrauH);
                if (jogador.getLimites().intersects(degrauRect)) {
                    resolverColisaoRetangulo(jogador, degrauRect);
                }
            }
            return;
        }

        resolverColisaoRetangulo(jogador, this.getLimites());
    }

    private void resolverColisaoRetangulo(Jogador jogador, Rectangle rectAlvo) {
        Rectangle rectJogador = jogador.getLimites();
        Rectangle intersecao = rectJogador.intersection(rectAlvo);
        if (intersecao.isEmpty()) return;

        if (intersecao.width >= intersecao.height) {
            if (jogador.getY() < rectAlvo.y && jogador.getVelocidadeY() >= 0) {
                jogador.setY(rectAlvo.y - jogador.getAltura());
                jogador.setVelocidadeY(0);
                jogador.setNoChao(true);
            } else if (jogador.getY() > rectAlvo.y && jogador.getVelocidadeY() < 0) {
                jogador.setY(rectAlvo.y + rectAlvo.height);
                jogador.setVelocidadeY(0);
            }
        } else {
            if (jogador.getX() < rectAlvo.x) {
                jogador.setX(rectAlvo.x - jogador.getLargura());
            } else {
                jogador.setX(rectAlvo.x + rectAlvo.width);
            }
        }
    }
}