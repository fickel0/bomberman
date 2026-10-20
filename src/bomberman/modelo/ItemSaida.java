package bomberman.modelo;

public class ItemSaida extends Item {
    private static final long serialVersionUID = 1L;

    @Override
    void aplicar(Jogo jogo) {
        jogo.vencer(Jogo.Evento.VITORIA_BANDEIRA);
    }
}
