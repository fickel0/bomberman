package bomberman.modelo;

public class BombasEsgotadasException extends Exception {
    private static final long serialVersionUID = 1L;

    public BombasEsgotadasException(String mensagem) {
        super(mensagem);
    }
}
