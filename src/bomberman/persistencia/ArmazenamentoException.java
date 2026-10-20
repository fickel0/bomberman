package bomberman.persistencia;

public class ArmazenamentoException extends Exception {
    private static final long serialVersionUID = 1L;

    public ArmazenamentoException(String mensagem) {
        super(mensagem);
    }

    public ArmazenamentoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
