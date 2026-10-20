package bomberman.modelo;

import java.io.Serializable;

final class Ponto implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int x;
    private final int y;

    Ponto(int x, int y) {
        this.x = x;
        this.y = y;
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }
}
