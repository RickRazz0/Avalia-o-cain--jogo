package jogo.principal;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import jogo.base.ElementoJogo;
import jogo.jogadores.Jogador;
import jogo.obstaculos.Arco;
import jogo.obstaculos.Armadilha;
import jogo.obstaculos.Chegada;
import jogo.obstaculos.Plataforma;
import jogo.ui.PergaminhoPlacar;

public class App extends JPanel implements ActionListener, KeyListener {

    private Jogador player1;
    private List<ElementoJogo> pecasDoMapa;
    private PergaminhoPlacar pergaminho;
    private Timer loopJogo;

    private boolean indoEsquerda = false;
    private boolean indoDireita = false;

    public App() {
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(this);

        this.player1 = new Jogador(80, 350, Color.RED);
        this.pergaminho = new PergaminhoPlacar();
        this.pecasDoMapa = new ArrayList<>();

        // 1. Plataforma Inicial (Esquerda)
        pecasDoMapa.add(new Plataforma(20, 420, 160, 180, Color.DARK_GRAY));

        // 2. Plataforma de Madeira Flutuante (Marrom)
        pecasDoMapa.add(new Plataforma(230, 400, 120, 25, new Color(139, 69, 19)));

        // 3. Escada Flutuante
        pecasDoMapa.add(new Plataforma(380, 310, 50, 20, Color.GRAY));
        pecasDoMapa.add(new Plataforma(430, 285, 50, 20, Color.GRAY));
        pecasDoMapa.add(new Plataforma(480, 260, 50, 20, Color.GRAY));

        // 4. Armadilha de Espinhos embaixo
        pecasDoMapa.add(new Plataforma(310, 510, 140, 40, Color.YELLOW));
        pecasDoMapa.add(new Armadilha(315, 495, 130, 15, 0));

        // 5. O Arco na plataforma da direita + Flecha mais lenta (-3 em vez de -5)
        pecasDoMapa.add(new Arco(610, 360));
        pecasDoMapa.add(new Armadilha(600, 380, 30, 8, -3));

        // 6. Plataforma Final (Direita) + Bandeira Verde
        pecasDoMapa.add(new Plataforma(640, 420, 140, 180, Color.DARK_GRAY));
        pecasDoMapa.add(new Chegada(720, 370));

        this.loopJogo = new Timer(16, this);
        this.loopJogo.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        int offsetX = (getWidth() - 800) / 2;
        int offsetY = (getHeight() - 600) / 2;
        g2d.translate(offsetX, offsetY);

        for (ElementoJogo peca : pecasDoMapa) {
            peca.desenhar(g2d);
        }
        player1.desenhar(g2d);

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.drawString("Round: " + pergaminho.getRoundAtual() + "  |  Vitórias: " + pergaminho.getPontos() + "/4  |  ESC: Sair", 20, 30);

        pergaminho.desenhar(g2d);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (pergaminho.isAberto()) {
            pergaminho.atualizarAnimacao();
            repaint();
            return;
        }

        if (indoEsquerda) player1.moverEsquerda();
        if (indoDireita)  player1.moverDireita();
        player1.aplicarGravidade();

        for (ElementoJogo peca : pecasDoMapa) {
            if (peca instanceof Armadilha) {
                ((Armadilha) peca).atualizar();
            }
            if (player1.getLimites().intersects(peca.getLimites())) {
                peca.aoColidir(player1);
                // Se encostou na bandeira ou na armadilha, para o loop imediatamente!
                if (player1.isVenceuNoRound() || player1.isMorreuNoRound()) {
                    break;
                }
            }
        }

        if (player1.isVenceuNoRound()) {
            indoEsquerda = false;
            indoDireita = false;
            pergaminho.abrir(true);
        } else if (player1.isMorreuNoRound()) {
            indoEsquerda = false;
            indoDireita = false;
            pergaminho.abrir(false);
        }

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int tecla = e.getKeyCode();

        if (tecla == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }

        if (pergaminho.isAberto()) {
            if (tecla == KeyEvent.VK_SPACE || tecla == KeyEvent.VK_ENTER) {
                pergaminho.avancarRound();
                player1.resetarParaNovoRound();
            }
            return;
        }

        if (tecla == KeyEvent.VK_LEFT  || tecla == KeyEvent.VK_A) indoEsquerda = true;
        if (tecla == KeyEvent.VK_RIGHT || tecla == KeyEvent.VK_D) indoDireita = true;
        if (tecla == KeyEvent.VK_UP    || tecla == KeyEvent.VK_W || tecla == KeyEvent.VK_SPACE) {
            player1.pular();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int tecla = e.getKeyCode();
        if (tecla == KeyEvent.VK_LEFT  || tecla == KeyEvent.VK_A) indoEsquerda = false;
        if (tecla == KeyEvent.VK_RIGHT || tecla == KeyEvent.VK_D) indoDireita = false;
    }

    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame moldura = new JFrame("Ultimate Chicken Horse - Versão Java");
        App telaPreta = new App();

        moldura.add(telaPreta);
        moldura.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        moldura.setUndecorated(true);
        moldura.setExtendedState(JFrame.MAXIMIZED_BOTH);
        moldura.setVisible(true);
    }
}