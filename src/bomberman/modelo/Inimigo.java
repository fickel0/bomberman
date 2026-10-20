package bomberman.modelo;

public abstract class Inimigo extends Personagem {
    private static final long serialVersionUID = 1L;

    static final int PONTOS = 5;

    static final int[][] DIRECOES = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    Inimigo(int x, int y) {
        super(x, y);
    }

    abstract void agir(Jogo jogo);

    @Override
    public void destruir(Jogo jogo, int x, int y) {
        jogo.removerInimigo(this);
        jogo.getJogador().ganharPontos(PONTOS);
        jogo.registrarEvento(Jogo.Evento.INIMIGO_DERROTADO);
    }

    boolean seguirJogador(Jogo jogo) {
        Jogador jogador = jogo.getJogador();
        // signum devolve -1, 0 ou 1. representa a direção
        int dx = Integer.signum(jogador.getX() - getX());
        int dy = Integer.signum(jogador.getY() - getY());
        if (dx == 0 && dy == 0) {
            return false;
        }
        if (!temLinhaDeVisao(jogo, jogador.getX(), jogador.getY())) {
            return false;
        }
        if (!jogo.podeEntrar(getX() + dx, getY() + dy)) {
            return false;
        }
        mover(dx, dy);
        return true;
    }

    void moverAleatorio(Jogo jogo) {
        int[][] opcoes = new int[4][2];
        int total = 0;
        for (int[] direcao : DIRECOES) {
            if (jogo.podeEntrar(getX() + direcao[0], getY() + direcao[1])) {
                opcoes[total++] = direcao;
            }
        }
        if (total > 0) {
            int[] direcao = opcoes[(int) (Math.random() * total)];
            mover(direcao[0], direcao[1]);
        }
    }

    private boolean temLinhaDeVisao(Jogo jogo, int alvoX, int alvoY) {
        if (getX() != alvoX && getY() != alvoY) {
            return false;
        }
        int passoX = Integer.signum(alvoX - getX());
        int passoY = Integer.signum(alvoY - getY());
        int atualX = getX() + passoX;
        int atualY = getY() + passoY;
        while (atualX != alvoX || atualY != alvoY) {
            if (jogo.getTabuleiro().getCasa(atualX, atualY).bloqueia()) {
                return false;
            }
            atualX += passoX;
            atualY += passoY;
        }
        return true;
    }
}
