import java.awt.Color;
import java.awt.Graphics;

public class Chegada extends ElementoJogo {

    public Chegada(int x, int y) {
        super(x, y, 35, 50);
    }

    @Override
    public void desenhar(Graphics g) {
        // Desenha o mastro branco e a bandeira verde igual ao seu desenho!
        g.setColor(Color.WHITE);
        g.fillRect(getX(), getY(), 5, getAltura());
        g.setColor(Color.GREEN);
        g.fillRect(getX() + 5, getY(), 30, 25);
    }

    // POLIMORFISMO: Chegou na bandeira = Vitória!
    @Override
    public void aoColidir(Jogador jogador) {
        System.out.println("VITÓRIA! Chegou na bandeira!");
        jogador.morrer(); // Reinicia na plataforma inicial para a próxima rodada
    }
}