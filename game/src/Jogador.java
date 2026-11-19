import java.awt.Color;
import java.awt.Graphics;

// HERANÇA (Aula 05): Jogador herda posição, tamanho e limites de ElementoJogo
public class Jogador extends ElementoJogo {

    private int velocidadeX;
    private int velocidadeY; // Controla a queda (gravidade) e o impulso do pulo
    private boolean noChao;  // Diz se o jogador está pisando em alguma plataforma
    private Color cor;       // Permite criar o Player 1 (Vermelho) e Player 2 (Azul) da imagem!

    public Jogador(int x, int y, Color cor) {
        super(x, y, 30, 30);
        this.velocidadeX = 6;
        this.velocidadeY = 0;
        this.noChao = false;
        this.cor = cor;
    }

    // Atualiza a física do jogador a cada frame (60 vezes por segundo)
    public void aplicarGravidade() {
        // 1. Aplica a gravidade puxando para baixo (limite máximo de queda = 12)
        if (velocidadeY < 12) {
            velocidadeY += 1;
        }

        // 2. Move o jogador verticalmente conforme a velocidade atual
        setY(getY() + velocidadeY);

        // 3. Assume que está no ar até que alguma Plataforma prove o contrário na colisão
        noChao = false;

        // 4. Se cair abaixo do fundo da tela (y > 600), caiu no abismo e morreu!
        if (getY() > 600) {
            System.out.println("Caiu no abismo! Morreu!");
            morrer();
        }
    }

    public void pular() {
        // Só permite pular se estiver pisando em um terreno firme!
        if (noChao) {
            velocidadeY = -16; // Impulso negativo joga o personagem para cima
            noChao = false;
        }
    }

    public void moverEsquerda() { setX(getX() - velocidadeX); }
    public void moverDireita()  { setX(getX() + velocidadeX); }

    // Método chamado quando cai no buraco ou encosta em uma armadilha
    public void morrer() {
        setX(60);  // Volta para a plataforma inicial da esquerda
        setY(350);
        velocidadeY = 0;
    }

    // Getters e Setters para a Plataforma poder apoiar o jogador em cima dela
    public int getVelocidadeY() { return velocidadeY; }
    public void setVelocidadeY(int velocidadeY) { this.velocidadeY = velocidadeY; }
    public void setNoChao(boolean noChao) { this.noChao = noChao; }

    @Override
    public void desenhar(Graphics g) {
        g.setColor(cor);
        g.fillRect(getX(), getY(), getLargura(), getAltura());
    }

    @Override
    public void aoColidir(Jogador jogador) {}
}