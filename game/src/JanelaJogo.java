// Importamos as ferramentas gráficas nativas do Java (não precisa instalar nada!)
import javax.swing.JFrame; // Cria a janela do Windows
import javax.swing.JPanel; // Cria a área interna onde vamos desenhar
import java.awt.Color;     // Permite escolher cores (como o Preto)
import java.awt.Dimension; // Permite definir largura e altura da tela

// Nossa classe "JanelaJogo" HERDA (extends) tudo o que um JPanel sabe fazer!
// (Aqui você já está usando o pilar da HERANÇA da Aula 05!)
public class JanelaJogo extends JPanel {

    // Construtor: é executado automaticamente quando a tela é criada (Aula 03)
    public JanelaJogo() {
        // Define o tamanho da área de jogo: 800 pixels de largura por 600 de altura
        this.setPreferredSize(new Dimension(800, 600));
        
        // Pinta o fundo do nosso painel de PRETO (a "telinha preta" do ScreenSaver!)
        this.setBackground(Color.BLACK);
    }

    // Método principal (main): é por onde o Java começa a executar o programa
    public static void main(String[] args) {
        // 1. Cria a moldura da janela com o título do nosso jogo
        JFrame moldura = new JFrame("Ultimate Animal - Java");

        // 2. Cria o nosso painel preto (instanciando a classe que fizemos acima)
        JanelaJogo telaPreta = new JanelaJogo();

        // 3. Coloca a tela preta dentro da moldura
        moldura.add(telaPreta);

        // 4. Ajusta o tamanho da moldura exatamente para caber a tela de 800x600
        moldura.pack();

        // 5. Faz o programa encerrar de verdade quando você clicar no "X" da janela
        moldura.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 6. Centraliza a janela no meio do seu monitor
        moldura.setLocationRelativeTo(null);

        // 7. Torna a janela visível na tela (sem isso ela fica escondida na memória!)
        moldura.setVisible(true);
    }
}