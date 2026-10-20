package bomberman.modelo;

public class InimigoRastreador extends Inimigo {
    private static final long serialVersionUID = 1L;

    InimigoRastreador(int x, int y) {
        super(x, y);
    }

    @Override
    void agir(Jogo jogo) {
        if (!seguirJogador(jogo)) {
            moverAleatorio(jogo);
        }
    }
}
