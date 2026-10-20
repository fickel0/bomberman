package bomberman.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class Jogo implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Estado {
        JOGANDO,
        VITORIA,
        DERROTA
    }

    public enum Evento {
        BOMBA_PLANTADA,
        CAIXOTE_QUEBRADO,
        INIMIGO_DERROTADO,
        ITEM_BOMBA,
        ITEM_RAIO,
        BOMBA_DESARMADA,
        PERDEU_VIDA,
        DERROTA,
        VITORIA_BANDEIRA,
        VITORIA_INIMIGOS,
        DICA_USADA,
        SEM_DICAS,
        SEM_CAIXOTE_COM_ITEM
    }

    private static final int[][] INIMIGOS_POR_DIFICULDADE = {{5, 0, 0}, {4, 2, 0}, {4, 3, 2}};
    private static final int[][] DIRECOES = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private final Tabuleiro tabuleiro;
    private final Jogador jogador;
    private final int dificuldade;
    private final ArrayList<Bomba> bombas = new ArrayList<>();
    private final ArrayList<Fogo> fogos = new ArrayList<>();
    private final ArrayList<Inimigo> inimigos = new ArrayList<>();
    private Estado estado;
    private Ponto dica;
    private final ArrayList<Evento> eventos = new ArrayList<>();

    // montagem do jogo

    public Jogo(int dificuldade) {
        if (dificuldade < 0 || dificuldade >= INIMIGOS_POR_DIFICULDADE.length) {
            throw new IllegalArgumentException("dificuldade inválida: " + dificuldade);
        }

        this.dificuldade = dificuldade;
        this.tabuleiro = Tabuleiro.gerar();
        this.jogador = new Jogador(Tabuleiro.INICIO_X, Tabuleiro.INICIO_Y);
        criaInimigos();

        this.estado = Estado.JOGANDO;
    }

    private void criaInimigos() {
        ArrayList<Ponto> livres = new ArrayList<>();
        for (int y = 0; y < Tabuleiro.TAMANHO; y++) {
            for (int x = 0; x < Tabuleiro.TAMANHO; x++) {
                int distancia = Math.abs(x - Tabuleiro.INICIO_X) + Math.abs(y - Tabuleiro.INICIO_Y);
                // 5 casas de distância evitam inimigo nascendo colado no jogador
                if (distancia >= 5 && podeEntrar(x, y)) {
                    livres.add(new Ponto(x, y));
                }
            }
        }
        Collections.shuffle(livres);

        int proximo = 0;
        int[] quantidades = INIMIGOS_POR_DIFICULDADE[dificuldade];
        for (int q = 0; q < quantidades[0]; q++) {
            Ponto posicao = livres.get(proximo++);
            inimigos.add(new InimigoComum(posicao.getX(), posicao.getY()));
        }
        for (int q = 0; q < quantidades[1]; q++) {
            Ponto posicao = livres.get(proximo++);
            inimigos.add(new InimigoRastreador(posicao.getX(), posicao.getY()));
        }
        for (int q = 0; q < quantidades[2]; q++) {
            Ponto posicao = livres.get(proximo++);
            inimigos.add(new InimigoSabotador(posicao.getX(), posicao.getY()));
        }
    }

    // leitura do estado

    public Tabuleiro getTabuleiro() {
        return tabuleiro;
    }

    public Jogador getJogador() {
        return jogador;
    }

    // lista combinada só para quem desenha o jogo
    public ArrayList<Agente> getAgentes() {
        ArrayList<Agente> todos = new ArrayList<>();
        todos.add(jogador);
        todos.addAll(inimigos);
        todos.addAll(fogos);
        todos.addAll(bombas);
        return todos;
    }

    public int getDificuldade() {
        return dificuldade;
    }

    public Estado getEstado() {
        return estado;
    }

    public boolean estaJogando() {
        return estado == Estado.JOGANDO;
    }

    public ArrayList<Evento> getEventos() {
        return eventos;
    }

    public boolean temDicaEm(int x, int y) {
        return dica != null && dica.getX() == x && dica.getY() == y;
    }

    // ações do jogador

    public void moverJogador(int dx, int dy) throws MovimentoInvalidoException {
        iniciarAcao();
        int x = jogador.getX() + dx;
        int y = jogador.getY() + dy;
        if (!podeEntrar(x, y)) {
            throw new MovimentoInvalidoException("movimento inválido");
        }

        jogador.mover(dx, dy);
        tabuleiro.getCasa(x, y).pisar(this, x, y);
        if (!estaJogando()) {
            return;
        }
        concluirAcao();
    }

    public void plantarBomba() throws CasaOcupadaException, BombasEsgotadasException {
        // plantar não gasta turno, a bomba só começa a contar no próximo
        iniciarAcao();
        if (getBombaEm(jogador.getX(), jogador.getY()) != null) {
            throw new CasaOcupadaException("já tem uma bomba aqui");
        }
        if (bombas.size() >= jogador.getMaxBombas()) {
            throw new BombasEsgotadasException("limite de bombas atingido");
        }
        bombas.add(new Bomba(jogador.getX(), jogador.getY(), jogador.getRaio()));
        registrarEvento(Evento.BOMBA_PLANTADA);
    }

    public void esperar() {
        iniciarAcao();
        concluirAcao();
    }

    public void pedirDica() {
        iniciarAcao();
        if (jogador.getDicasDisponiveis() <= 0) {
            registrarEvento(Evento.SEM_DICAS);
            return;
        }
        Ponto alvo = tabuleiro.caixoteComItemMaisProximo(jogador.getX(), jogador.getY());
        if (alvo == null) {
            registrarEvento(Evento.SEM_CAIXOTE_COM_ITEM);
            return;
        }
        jogador.usarDica();
        dica = alvo;
        registrarEvento(Evento.DICA_USADA);
    }

    // toda ação começa zerando os eventos e a dica da rodada anterior
    private void iniciarAcao() {
        eventos.clear();
        dica = null;
    }

    // apaga o fogo do turno anterior antes de resolver esta ação
    private void concluirAcao() {
        fogos.clear();
        verificarColisaoComInimigos();
        if (estaJogando()) {
            passarTurno();
        }
    }

    private void passarTurno() {
        // bombas primeiro. queimar só quando todos andaram
        // cópia pq uma bomba pode explodir e remover outras da lista
        for (Bomba bomba : new ArrayList<>(bombas)) {
            bomba.agir(this);
        }
        if (!estaJogando()) {
            return;
        }

        for (Inimigo inimigo : inimigos) {
            inimigo.agir(this);
            if (!estaJogando()) {
                return;
            }
        }

        queimarInimigos();
        verificarColisaoComInimigos();
        if (!estaJogando()) {
            return;
        }
        if (inimigos.isEmpty()) {
            vencer(Evento.VITORIA_INIMIGOS);
        }
    }

    private void verificarColisaoComInimigos() {
        if (!estaJogando()) {
            return;
        }
        for (Inimigo inimigo : inimigos) {
            if (inimigo.getX() == jogador.getX() && inimigo.getY() == jogador.getY()) {
                jogador.destruir(this, jogador.getX(), jogador.getY());
                return;
            }
        }
    }

    private void queimarInimigos() {
        Set<Integer> fogo = new HashSet<>();
        for (Fogo atual : fogos) {
            fogo.add(atual.getY() * Tabuleiro.TAMANHO + atual.getX());
        }
        for (Inimigo inimigo : new ArrayList<>(inimigos)) {
            int posicao = inimigo.getY() * Tabuleiro.TAMANHO + inimigo.getX();
            if (fogo.contains(posicao)) {
                inimigo.destruir(this, inimigo.getX(), inimigo.getY());
            }
        }
        // indexar por posição evita O(n²)
    }

    void explodir(Bomba bomba) {
        boolean jogadorAtingido = propagarExplosao(bomba);
        // consequências ficam fora da recursão, senão a corrente tira várias vidas de uma vez
        queimarInimigos();
        if (jogadorAtingido) {
            jogador.destruir(this, jogador.getX(), jogador.getY());
        }
    }

    // propaga a explosão em cadeia e diz se o jogador foi atingido
    private boolean propagarExplosao(Bomba bomba) {
        // se a bomba já saiu da lista, outra explosão já cuidou dela
        if (!bombas.remove(bomba)) {
            return false;
        }
        boolean jogadorAtingido = gerarFogoEm(bomba.getX(), bomba.getY());

        for (int[] direcao : DIRECOES) {
            for (int passo = 1; passo <= bomba.getRaio(); passo++) {
                int x = bomba.getX() + direcao[0] * passo;
                int y = bomba.getY() + direcao[1] * passo;
                if (!tabuleiro.dentroDosLimites(x, y)) {
                    break;
                }
                Casa casa = tabuleiro.getCasa(x, y);
                if (casa.bloqueiaFogo()) {
                    break;
                }
                jogadorAtingido |= gerarFogoEm(x, y);
                if (casa instanceof Destrutivel) {
                    // destruir() serve pra caixote e inimigo
                    ((Destrutivel) casa).destruir(this, x, y);
                    break;
                }

                Bomba outra = getBombaEm(x, y);
                if (outra != null) {
                    jogadorAtingido |= propagarExplosao(outra);
                    break;
                }
            }
        }
        return jogadorAtingido;
    }

    // taca fogo na casa e diz se o jogador tava embaixo
    private boolean gerarFogoEm(int x, int y) {
        for (Fogo fogo : fogos) {
            if (fogo.getX() == x && fogo.getY() == y) {
                return jogador.getX() == x && jogador.getY() == y;
            }
        }
        fogos.add(new Fogo(x, y));
        return jogador.getX() == x && jogador.getY() == y;
    }

    void limparBombas() {
        bombas.clear();
    }

    void limparPerigosEm(int x, int y) {
        for (Fogo fogo : new ArrayList<>(fogos)) {
            if (fogo.getX() == x && fogo.getY() == y) {
                fogos.remove(fogo);
            }
        }
        for (Inimigo inimigo : new ArrayList<>(inimigos)) {
            if (inimigo.getX() == x && inimigo.getY() == y) {
                inimigos.remove(inimigo);
            }
        }
    }

    void registrarEvento(Evento evento) {
        eventos.add(evento);
    }

    void vencer(Evento evento) {
        estado = Estado.VITORIA;
        registrarEvento(evento);
    }

    void derrotar() {
        estado = Estado.DERROTA;
        registrarEvento(Evento.DERROTA);
    }

    void removerInimigo(Inimigo inimigo) {
        inimigos.remove(inimigo);
    }

    void desarmarBomba(Bomba bomba) {
        bombas.remove(bomba);
        registrarEvento(Evento.BOMBA_DESARMADA);
    }

    Bomba getBombaEm(int x, int y) {
        for (Bomba bomba : bombas) {
            if (bomba.getX() == x && bomba.getY() == y) {
                return bomba;
            }
        }
        return null;
    }

    // posição fora do mapa, casa sólida ou bomba impedem a entrada
    boolean podeEntrar(int x, int y) {
        return tabuleiro.dentroDosLimites(x, y)
                && !tabuleiro.getCasa(x, y).bloqueia()
                && getBombaEm(x, y) == null;
    }
}
