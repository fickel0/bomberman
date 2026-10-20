package bomberman.modelo;

import java.io.Serializable;

public abstract class Casa implements Serializable {
    private static final long serialVersionUID = 1L;

    abstract boolean bloqueia();

    // só a Parede barra o fogo
    boolean bloqueiaFogo() {
        return false;
    }

    // casa comum não reage a ser pisada
    void pisar(Jogo jogo, int x, int y) {
    }
}
