package bomberman.modelo;

public abstract class Personagem extends Agente implements Destrutivel {
    private static final long serialVersionUID = 1L;

    Personagem(int x, int y) {
        super(x, y);
    }

    void mover(int dx, int dy) {
        x += dx;
        y += dy;
    }
}
