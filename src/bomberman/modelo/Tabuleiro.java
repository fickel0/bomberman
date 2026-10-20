package bomberman.modelo;

import java.io.Serializable;
import java.util.ArrayList;

public class Tabuleiro implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int TAMANHO = 21;
    static final int INICIO_X = 2;
    static final int INICIO_Y = 2;

    private static final double DENSIDADE_CAIXOTES = 0.30;
    private static final double CHANCE_POWERUP = 0.20;

    private final Casa[][] casas = new Casa[TAMANHO][TAMANHO];

    private Tabuleiro() {
        for (int y = 0; y < TAMANHO; y++) {
            for (int x = 0; x < TAMANHO; x++) {
                casas[y][x] = new Chao();
            }
        }
    }

    static Tabuleiro gerar() {
        Tabuleiro tabuleiro = new Tabuleiro();
        tabuleiro.criarParedes();
        tabuleiro.distribuirCaixotes();
        return tabuleiro;
    }

    private void criarParedes() {
        // blocos nos índices ímpares: padrão pontilhado, borda sempre livre
        for (int y = 1; y < TAMANHO - 1; y += 2) {
            for (int x = 1; x < TAMANHO - 1; x += 2) {
                casas[y][x] = new Parede();
            }
        }
    }

    private void distribuirCaixotes() {
        ArrayList<Ponto> livres = new ArrayList<>();
        for (int y = 0; y < TAMANHO; y++) {
            for (int x = 0; x < TAMANHO; x++) {
                boolean pertoDoInicio = Math.abs(x - INICIO_X) <= 1 && Math.abs(y - INICIO_Y) <= 1;
                if (!casas[y][x].bloqueia() && !pertoDoInicio) {
                    livres.add(new Ponto(x, y));
                }
            }
        }
        for (int i = livres.size() - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            Ponto troca = livres.get(i);
            livres.set(i, livres.get(j));
            livres.set(j, troca);
        }

        int quantidade = (int) (livres.size() * DENSIDADE_CAIXOTES);
        int indicePorta = (int) (Math.random() * quantidade);
        for (int i = 0; i < quantidade; i++) {
            Item conteudo = null;
            if (i == indicePorta) {
                conteudo = new ItemSaida();
            } else if (Math.random() < CHANCE_POWERUP) {
                conteudo = Math.random() < 0.5 ? new ItemBomba() : new ItemRaio();
            }
            Ponto posicao = livres.get(i);
            casas[posicao.getY()][posicao.getX()] = new Caixote(conteudo);
        }
    }

    boolean dentroDosLimites(int x, int y) {
        return x >= 0 && x < TAMANHO && y >= 0 && y < TAMANHO;
    }

    public Casa getCasa(int x, int y) {
        if (!dentroDosLimites(x, y)) {
            throw new IllegalArgumentException("posição fora do mapa: (" + x + ", " + y + ")");
        }
        return casas[y][x];
    }

    void substituir(int x, int y, Casa casa) {
        casas[y][x] = casa;
    }

    Ponto caixoteComItemMaisProximo(int x, int y) {
        Ponto melhor = null;
        int melhorDistancia = Integer.MAX_VALUE;
        for (int cy = 0; cy < TAMANHO; cy++) {
            for (int cx = 0; cx < TAMANHO; cx++) {
                Casa casa = casas[cy][cx];
                if (casa instanceof Caixote && ((Caixote) casa).getItem() != null) {
                    int distancia = Math.abs(cx - x) + Math.abs(cy - y);
                    if (distancia < melhorDistancia) {
                        melhorDistancia = distancia;
                        melhor = new Ponto(cx, cy);
                    }
                }
            }
        }
        return melhor;
    }
}
