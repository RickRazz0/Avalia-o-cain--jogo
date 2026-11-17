import java.awt.Color;
import java.awt.Graphics;

// HERANÇA: "extends ElementoJogo" faz o Jogador herdar x, y, largura, altura e os getters/setters!
public class Jogador extends ElementoJogo {

    // Construtor do Jogador: recebe apenas onde ele vai nascer (x e y)
    public Jogador(int x, int y) {
        // "super(...)" chama o construtor da classe mãe (ElementoJogo),
        // fixando o tamanho do nosso jogador em 50x50 pixels.
        super(x, y, 50, 50);
    }

    // POLIMORFISMO: Aqui cumprimos a obrigação da classe mãe e ensinamos
    // como o JOGADOR específico deve ser desenhado (um quadrado vermelho).
    @Override
    public void desenhar(Graphics g) {
        g.setColor(Color.RED);
        // Usamos getX(), getY(), getLargura() e getAltura() porque os atributos são privados na mãe!
        g.fillRect(getX(), getY(), getLargura(), getAltura());
    }
}