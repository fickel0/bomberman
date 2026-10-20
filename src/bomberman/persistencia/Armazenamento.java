package bomberman.persistencia;

import bomberman.modelo.Jogo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Armazenamento {
    public static final String ARQUIVO = "save.dat";

    private Armazenamento() {}

    public static void salvar(Jogo jogo) throws ArmazenamentoException {
        try (ObjectOutputStream saida = new ObjectOutputStream(new FileOutputStream(ARQUIVO))) {
            saida.writeObject(jogo);
        } catch (IOException e) {
            throw new ArmazenamentoException("falha ao salvar o jogo", e);
        }
    }

    public static Jogo carregar() throws ArmazenamentoException {
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(ARQUIVO))) {
            return (Jogo) entrada.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new ArmazenamentoException("falha ao carregar o jogo", e);
        }
    }

    public static byte[] copiar(Jogo jogo) throws ArmazenamentoException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream saida = new ObjectOutputStream(bytes)) {
            saida.writeObject(jogo);
        } catch (IOException e) {
            throw new ArmazenamentoException("falha ao copiar o estado do jogo", e);
        }
        return bytes.toByteArray();
    }

    public static Jogo restaurar(byte[] dados) throws ArmazenamentoException {
        try (ObjectInputStream entrada = new ObjectInputStream(new ByteArrayInputStream(dados))) {
            return (Jogo) entrada.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new ArmazenamentoException("falha ao restaurar o estado do jogo", e);
        }
    }
}
