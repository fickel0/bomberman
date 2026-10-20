package bomberman.modelo;

import java.io.Serializable;

public abstract class Agente implements Serializable {
    private static final long serialVersionUID = 1L;

    int x;
    int y;

    Agente(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
