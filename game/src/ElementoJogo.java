import java.awt.Graphics; // Precisamos do "pincel" para o método abstrato

// ABSTRAÇÃO: A palavra "abstract" proíbe criar um objeto genérico "ElementoJogo".
// Ela serve apenas como molde obrigatório para as classes filhas (Jogador, Plataforma, Espinho).
public abstract class ElementoJogo {

    // ENCAPSULAMENTO: "private" protege os dados para ninguém alterar a posição por acidente.
    private int x;
    private int y;
    private int largura;
    private int altura;

    // CONSTRUTOR: Define a posição e o tamanho inicial quando o objeto nasce
    public ElementoJogo(int x, int y, int largura, int altura) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
    }

    // MÉTODO ABSTRATO: Não tem código aqui (acaba com ponto e vírgula).
    // Obriga cada filho a explicar COMO ele quer ser desenhado na tela!
    public abstract void desenhar(Graphics g);

    // GETTERS E SETTERS (Encapsulamento - Aula 03):
    // Portas seguras para ler (get) ou alterar (set) a posição do elemento
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getLargura() {
        return largura;
    }

    public int getAltura() {
        return altura;
    }
}