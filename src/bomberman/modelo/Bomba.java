package bomberman.modelo;

public class Bomba extends Agente {
    private static final long serialVersionUID = 1L;

    private static final int TURNOS_PARA_EXPLODIR = 3;

    private final int raio;
    private int turnosRestantes = TURNOS_PARA_EXPLODIR;

    Bomba(int x, int y, int raio) {
        super(x, y);
        this.raio = raio;
    }

    int getRaio() {
        return raio;
    }

    void agir(Jogo jogo) {
        if (--turnosRestantes <= 0) {
            jogo.explodir(this);
        }
    }
}
