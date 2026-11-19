import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class Plataforma extends ElementoJogo {

    private Color cor;

    public Plataforma(int x, int y, int largura, int altura, Color cor) {
        super(x, y, largura, altura);
        this.cor = cor;
    }

    @Override
    public void desenhar(Graphics g) {
        g.setColor(cor);
        g.fillRect(getX(), getY(), getLargura(), getAltura());
    }

    // POLIMORFISMO: Colisão sólida completa (cima, baixo, esquerda e direita)
    @Override
    public void aoColidir(Jogador jogador) {
        Rectangle rectJogador = jogador.getLimites();
        Rectangle rectPlataforma = this.getLimites();

        // Calcula o retângulo exato onde os dois corpos se sobrepuseram
        Rectangle intersecao = rectJogador.intersection(rectPlataforma);

        // Se a sobreposição for mais LARGA do que ALTA, a batida foi VERTICAL (cima ou baixo)
        if (intersecao.width >= intersecao.height) {
            if (jogador.getY() < this.getY() && jogador.getVelocidadeY() >= 0) {
                // Bateu por CIMA (pousou no degrau/chão)
                jogador.setY(this.getY() - jogador.getAltura());
                jogador.setVelocidadeY(0);
                jogador.setNoChao(true);
            } else if (jogador.getY() > this.getY() && jogador.getVelocidadeY() < 0) {
                // Bateu a cabeça por BAIXO do degrau
                jogador.setY(this.getY() + this.getAltura());
                jogador.setVelocidadeY(0);
            }
        } else {
            // Se a sobreposição for mais ALTA do que LARGA, a batida foi LATERAL (esquerda ou direita)
            if (jogador.getX() < this.getX()) {
                // Bateu na lateral ESQUERDA do degrau -> empurra para a esquerda
                jogador.setX(this.getX() - jogador.getLargura());
            } else {
                // Bateu na lateral DIREITA do degrau -> empurra para a direita
                jogador.setX(this.getX() + this.getLargura());
            }
        }
    }
}