# POO no jogo 2D Java

Este projeto é um jogo local para dois jogadores, feito com Java Swing. A arquitetura foi organizada para demonstrar abstração, encapsulamento, herança, polimorfismo e coleções em uma situação prática: objetos diferentes coexistem no mesmo mapa, mas cada um tem comportamento próprio.

## 1. Os quatro pilares

### Abstração

Abstrair é escolher as características que todos os objetos de um domínio têm em comum e representar somente o que interessa para o programa. No jogo, qualquer objeto visível possui posição, tamanho, desenho e reação à colisão. `ElementoJogo` concentra esse contrato, sem tentar decidir como uma serra ou uma bandeira devem funcionar.

```java
public abstract class ElementoJogo {
    public abstract void desenhar(Graphics g);
    public abstract void aoColidir(Jogador jogador);
}
```

Java não permite criar diretamente um `ElementoJogo`, pois ele é abstrato. A linguagem exige que uma subclasse forneça as versões concretas de `desenhar` e `aoColidir`.

### Encapsulamento

Encapsular é proteger o estado interno e permitir alterações por operações com significado. Os campos de posição e tamanho são privados; outras classes usam getters, setters ou métodos de negócio como `girarHorario`, `solicitarPulo` e `morrer`.

```java
private int x;
private int y;
private int largura;
private int altura;

public void girarHorario() {
    rotacao = (rotacao + 1) % 4;
    int dimensaoAnterior = largura;
    largura = altura;
    altura = dimensaoAnterior;
}
```

Ao chamar esse método, o restante do programa não precisa repetir a regra de inverter a hitbox. O objeto mantém sua própria consistência.

### Herança

Herança permite reutilizar a estrutura comum de uma classe-base. `Jogador`, `Plataforma`, `Armadilha`, `Chegada` e `Arco` usam `extends ElementoJogo`.

```java
public class Plataforma extends ElementoJogo {
    public Plataforma(int x, int y, int largura, int altura, Color cor, String tipo) {
        super(x, y, largura, altura);
        this.cor = cor;
        this.tipo = tipo;
    }
}
```

`super(...)` executa o construtor da classe-base e inicializa os dados universais. Depois, a plataforma acrescenta sua cor e seu tipo.

### Polimorfismo

Polimorfismo significa que uma referência do tipo comum pode apontar para objetos concretos diferentes. O loop trata todos como `ElementoJogo`, mas Java chama a sobrescrita correta em tempo de execução.

```java
for (ElementoJogo peca : pecasDoMapa) {
    peca.desenhar(g2d);
}
```

Se `peca` for uma `Plataforma`, executa `Plataforma.desenhar`; se for `Armadilha`, executa `Armadilha.desenhar`. O `App` não precisa de um grande conjunto de `if` para desenhar cada tipo.

## 2. Aplicação na arquitetura

`ElementoJogo` representa a camada comum do mundo. `Jogador` concentra a física e o estado individual. `Plataforma` resolve superfícies normais, escadas, molas e peças quebradiças. `Armadilha` reúne espinhos, serras, lança-chamas, flechas e besta. `Chegada` transforma uma colisão em vitória. A interface `FaseConstrucao` cria essas subclasses pelo catálogo, enquanto `App` mantém o loop e a lista do mapa.

Essa separação é útil porque acrescentar uma nova peça exige, em geral, criar ou configurar uma classe concreta, sem reescrever a engine inteira. A bomba, por exemplo, percorre a coleção e usa `instanceof` apenas para aplicar sua regra específica de destruição; o desenho e a colisão continuam polimórficos.

## 3. Coleções

Além dos pilares, `List<ElementoJogo>` e `ArrayList` são essenciais. A coleção aceita subclasses diferentes e cresce conforme as peças são colocadas ou flechas são disparadas.

```java
private final List<ElementoJogo> pecasDoMapa;
this.pecasDoMapa = new ArrayList<>();
```

Ao atualizar armadilhas, o jogo usa uma cópia temporária (`new ArrayList<>(pecasDoMapa)`) para permitir que a besta crie flechas sem causar `ConcurrentModificationException` durante a iteração.

## 4. Mapa de leitura do código

- `base/ElementoJogo.java`: contrato abstrato, hitbox e rotação.
- `jogadores/Jogador.java`: gravidade, movimento, pulo, morte e vitória.
- `obstaculos/Plataforma.java`: superfícies e animações de mola/quebra.
- `obstaculos/Armadilha.java`: perigos, besta/flechas e lança-chamas.
- `ui/FaseConstrucao.java`: cartas, grade, validação, bomba e preview.
- `ui/PergaminhoPlacar.java`: pontuação e tela de transição.
- `principal/App.java`: janela, entradas, Timer e loop principal.
