package bomberman.modelo;

public class ItemBomba extends Item {
    private static final long serialVersionUID = 1L;

    @Override
    void aplicar(Jogo jogo) {
        jogo.getJogador().aumentarBombas();
        jogo.registrarEvento(Jogo.Evento.ITEM_BOMBA);
    }
}
