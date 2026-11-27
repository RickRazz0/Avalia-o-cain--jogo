# Lógica de back-end do jogo

## 1. Arquitetura e game loop

`App` é um `JPanel` do Swing. Seu construtor cria os dois jogadores, o placar, a fase de construção, a coleção `pecasDoMapa` e o timer:

```java
this.loopJogo = new Timer(16, this);
this.loopJogo.start();
```

O intervalo de 16 ms equivale aproximadamente a 60 atualizações por segundo (`1000 / 16`). A cada disparo, Swing chama `actionPerformed`. Esse método funciona como a atualização do jogo: primeiro identifica qual tela está ativa, depois atualiza animações/objetos, processa jogadores, verifica fim de round e chama `repaint()`.

`paintComponent(Graphics g)` é o ciclo de desenho. Ele não deve conter a física; apenas lê o estado já calculado e desenha. As telas são controladas por flags: `naTelaInicial`, `faseConstrucao.isAtiva()`, `pergaminho.isAberto()` e `telaVitoria`. A ordem garante que somente uma camada de tela seja apresentada por vez.

## 2. Física, movimento e colisão

O jogador possui velocidade horizontal fixa e velocidade vertical variável. Teclas de direção ativam flags; o loop usa essas flags para mover. O pulo é desacoplado: `solicitarPulo()` apenas registra o comando, e `consumirPulo()` só executa o salto se `noChao` tiver sido definido pelas colisões do mesmo frame.

```java
if (velocidadeY < 10) velocidadeY += 1;
setY(getY() + velocidadeY);
noChao = false;
```

Essa é a gravidade discreta: a cada frame a velocidade vertical aumenta até 10 e a posição Y recebe essa velocidade. Uma plataforma tocada por cima zera a velocidade, reposiciona o jogador no topo e marca `noChao`.

As hitboxes são retângulos AABB: `getLimites()` retorna `new Rectangle(x, y, largura, altura)`. AABB é uma técnica eficiente porque detecta sobreposição comparando retângulos alinhados aos eixos. `intersects` detecta contato; `intersection` informa a área comum e permite decidir se a separação deve ser vertical ou lateral. Plataformas resolvem posicionamento; armadilhas normalmente chamam `jogador.morrer()`.

O lança-chamas verifica seu jato fora do bloco sólido antes e depois da gravidade. A besta cria flechas móveis; uma flecha desaparece ao tocar em elemento do mapa ou jogador.

## 3. Construção e grade

Ao iniciar o round, `FaseConstrucao.iniciar()` cria um catálogo, usa `Collections.shuffle(catalogo)` e seleciona quatro cartas para o pergaminho 2x2. Cada `OpcaoPeca` guarda nome, dimensões, tipo e se é letal/bomba.

`ajustarNaGrade` converte a posição do cursor para múltiplos de 20 pixels. Assim todas as peças ficam encaixadas na grade 20x20. O método `posicaoValida` monta um retângulo candidato já com largura e altura rotacionadas e rejeita posições fora do palco, sobre áreas protegidas de nascimento/chegada ou sobre qualquer hitbox existente.

A bomba não vira uma peça persistente. Ao confirmar posições, `aplicarEfeitoPeca` cria uma área 3x3 e usa `removeIf` para remover armadilhas e plataformas destrutíveis cuja hitbox tenha qualquer interseção com a explosão. O chão-base (`CHAO`) é preservado.

## 4. Rotação

Cada cursor possui uma rotação de 0, 1, 2 ou 3, correspondente a 0°, 90°, 180° e 270°. `E` controla P1 e `Ç`/`;` controla P2. Ao colocar a peça, `ElementoJogo.girarHorario()` é chamado tantas vezes quanto necessário.

```java
rotacao = (rotacao + 1) % 4;
int dimensaoAnterior = largura;
largura = altura;
altura = dimensaoAnterior;
```

Trocar largura e altura faz a hitbox acompanhar peças retangulares. A besta também usa `Graphics2D.rotate(...)` no seu desenho; isso gira a representação ao redor do centro visual, e sua direção de disparo usa a mesma rotação.

## 5. Rounds e placar

Quando alguém chega à bandeira, ou os dois morrem, `App` abre `PergaminhoPlacar`. Ele registra os pontos, desenrola uma largura gradualmente e mostra o resultado do round; um jogador morto é identificado visualmente com `RIP`. Ao pressionar Enter/Espaço, o jogo verifica se alguém chegou a quatro vitórias. Caso contrário, avança o round, reposiciona jogadores, restaura estados temporários de plataformas e reabre a construção. Ao alcançar quatro pontos, a tela final informa o vencedor e permite reiniciar a partida completa.

## Fluxo resumido

1. Tela inicial recebe clique ou Enter.
2. Construção sorteia cartas; jogadores escolhem, movem, giram e confirmam peças.
3. Partida atualiza objetos e física a cada frame.
4. Chegada/mortes encerram o round e abrem o pergaminho.
5. O ciclo continua até quatro vitórias.
