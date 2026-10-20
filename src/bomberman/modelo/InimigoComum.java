package bomberman.modelo;

public class InimigoComum extends Inimigo {
    private static final long serialVersionUID = 1L;

    InimigoComum(int x, int y) {
        super(x, y);
    }

    @Override
    void agir(Jogo jogo) {
        moverAleatorio(jogo);
    }
}
