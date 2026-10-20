package bomberman.modelo;

public class CasaOcupadaException extends Exception {
    private static final long serialVersionUID = 1L;

    public CasaOcupadaException(String mensagem) {
        super(mensagem);
    }
}
