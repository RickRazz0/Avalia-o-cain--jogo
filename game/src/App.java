import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics; // Importamos o "pincel" do Java para desenhar formas na tela

public class App extends JPanel {

    // Construtor da tela
    public App() {
        this.setPreferredSize(new Dimension(800, 600));
        this.setBackground(Color.BLACK);
    }

    // POLIMORFISMO (@Override): Estamos sobrescrevendo o método original do JPanel
    // O Java chama este método automaticamente sempre que precisa desenhar a tela
    @Override
    protected void paintComponent(Graphics g) {
        // 1. Mantém o comportamento original de limpar o fundo e pintar de preto
        super.paintComponent(g);

        // 2. Escolhemos a cor da tinta do nosso "pincel" (g)
        g.setColor(Color.BLUE); // Vamos fazer um quadrado vermelho para destacar no preto

        // 3. Desenhamos um retângulo preenchido (fillRect)
        // Os 4 números significam: (posição X, posição Y, largura, altura)
        // Como a tela é 800x600 e o quadrado é 50x50, (375, 275) deixa ele bem no centro!
        g.fillRect(375, 275, 50, 50);
    }

    public static void main(String[] args) {
        JFrame moldura = new JFrame("Ultimate Chicken Horse - Versão Java");
        App telaPreta = new App();

        moldura.add(telaPreta);
        moldura.pack();
        moldura.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        moldura.setLocationRelativeTo(null);
        moldura.setVisible(true);
    }
}