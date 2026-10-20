package bomberman.modelo;

public class InimigoSabotador extends Inimigo {
    private static final long serialVersionUID = 1L;

    InimigoSabotador(int x, int y) {
        super(x, y);
    }

    @Override
    void agir(Jogo jogo) {
        if (!seguirBomba(jogo) && !seguirJogador(jogo)) {
            moverAleatorio(jogo);
        }
    }

    private boolean seguirBomba(Jogo jogo) {
        Bomba bomba = bombaNaLinha(jogo);
        if (bomba == null) {
            return false;
        }
        int dx = Integer.signum(bomba.getX() - getX());
        int dy = Integer.signum(bomba.getY() - getY());
        if ((dx == 0 && dy == 0) || (dx != 0 && dy != 0)) {
            return false;
        }
        int alvoX = getX() + dx;
        int alvoY = getY() + dy;
        // desarma só quando está adjacente, chegar na casa da bomba é o que dispara
        if (alvoX == bomba.getX() && alvoY == bomba.getY()) {
            jogo.desarmarBomba(bomba);
            mover(dx, dy);
            return true;
        }
        if (!jogo.podeEntrar(alvoX, alvoY)) {
            return false;
        }
        mover(dx, dy);
        return true;
    }

    // escolhe a bomba mais próxima na mesma linha ou coluna
    private Bomba bombaNaLinha(Jogo jogo) {
        Bomba melhor = null;
        int melhorDistancia = Integer.MAX_VALUE;
        for (int[] direcao : DIRECOES) {
            int alvoX = getX();
            int alvoY = getY();
            int distancia = 0;
            while (true) {
                alvoX += direcao[0];
                alvoY += direcao[1];
                if (!jogo.getTabuleiro().dentroDosLimites(alvoX, alvoY)) {
                    break;
                }
                if (jogo.getTabuleiro().getCasa(alvoX, alvoY).bloqueia()) {
                    break;
                }
                distancia++;
                Bomba bomba = jogo.getBombaEm(alvoX, alvoY);
                if (bomba != null) {
                    if (distancia < melhorDistancia) {
                        melhorDistancia = distancia;
                        melhor = bomba;
                    }
                    break;
                }
            }
        }
        return melhor;
    }
}
