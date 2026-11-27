package jogo.principal;

import java.awt.BasicStroke;
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
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import jogo.base.ElementoJogo;
import jogo.jogadores.Jogador;
import jogo.obstaculos.Armadilha;
import jogo.obstaculos.Chegada;
import jogo.obstaculos.Plataforma;
import jogo.ui.FaseConstrucao;
import jogo.ui.PergaminhoPlacar;

public class App extends JPanel implements ActionListener, KeyListener, MouseListener {

    private static final int LARGURA_PALCO = 1280;
    private static final int ALTURA_PALCO = 720;

    private final Jogador player1;
    private final Jogador player2;
    private final List<ElementoJogo> pecasDoMapa;
    private final PergaminhoPlacar pergaminho;
    private final FaseConstrucao faseConstrucao;
    private final Timer loopJogo;

    private boolean naTelaInicial = true;
    private boolean telaVitoria = false;
    private String textoVencedor = "";
    private Color corVencedor = Color.YELLOW;

    private final Rectangle botaoJogar = new Rectangle(550, 380, 180, 56);
    private final Rectangle botaoJogarNovamente = new Rectangle(515, 450, 250, 56);

    private boolean p1Esquerda = false;
    private boolean p1Direita = false;

    private boolean p2Esquerda = false;
    private boolean p2Direita = false;

    private final int P1_X_INICIAL = 60;
    private final int P2_X_INICIAL = 105;

    public App() {
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(this);
        this.addMouseListener(this);

        this.player1 = new Jogador(P1_X_INICIAL, 480, Color.RED);
        this.player2 = new Jogador(P2_X_INICIAL, 480, Color.BLUE);
        this.pergaminho = new PergaminhoPlacar();
        this.faseConstrucao = new FaseConstrucao();
        this.pecasDoMapa = new ArrayList<>();

        montarCenarioBase();

        this.loopJogo = new Timer(16, this);
        this.loopJogo.start();
    }

    private double getEscalaX() {
        return (double) getWidth() / LARGURA_PALCO;
    }

    private double getEscalaY() {
        return (double) getHeight() / ALTURA_PALCO;
    }

    private void iniciarPartida() {
        naTelaInicial = false;
        telaVitoria = false;
        this.faseConstrucao.iniciar();
    }

    private void abrirTelaVitoria() {
        int p1 = pergaminho.getPontosP1();
        int p2 = pergaminho.getPontosP2();

        if (p1 >= 4 && p2 >= 4) {
            textoVencedor = "EMPATE! AMBOS COMPLETARAM 4/4!";
            corVencedor = Color.ORANGE;
        } else if (p1 >= 4) {
            textoVencedor = "PLAYER 1 (VERMELHO) COMPLETOU AS 4 VITORIAS!";
            corVencedor = new Color(235, 70, 70);
        } else {
            textoVencedor = "PLAYER 2 (AZUL) COMPLETOU AS 4 VITORIAS!";
            corVencedor = new Color(70, 145, 255);
        }

        pergaminho.avancarRound();
        telaVitoria = true;
    }

    private void reiniciarPartidaCompleta() {
        telaVitoria = false;
        pergaminho.resetarPartida();
        montarCenarioBase();
        player1.resetarParaNovoRound(P1_X_INICIAL);
        player2.resetarParaNovoRound(P2_X_INICIAL);
        faseConstrucao.iniciar();
    }

    private void montarCenarioBase() {
        pecasDoMapa.clear();
        pecasDoMapa.add(new Plataforma(20, 520, 160, 200, Color.DARK_GRAY, "CHAO"));
        pecasDoMapa.add(new Plataforma(1080, 520, 180, 200, Color.DARK_GRAY, "CHAO"));
        pecasDoMapa.add(new Chegada(1170, 470));
    }

    private void limparMovimentos() {
        p1Esquerda = false;
        p1Direita = false;
        p2Esquerda = false;
        p2Direita = false;
    }

    private void resetarPlataformasDoRound() {
        for (ElementoJogo peca : pecasDoMapa) {
            if (peca instanceof Plataforma plataforma) {
                plataforma.resetarParaNovoRound();
            }
        }
    }

    private void processarJogador(Jogador jogador, boolean esq, boolean dir) {
        if (jogador.isVenceuNoRound() || jogador.isMorreuNoRound()) return;

        // 1. Movimento horizontal e colisao lateral antes da gravidade
        if (esq && !dir) {
            jogador.moverEsquerda();
        } else if (dir && !esq) {
            jogador.moverDireita();
        }

        if (verificarJatosDeFogo(jogador)) return;

        for (ElementoJogo peca : pecasDoMapa) {
            if (!(peca instanceof Plataforma) && !(peca instanceof Armadilha)) continue;
            if (jogador.getLimites().intersects(peca.getLimites())) {
                if (peca instanceof Armadilha arm && !"LANCA_CHAMAS".equals(arm.getTipo()) && !"SERRA_TRILHO".equals(arm.getTipo())) {
                    peca.aoColidir(jogador);
                    if (jogador.isMorreuNoRound()) return;
                }
            }
        }

        // 2. Gravidade e movimento vertical
        jogador.aplicarGravidade();
        if (jogador.isMorreuNoRound()) return;

        if (verificarJatosDeFogo(jogador)) return;

        // 3. Colisoes gerais (plataformas, molas, escadas, armadilhas, chegada)
        for (ElementoJogo peca : pecasDoMapa) {
            if (jogador.getLimites().intersects(peca.getLimites())) {
                peca.aoColidir(jogador);
                if (jogador.isVenceuNoRound() || jogador.isMorreuNoRound()) {
                    return;
                }
            }
        }

        // 4. Consome o pulo ja sabendo com precisao se o jogador esta apoiado no chao
        jogador.consumirPulo();
    }

    private boolean verificarJatosDeFogo(Jogador jogador) {
        for (ElementoJogo peca : pecasDoMapa) {
            if (peca instanceof Armadilha armadilha
                    && armadilha.causarDanoSeAtingirPeloJato(jogador)) {
                return true;
            }
        }
        return false;
    }

    private void desenharTelaVitoria(Graphics2D g2d) {
        g2d.setColor(new Color(15, 18, 26));
        g2d.fillRect(0, 0, LARGURA_PALCO, ALTURA_PALCO);

        g2d.setColor(corVencedor);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRoundRect(220, 130, 840, 430, 24, 24);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 42));
        g2d.drawString("TELA DE VITORIA", 460, 205);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 26));
        g2d.drawString(textoVencedor, 315, 275);

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 22));
        g2d.drawString(
            "Placar Final  |  Player 1: " + pergaminho.getPontosP1() + "/4   -   Player 2: " + pergaminho.getPontosP2() + "/4",
            390, 355
        );

        g2d.setColor(new Color(46, 160, 67));
        g2d.fillRoundRect(botaoJogarNovamente.x, botaoJogarNovamente.y, botaoJogarNovamente.width, botaoJogarNovamente.height, 14, 14);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(botaoJogarNovamente.x, botaoJogarNovamente.y, botaoJogarNovamente.width, botaoJogarNovamente.height, 14, 14);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        g2d.drawString("JOGAR NOVAMENTE", botaoJogarNovamente.x + 24, botaoJogarNovamente.y + 35);

        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g2d.drawString("Pressione [ESPACO] ou [ENTER] para reiniciar  |  [ESC] para sair", 415, 535);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.scale(getEscalaX(), getEscalaY());

        if (naTelaInicial) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 44));
            g2d.drawString("ULTIMATE CHICKEN HORSE - JAVA", 275, 280);

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 18));
            g2d.drawString("Modo Local 2 Jogadores  |  ESC para sair", 460, 330);

            g2d.setColor(new Color(46, 160, 67));
            g2d.fillRoundRect(botaoJogar.x, botaoJogar.y, botaoJogar.width, botaoJogar.height, 14, 14);
            g2d.setColor(Color.WHITE);
            g2d.drawRoundRect(botaoJogar.x, botaoJogar.y, botaoJogar.width, botaoJogar.height, 14, 14);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
            g2d.drawString("JOGAR", botaoJogar.x + 52, botaoJogar.y + 36);
            return;
        }

        if (telaVitoria) {
            desenharTelaVitoria(g2d);
            return;
        }

        for (ElementoJogo peca : pecasDoMapa) {
            peca.desenhar(g2d);
        }

        faseConstrucao.desenharExplosoes(g2d);

        if (!player1.isMorreuNoRound()) {
            player1.desenhar(g2d);
        }
        if (!player2.isMorreuNoRound()) {
            player2.desenhar(g2d);
        }

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));

        if (faseConstrucao.isAtiva()) {
            g2d.drawString(
                "CONSTRUCAO | P1: WASD + [N] confirmar | P2: Setas + [M] confirmar",
                40, 36
            );
            faseConstrucao.desenhar(g2d, pecasDoMapa);
        } else {
            g2d.drawString(
                "Round: " + pergaminho.getRoundAtual() +
                "  |  P1: " + pergaminho.getPontosP1() + "/4" +
                "  |  P2: " + pergaminho.getPontosP2() + "/4" +
                "  |  P1: WASD  |  P2: Setas  |  ESC: Sair",
                40, 36
            );
        }

        pergaminho.desenhar(g2d);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (naTelaInicial || telaVitoria) {
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

        if (faseConstrucao.temExplosaoAtiva()) {
            faseConstrucao.atualizarExplosoes();
        }

        for (ElementoJogo peca : pecasDoMapa) {
            if (peca instanceof Armadilha armadilha) {
                armadilha.atualizar(pecasDoMapa);
            } else if (peca instanceof Plataforma plataforma) {
                plataforma.atualizar();
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

        if (telaVitoria) {
            if (tecla == KeyEvent.VK_ENTER || tecla == KeyEvent.VK_SPACE) {
                reiniciarPartidaCompleta();
            }
            return;
        }

        if (pergaminho.isAberto()) {
            if (tecla == KeyEvent.VK_SPACE || tecla == KeyEvent.VK_ENTER) {
                if (pergaminho.partidaFinalizada()) {
                    abrirTelaVitoria();
                } else {
                    pergaminho.avancarRound();
                    player1.resetarParaNovoRound(P1_X_INICIAL);
                    player2.resetarParaNovoRound(P2_X_INICIAL);
                    resetarPlataformasDoRound();
                    faseConstrucao.iniciar();
                }
            }
            return;
        }

        if (faseConstrucao.isAtiva()) {
            if (tecla == KeyEvent.VK_W) faseConstrucao.acaoP1("CIMA", pecasDoMapa);
            if (tecla == KeyEvent.VK_S) faseConstrucao.acaoP1("BAIXO", pecasDoMapa);
            if (tecla == KeyEvent.VK_A) faseConstrucao.acaoP1("ESQ", pecasDoMapa);
            if (tecla == KeyEvent.VK_D) faseConstrucao.acaoP1("DIR", pecasDoMapa);
            if (tecla == KeyEvent.VK_N) faseConstrucao.acaoP1("CONFIRMAR", pecasDoMapa);

            if (tecla == KeyEvent.VK_UP)    faseConstrucao.acaoP2("CIMA", pecasDoMapa);
            if (tecla == KeyEvent.VK_DOWN)  faseConstrucao.acaoP2("BAIXO", pecasDoMapa);
            if (tecla == KeyEvent.VK_LEFT)  faseConstrucao.acaoP2("ESQ", pecasDoMapa);
            if (tecla == KeyEvent.VK_RIGHT) faseConstrucao.acaoP2("DIR", pecasDoMapa);
            if (tecla == KeyEvent.VK_M)     faseConstrucao.acaoP2("CONFIRMAR", pecasDoMapa);
            return;
        }

        if (tecla == KeyEvent.VK_A) p1Esquerda = true;
        if (tecla == KeyEvent.VK_D) p1Direita = true;
        if (tecla == KeyEvent.VK_W) player1.solicitarPulo();

        if (tecla == KeyEvent.VK_LEFT)  p2Esquerda = true;
        if (tecla == KeyEvent.VK_RIGHT) p2Direita = true;
        if (tecla == KeyEvent.VK_UP)    player2.solicitarPulo();
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
        int mx = (int) (e.getX() / getEscalaX());
        int my = (int) (e.getY() / getEscalaY());

        if (naTelaInicial && botaoJogar.contains(mx, my)) {
            iniciarPartida();
        } else if (telaVitoria && botaoJogarNovamente.contains(mx, my)) {
            reiniciarPartidaCompleta();
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
        moldura.setExtendedState(JFrame.MAXIMIZED_BOTH);
        moldura.setVisible(true);
    }
}
