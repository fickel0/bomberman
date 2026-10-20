package bomberman;

import bomberman.modelo.*;
import bomberman.persistencia.*;

import java.util.ArrayList;
import java.util.Scanner;

public class EntradaSaida {

    // códigos ANSI pra colorir a tela
    private static final int PRETO = 0;
    private static final int AMARELO = 3;
    private static final int MAGENTA = 5;
    private static final int CINZA = 8;
    private static final int VERMELHO_CLARO = 9;
    private static final int VERDE_CLARO = 10;
    private static final int AMARELO_CLARO = 11;
    private static final int AZUL_CLARO = 12;
    private static final int MAGENTA_CLARO = 13;
    private static final int CIANO_CLARO = 14;
    private static final int BRANCO_CLARO = 15;
    private static final String ESC = "\033[";
    private static final String NEGRITO = ESC + "1m";
    private static final String RESET = ESC + "0m";
    private static final String LIMPAR = ESC + "H" + ESC + "2J";

    private static final String[] DIFICULDADES = {
        "fácil", "médio", "difícil",
    };

    private final Scanner scanner = new Scanner(System.in);
    private byte[] estadoInicial; // usado para reiniciar o jogo com mesmo mapa e inimigos
    private boolean debug;
    private String aviso;

    public void menuPrincipal() {
        while (true) {
            System.out.println(NEGRITO + "\nBOMBERMAN\n" + RESET);
            System.out.println("(1) novo jogo");
            System.out.println("(2) carregar jogo salvo");
            System.out.println("(3) sair");
            System.out.println("(d)ebug: " + NEGRITO + (debug ? "on" : "off") + RESET);
            System.out.print("? ");
            String texto = lerLinha();
            if (texto == null) {
                return;
            }
            if (texto.equalsIgnoreCase("d")) {
                debug = !debug;
                continue;
            }
            int opcao = paraOpcao(texto, 1, 3);
            if (opcao == 0) {
                System.out.println("opção inválida");
                continue;
            }
            if (opcao == 3) {
                return;
            }

            Jogo jogo;
            if (opcao == 2) {
                try {
                    jogo = Armazenamento.carregar();
                    System.out.println("jogo carregado de " + Armazenamento.ARQUIVO);
                } catch (ArmazenamentoException e) {
                    System.out.println("não deu pra carregar: " + e.getMessage());
                    continue;
                }
            } else {
                jogo = new Jogo(menuDificuldade());
            }
            jogar(jogo);
        }
    }

    private void jogar(Jogo jogo) {
        if (!guardarEstadoInicial(jogo)) {
            return;
        }
        while (true) {
            while (jogo.estaJogando()) {
                desenhar(jogo);
                char comando = lerComando();
                try {
                    switch (comando) {
                        case 'w':
                            jogo.moverJogador(0, -1);
                            break;
                        case 's':
                            jogo.moverJogador(0, 1);
                            break;
                        case 'a':
                            jogo.moverJogador(-1, 0);
                            break;
                        case 'd':
                            jogo.moverJogador(1, 0);
                            break;
                        case 'b':
                            jogo.plantarBomba();
                            break;
                        case 'i':
                            jogo.pedirDica();
                            break;
                        case 'e':
                            jogo.esperar();
                            break;
                        case 'q':
                            System.out.print("guardar antes de sair? (s/n) ");
                            String resposta = lerLinha();
                            if (resposta != null && resposta.toLowerCase().startsWith("s")) {
                                salvar(jogo);
                            }
                            return;
                        case 0:
                            break;
                        default:
                            aviso = "comando inválido";
                    }
                } catch (MovimentoInvalidoException
                        | CasaOcupadaException
                        | BombasEsgotadasException e) {
                    aviso = e.getMessage();
                }
            }

            desenhar(jogo);
            int opcao = menuFim(jogo);
            if (opcao == 1) {
                jogo = new Jogo(menuDificuldade());
                if (!guardarEstadoInicial(jogo)) {
                    return;
                }
            } else if (opcao == 2) {
                try {
                    jogo = Armazenamento.restaurar(estadoInicial);
                } catch (ArmazenamentoException e) {
                    System.out.println("erro: " + e.getMessage());
                }
            } else {
                return;
            }
        }
    }

    private void salvar(Jogo jogo) {
        try {
            Armazenamento.salvar(jogo);
            System.out.println("salvo em " + Armazenamento.ARQUIVO);
        } catch (ArmazenamentoException e) {
            System.out.println("erro: " + e.getMessage());
        }
    }

    private boolean guardarEstadoInicial(Jogo jogo) {
        try {
            estadoInicial = Armazenamento.copiar(jogo);
            return true;
        } catch (ArmazenamentoException e) {
            System.out.println("erro: " + e.getMessage());
            return false;
        }
    }

    private int menuDificuldade() {
        System.out.println(NEGRITO + "\nDIFICULDADE\n" + RESET);
        for (int i = 0; i < DIFICULDADES.length; i++) {
            System.out.println("(" + (i + 1) + ") " + DIFICULDADES[i]);
        }
        int opcao = lerOpcao(1, DIFICULDADES.length);
        return opcao < 0 ? 0 : opcao - 1;
    }

    private int menuFim(Jogo jogo) {
        System.out.println();
        System.out.println(
                NEGRITO
                        + (jogo.getEstado() == Jogo.Estado.VITORIA ? "VITÓRIA!" : "DERROTA!")
                        + RESET);
        System.out.println();
        System.out.println("(1) novo jogo");
        System.out.println("(2) reiniciar");
        System.out.println("(3) menu principal");
        return lerOpcao(1, 3);
    }

    private void desenhar(Jogo jogo) {
        StringBuilder desenho = new StringBuilder(LIMPAR);
        desenho.append(NEGRITO + "BOMBERMAN" + RESET).append('\n');
        desenho.append(DIFICULDADES[jogo.getDificuldade()] + (debug ? " - debug" : ""))
                .append("\n\n");

        Jogador jogador = jogo.getJogador();
        desenho.append(
                        "vidas: "
                                + jogador.getVidas()
                                + "\t"
                                + fundo(CIANO_CLARO)
                                + texto(PRETO)
                                + "()"
                                + RESET
                                + ": "
                                + jogador.getMaxBombas()
                                + "\t"
                                + fundo(CIANO_CLARO)
                                + texto(PRETO)
                                + "XX"
                                + RESET
                                + ": "
                                + jogador.getRaio()
                                + "\nscore: "
                                + jogador.getPontos()
                                + "\tdicas: "
                                + jogador.getDicasDisponiveis())
                .append('\n');

        Agente[][] grade = gradeDeAgentes(jogo);
        for (int y = 0; y < Tabuleiro.TAMANHO; y++) {
            for (int x = 0; x < Tabuleiro.TAMANHO; x++) {
                desenharCasa(jogo, x, y, grade[y][x], desenho);
            }
            desenho.append('\n');
        }

        // aviso e evento dividem a mesma linha, senão um empilha em cima do outro
        String status = aviso != null ? aviso : resumirEventos(jogo);
        aviso = null;
        desenho.append(status).append("\n\n");
        desenho.append("(wasd) (b)omba d(i)ca (e)sperar (q)sair").append('\n');
        System.out.print(desenho);
        System.out.flush();
    }

    // decide quem aparece na célula, passando uma vez só por todos os agentes
    private Agente[][] gradeDeAgentes(Jogo jogo) {
        Agente[][] grade = new Agente[Tabuleiro.TAMANHO][Tabuleiro.TAMANHO];
        for (Agente agente : jogo.getAgentes()) {
            Agente atual = grade[agente.getY()][agente.getX()];
            if (atual == null || prioridade(agente) < prioridade(atual)) {
                grade[agente.getY()][agente.getX()] = agente;
            }
        }
        return grade;
    }

    // jogador primeiro, depois inimigo, fogo e bomba
    private int prioridade(Agente agente) {
        if (agente instanceof Jogador) {
            return 0;
        }
        if (agente instanceof Inimigo) {
            return 1;
        }
        if (agente instanceof Fogo) {
            return 2;
        }
        return 3;
    }

    private static String fundo(int c) {
        return ESC + (c < 8 ? 40 + c : 100 + c - 8) + "m";
    }

    private static String texto(int c) {
        return ESC + (c < 8 ? 30 + c : 90 + c - 8) + "m";
    }

    private void desenharCasa(Jogo jogo, int x, int y, Agente agente, StringBuilder desenho) {
        Casa casa = jogo.getTabuleiro().getCasa(x, y);

        String cor;
        if (casa instanceof Parede) {
            cor = fundo(CINZA);
        } else if (jogo.temDicaEm(x, y)) {
            cor = fundo(CIANO_CLARO) + texto(PRETO);
        } else if (debug && casa instanceof Item) {
            cor = casa instanceof ItemSaida ? fundo(PRETO) : fundo(CIANO_CLARO) + texto(PRETO);
        } else if (debug && casa instanceof Caixote && ((Caixote) casa).getItem() != null) {
            cor = fundo(CIANO_CLARO) + texto(PRETO);
        } else if (agente instanceof Jogador) {
            cor = fundo(AZUL_CLARO) + texto(BRANCO_CLARO);
        } else if (agente instanceof InimigoRastreador) {
            cor = fundo(MAGENTA_CLARO) + texto(BRANCO_CLARO);
        } else if (agente instanceof InimigoSabotador) {
            cor = fundo(MAGENTA) + texto(BRANCO_CLARO);
        } else if (agente instanceof InimigoComum) {
            cor = fundo(VERMELHO_CLARO) + texto(BRANCO_CLARO);
        } else if (agente instanceof Fogo) {
            cor = fundo(AMARELO_CLARO);
        } else if (agente instanceof Bomba) {
            cor = fundo(PRETO) + texto(BRANCO_CLARO);
        } else if (casa instanceof Item) {
            cor = casa instanceof ItemSaida ? fundo(PRETO) : fundo(CIANO_CLARO) + texto(PRETO);
        } else if (casa instanceof Caixote) {
            cor = fundo(AMARELO);
        } else {
            cor = fundo(VERDE_CLARO);
        }

        String simbolo;
        if (agente instanceof Jogador) {
            simbolo = "°°";
        } else if (agente instanceof InimigoRastreador) {
            simbolo = "`´";
        } else if (agente instanceof InimigoSabotador) {
            simbolo = "¬¬";
        } else if (agente instanceof InimigoComum) {
            simbolo = "''";
        } else if (agente instanceof Fogo) {
            simbolo = "  ";
        } else if (agente instanceof Bomba) {
            simbolo = "()";
        } else if (casa instanceof ItemBomba) {
            simbolo = "()";
        } else if (casa instanceof ItemRaio) {
            simbolo = "XX";
        } else {
            simbolo = "  ";
        }

        desenho.append(cor).append(simbolo).append(RESET);
    }

    // vira texto o que aconteceu nesta ação, tudo numa linha só
    private String resumirEventos(Jogo jogo) {
        StringBuilder texto = new StringBuilder();
        ArrayList<Jogo.Evento> lista = jogo.getEventos();
        for (int i = 0; i < lista.size(); i++) {
            if (i > 0) {
                texto.append(", ");
            }
            switch (lista.get(i)) {
                case BOMBA_PLANTADA:
                    texto.append("bomba plantada");
                    break;
                case CAIXOTE_QUEBRADO:
                    texto.append("+1");
                    break;
                case INIMIGO_DERROTADO:
                    texto.append("+5");
                    break;
                case ITEM_BOMBA:
                    texto.append("+1 bomba");
                    break;
                case ITEM_RAIO:
                    texto.append("+1 raio");
                    break;
                case BOMBA_DESARMADA:
                    texto.append("um sabotador desarmou sua bomba!");
                    break;
                case PERDEU_VIDA:
                    texto.append("você perdeu uma vida!");
                    break;
                case DERROTA:
                    texto.append("você morreu...");
                    break;
                case VITORIA_BANDEIRA:
                    texto.append("você escapou!");
                    break;
                case VITORIA_INIMIGOS:
                    texto.append("todos os inimigos eliminados!");
                    break;
                case DICA_USADA:
                    texto.append("dica usada - item destacado no mapa");
                    break;
                case SEM_DICAS:
                    texto.append("sem dicas - junte 10 pontos");
                    break;
                case SEM_CAIXOTE_COM_ITEM:
                    texto.append("não há mais itens");
                    break;
                default:
                    break;
            }
        }
        return texto.toString();
    }

    private char lerComando() {
        System.out.print("? ");
        String texto = lerLinha();
        if (texto == null || texto.isEmpty()) {
            return 0;
        }
        return Character.toLowerCase(texto.charAt(0));
    }

    private int lerOpcao(int minimo, int maximo) {
        while (true) {
            System.out.print("? ");
            String texto = lerLinha();
            if (texto == null) {
                return -1;
            }
            int opcao = paraOpcao(texto, minimo, maximo);
            if (opcao != 0) {
                return opcao;
            }
            System.out.println("opção inválida");
        }
    }

    // devolve a opção se texto for um número entre minimo e maximo; 0 caso contrário
    private int paraOpcao(String texto, int minimo, int maximo) {
        try {
            int valor = Integer.parseInt(texto);
            return valor >= minimo && valor <= maximo ? valor : 0;
        } catch (NumberFormatException ignorada) {
            return 0;
        }
    }

    private String lerLinha() {
        if (!scanner.hasNextLine()) {
            return null;
        }
        return scanner.nextLine().trim();
    }
}
