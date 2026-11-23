package jogo.principal;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import jogo.base.ElementoJogo;
import jogo.jogadores.Jogador;
import jogo.obstaculos.Arco;
import jogo.obstaculos.Armadilha;
import jogo.obstaculos.Chegada;
import jogo.obstaculos.Plataforma;
import jogo.ui.FaseConstrucao;
import jogo.ui.PergaminhoPlacar;

public class App extends JPanel implements ActionListener, KeyListener, MouseListener {

    private Jogador player1;
    private Jogador player2;
    private List<ElementoJogo> pecasDoMapa;
    private PergaminhoPlacar pergaminho;
    private FaseConstrucao faseConstrucao;
    private Timer loopJogo;

    private boolean naTelaInicial = true;
    private final Rectangle botaoJogar = new Rectangle(310, 320, 180, 56);

    private boolean p1Esquerda = false;
    private boolean p1Direita = false;

    private boolean p2Esquerda = false;
    private boolean p2Direita = false;

    private final int P1_X_INICIAL = 70;
    private final int P2_X_INICIAL = 115;

    public App() {
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(this);
        this.addMouseListener(this);

        this.player1 = new Jogador(P1_X_INICIAL, 350, Color.RED);
        this.player2 = new Jogador(P2_X_INICIAL, 350, Color.BLUE);
        this.pergaminho = new PergaminhoPlacar();
        this.faseConstrucao = new FaseConstrucao();
        this.pecasDoMapa = new ArrayList<>();

        montarCenarioBase();

        this.loopJogo = new Timer(16, this);
        this.loopJogo.start();
    }

    private void iniciarPartida() {
        naTelaInicial = false;
        this.faseConstrucao.iniciar();
    }

    private void montarCenarioBase() {
        pecasDoMapa.clear();

        // 1. Plataforma Inicial (Esquerda)
        pecasDoMapa.add(new Plataforma(20, 420, 160, 180, Color.DARK_GRAY, "CHAO"));

        // 2. Plataforma de Madeira Flutuante (Marrom)
        pecasDoMapa.add(new Plataforma(230, 400, 120, 25, new Color(139, 69, 19), "MADEIRA"));

        // 3. Escada Flutuante
        pecasDoMapa.add(new Plataforma(380, 310, 50, 20, Color.GRAY, "DEGRAU"));
        pecasDoMapa.add(new Plataforma(430, 285, 50, 20, Color.GRAY, "DEGRAU"));
        pecasDoMapa.add(new Plataforma(480, 260, 50, 20, Color.GRAY, "DEGRAU"));

        // 4. Armadilha de Espinhos embaixo
        pecasDoMapa.add(new Plataforma(310, 510, 140, 40, Color.YELLOW, "CHAO"));
        pecasDoMapa.add(new Armadilha(315, 495, 130, 15, 0, "ESPINHO"));

        // 5. O Arco na plataforma da direita + Flecha
        pecasDoMapa.add(new Arco(610, 360));
        pecasDoMapa.add(new Armadilha(600, 380, 30, 8, -3, "FLECHA"));

        // 6. Plataforma Final (Direita) + Bandeira Verde
        pecasDoMapa.add(new Plataforma(640, 420, 140, 180, Color.DARK_GRAY, "CHAO"));
        pecasDoMapa.add(new Chegada(720, 370));
    }

    private void limparMovimentos() {
        p1Esquerda = false;
        p1Direita = false;
        p2Esquerda = false;
        p2Direita = false;
    }

    private void processarJogador(Jogador jogador, boolean esq, boolean dir) {
        if (jogador.isVenceuNoRound() || jogador.isMorreuNoRound()) return;

        if (esq) jogador.moverEsquerda();
        if (dir) jogador.moverDireita();
        jogador.aplicarGravidade();

        for (ElementoJogo peca : pecasDoMapa) {
            if (jogador.getLimites().intersects(peca.getLimites())) {
                peca.aoColidir(jogador);
                if (jogador.isVenceuNoRound() || jogador.isMorreuNoRound()) {
                    break;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        int offsetX = (getWidth() - 800) / 2;
        int offsetY = (getHeight() - 600) / 2;
        g2d.translate(offsetX, offsetY);

        if (naTelaInicial) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 38));
            g2d.drawString("ULTIMATE CHICKEN HORSE - JAVA", 95, 220);

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 16));
            g2d.drawString("Modo Local 2 Jogadores  |  ESC para sair", 250, 265);

            // Botao JOGAR
            g2d.setColor(new Color(46, 160, 67));
            g2d.fillRoundRect(botaoJogar.x, botaoJogar.y, botaoJogar.width, botaoJogar.height, 14, 14);
            g2d.setColor(Color.WHITE);
            g2d.drawRoundRect(botaoJogar.x, botaoJogar.y, botaoJogar.width, botaoJogar.height, 14, 14);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
            g2d.drawString("JOGAR", botaoJogar.x + 52, botaoJogar.y + 36);
            return;
        }

        for (ElementoJogo peca : pecasDoMapa) {
            peca.desenhar(g2d);
        }

        if (!player1.isMorreuNoRound()) {
            player1.desenhar(g2d);
        }
        if (!player2.isMorreuNoRound()) {
            player2.desenhar(g2d);
        }

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));

        if (faseConstrucao.isAtiva()) {
            g2d.drawString(
                "CONSTRUCAO | P1: WASD + [N] confirmar | P2: Setas + [M] confirmar",
                20, 30
            );
            faseConstrucao.desenhar(g2d, pecasDoMapa);
        } else {
            g2d.drawString(
                "Round: " + pergaminho.getRoundAtual() +
                "  |  P1: " + pergaminho.getPontosP1() + "/4" +
                "  |  P2: " + pergaminho.getPontosP2() + "/4" +
                "  |  P1: WASD  |  P2: Setas  |  ESC: Sair",
                20, 30
            );
        }

        pergaminho.desenhar(g2d);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (naTelaInicial) {
            repaint();
            return;
        }

        if (pergaminho.isAberto()) {
            pergaminho.atualizarAnimacao();
            repaint();
            return;
        }

        if (faseConstrucao.isAtiva()) {
            faseConstrucao.atualizarAnimacao();
            faseConstrucao.encerrarSePronto(pecasDoMapa);
            repaint();
            return;
        }

        for (ElementoJogo peca : pecasDoMapa) {
            if (peca instanceof Armadilha) {
                ((Armadilha) peca).atualizar(pecasDoMapa);
            }
        }

        processarJogador(player1, p1Esquerda, p1Direita);
        processarJogador(player2, p2Esquerda, p2Direita);

        boolean p1Venceu = player1.isVenceuNoRound();
        boolean p2Venceu = player2.isVenceuNoRound();
        boolean p1Morreu = player1.isMorreuNoRound();
        boolean p2Morreu = player2.isMorreuNoRound();

        if (p1Venceu || p2Venceu || (p1Morreu && p2Morreu)) {
            limparMovimentos();
            pergaminho.abrir(p1Venceu, p2Venceu);
        }

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int tecla = e.getKeyCode();

        if (tecla == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }

        if (naTelaInicial) {
            if (tecla == KeyEvent.VK_ENTER || tecla == KeyEvent.VK_SPACE) {
                iniciarPartida();
            }
            return;
        }

        if (pergaminho.isAberto()) {
            if (tecla == KeyEvent.VK_SPACE || tecla == KeyEvent.VK_ENTER) {
                pergaminho.avancarRound();
                player1.resetarParaNovoRound(P1_X_INICIAL);
                player2.resetarParaNovoRound(P2_X_INICIAL);
                faseConstrucao.iniciar();
            }
            return;
        }

        if (faseConstrucao.isAtiva()) {
            // Player 1: W (cima), S (baixo), A (esq), D (dir), N (confirmar)
            if (tecla == KeyEvent.VK_W) faseConstrucao.acaoP1("CIMA", pecasDoMapa);
            if (tecla == KeyEvent.VK_S) faseConstrucao.acaoP1("BAIXO", pecasDoMapa);
            if (tecla == KeyEvent.VK_A) faseConstrucao.acaoP1("ESQ", pecasDoMapa);
            if (tecla == KeyEvent.VK_D) faseConstrucao.acaoP1("DIR", pecasDoMapa);
            if (tecla == KeyEvent.VK_N) faseConstrucao.acaoP1("CONFIRMAR", pecasDoMapa);

            // Player 2: Seta Cima, Seta Baixo, Esq, Dir, M (confirmar)
            if (tecla == KeyEvent.VK_UP)    faseConstrucao.acaoP2("CIMA", pecasDoMapa);
            if (tecla == KeyEvent.VK_DOWN)  faseConstrucao.acaoP2("BAIXO", pecasDoMapa);
            if (tecla == KeyEvent.VK_LEFT)  faseConstrucao.acaoP2("ESQ", pecasDoMapa);
            if (tecla == KeyEvent.VK_RIGHT) faseConstrucao.acaoP2("DIR", pecasDoMapa);
            if (tecla == KeyEvent.VK_M)     faseConstrucao.acaoP2("CONFIRMAR", pecasDoMapa);
            return;
        }

        // Modo corrida normal
        if (tecla == KeyEvent.VK_A) p1Esquerda = true;
        if (tecla == KeyEvent.VK_D) p1Direita = true;
        if (tecla == KeyEvent.VK_W) player1.pular();

        if (tecla == KeyEvent.VK_LEFT)  p2Esquerda = true;
        if (tecla == KeyEvent.VK_RIGHT) p2Direita = true;
        if (tecla == KeyEvent.VK_UP)    player2.pular();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int tecla = e.getKeyCode();

        if (tecla == KeyEvent.VK_A) p1Esquerda = false;
        if (tecla == KeyEvent.VK_D) p1Direita = false;

        if (tecla == KeyEvent.VK_LEFT)  p2Esquerda = false;
        if (tecla == KeyEvent.VK_RIGHT) p2Direita = false;
    }

    @Override public void keyTyped(KeyEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if (!naTelaInicial) return;

        int offsetX = (getWidth() - 800) / 2;
        int offsetY = (getHeight() - 600) / 2;
        int mx = e.getX() - offsetX;
        int my = e.getY() - offsetY;

        if (botaoJogar.contains(mx, my)) {
            iniciarPartida();
        }
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        JFrame moldura = new JFrame("Ultimate Chicken Horse - Versao Java");
        App telaPreta = new App();

        moldura.add(telaPreta);
        moldura.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        moldura.setUndecorated(true);
        moldura.setExtendedState(JFrame.MAXIMIZED_BOTH);
        moldura.setVisible(true);
    }
}