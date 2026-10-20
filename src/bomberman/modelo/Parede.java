package bomberman.modelo;

public class Parede extends Casa {
    private static final long serialVersionUID = 1L;

    @Override
    boolean bloqueia() {
        return true;
    }

    @Override
    boolean bloqueiaFogo() {
        return true;
    }
}
