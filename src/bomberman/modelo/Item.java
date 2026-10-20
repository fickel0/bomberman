package bomberman.modelo;

public abstract class Item extends Casa {
    private static final long serialVersionUID = 1L;

    abstract void aplicar(Jogo jogo);

    @Override
    void pisar(Jogo jogo, int x, int y) {
        aplicar(jogo);
        jogo.getTabuleiro().substituir(x, y, new Chao());
    }

    @Override
    boolean bloqueia() {
        return false;
    }
}
