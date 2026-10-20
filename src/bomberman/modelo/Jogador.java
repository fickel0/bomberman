package bomberman.modelo;

public class Jogador extends Personagem {
    private static final long serialVersionUID = 1L;

    private static final int VIDAS_INICIAIS = 3;
    private static final int BOMBAS_INICIAIS = 1;
    private static final int RAIO_INICIAL = 1;
    private static final int PONTOS_POR_DICA = 10;

    private final int inicioX;
    private final int inicioY;
    private int vidas = VIDAS_INICIAIS;
    private int maxBombas = BOMBAS_INICIAIS;
    private int raio = RAIO_INICIAL;
    private int pontos;
    private int dicasUsadas;

    Jogador(int x, int y) {
        super(x, y);
        this.inicioX = x;
        this.inicioY = y;
    }

    public int getVidas() {
        return vidas;
    }

    public int getMaxBombas() {
        return maxBombas;
    }

    public int getRaio() {
        return raio;
    }

    public int getPontos() {
        return pontos;
    }

    public int getDicasDisponiveis() {
        // uma dica a cada 10 pontos, descontando as já usadas
        return pontos / PONTOS_POR_DICA - dicasUsadas;
    }

    void aumentarBombas() {
        maxBombas++;
    }

    void aumentarRaio() {
        raio++;
    }

    void ganharPontos(int pontos) {
        this.pontos += pontos;
    }

    void usarDica() {
        dicasUsadas++;
    }

    @Override
    public void destruir(Jogo jogo, int x, int y) {
        vidas--;
        jogo.limparBombas();
        if (vidas <= 0) {
            jogo.derrotar();
            return;
        }
        mover(inicioX - getX(), inicioY - getY());
        // limpa spawn pro jogador não morrer instantaneamente
        jogo.limparPerigosEm(inicioX, inicioY);
        jogo.registrarEvento(Jogo.Evento.PERDEU_VIDA);
    }
}
