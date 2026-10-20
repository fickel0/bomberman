package bomberman.modelo;

public class ItemRaio extends Item {
    private static final long serialVersionUID = 1L;

    @Override
    void aplicar(Jogo jogo) {
        jogo.getJogador().aumentarRaio();
        jogo.registrarEvento(Jogo.Evento.ITEM_RAIO);
    }
}
