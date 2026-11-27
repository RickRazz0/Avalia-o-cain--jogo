package jogo.obstaculos;
import java.awt.Color;
import java.awt.Graphics;

import jogo.base.ElementoJogo;
import jogo.jogadores.Jogador;

// HERANÇA: Arco também estende ElementoJogo[cite: 4, 6]
// [HERANÇA]: objeto sólido legado que também pode participar do cenário.
public class Arco extends ElementoJogo {

    public Arco(int x, int y) {
        super(x, y, 40, 50);
    }

    // POLIMORFISMO: Desenha o formato de arco/besta virado para a esquerda (igual ao seu rascunho!)[cite: 4, 6]
    // [POLIMORFISMO]: desenho específico do arco.
    @Override
    public void desenhar(Graphics g) {
        g.setColor(Color.ORANGE);
        // Desenha o arco curvo (meia-lua virada para a esquerda)
        g.drawArc(getX(), getY(), 30, getAltura(), 90, 180);
        g.drawArc(getX() + 1, getY(), 30, getAltura(), 90, 180); // Deixa a linha mais grossa
        // Desenha a haste/mira horizontal apontando para a esquerda
        g.fillRect(getX() + 10, getY() + (getAltura() / 2) - 3, 25, 6);
    }

    // POLIMORFISMO: Se o jogador encostar no arco, também é bloqueado como um objeto sólido[cite: 4, 6]
    @Override
    public void aoColidir(Jogador jogador) {
        if (jogador.getX() < this.getX()) {
            jogador.setX(this.getX() - jogador.getLargura());
        }
    }
}
