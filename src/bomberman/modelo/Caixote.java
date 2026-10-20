package bomberman.modelo;

public class Caixote extends Casa implements Destrutivel {
    private static final long serialVersionUID = 1L;

    static final int PONTOS = 1;

    private final Item item; // escondido - pode ser null

    public Caixote(Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    @Override
    boolean bloqueia() {
        return true;
    }

    @Override
    public void destruir(Jogo jogo, int x, int y) {
        jogo.getTabuleiro().substituir(x, y, item != null ? item : new Chao());
        jogo.getJogador().ganharPontos(PONTOS);
        jogo.registrarEvento(Jogo.Evento.CAIXOTE_QUEBRADO);
    }
}
