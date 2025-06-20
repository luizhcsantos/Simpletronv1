package org.simpletronv1.logic;

import java.util.Arrays;


/**
 * A classe SimpletronLogica encapsula a lógica central do simulador Simpletron.
 * Ela gerencia a memória, os registradores e a execução de instruções SML (Simpletron Machine Language).
 * Esta classe é projetada para funcionar de forma independente da interface do usuário,
 * permitindo que a simulação seja controlada e seu estado consultado.
 */

public class SimpletronLogic {

    // --- Constantes de Código de Operação ---

    public static final int READ = 10, WRITE = 11, LOAD = 20, STORE = 21;
    public static final int ADD = 30, SUBTRACT = 31, DIVIDE = 32, MULTIPLY = 33;
    public static final int BRANCH = 40, BRANCHNEG = 41, BRANCHZERO = 42, HALT = 43;

    // --- Registradores e Memória ---
    private final int[] memory;
    private String[] comments;
    private int accumulator;
    private int insctructionCounter;
    private int instructionRegister;
    private int operationCode;
    private int operand;


    public SimpletronLogic() {
        this.memory = new int[100];
        comments = new String[100];
        reiniciar();
    }


    /**
     * Carrega um programa na memória do Simpletron a partir de um array de strings.
     * Valida cada instrução quanto ao formato numérico e ao intervalo permitido.
     * Se um erro for encontrado, o carregamento é interrompido e uma mensagem de erro é retornada.
     *
     * @param linhasDoPrograma um array de strings, onde cada string representa uma instrução
     *                         ou uma linha em branco. As instruções devem ser inteiros
     *                         no intervalo de [-9999, 9999].
     * @return uma string com a mensagem de erro se o programa for inválido (ex: tamanho excedido,
     * instrução não numérica, valor fora do intervalo). Retorna null se o programa
     * for carregado com sucesso.
     */

    public String carregarPrograma(String[] linhasDoPrograma) {
        reiniciar(); // Garante que a máquina esteja limpa antes de carregar

        if (linhasDoPrograma.length > memory.length) {
            return "Erro: o programa é muito grande! Máximo de 100 instruções permitidas.";
        }

        for (int i = 0; i < linhasDoPrograma.length; i++) {
            String linha = linhasDoPrograma[i].trim();
            String parteInstrucao = linha;
            String parteComentario = "";

            if (linha.contains("//")) {
                int commentIndex = linha.indexOf("//");
                parteInstrucao = linha.substring(0, commentIndex);
                parteComentario = linha.substring(commentIndex + 2); // Pega o texto após "//"
            }

            // Armazena o comentário
            comments[i] = parteComentario.trim();

            String instrucaoStr = parteInstrucao.trim();
            if (instrucaoStr.isEmpty()) { // Ignora linhas em branco


                continue;
            }

            try {
                int instrucao = Integer.parseInt(instrucaoStr);

                if (instrucao < -9999 || instrucao > 9999) {
                    return String.format("Erro na linha %d: A instrução '%s' está fora " +
                            "do intervalo permitido [-9999, 9999].", i + 1, linha);
                }
                memory[i] = instrucao;
            } catch (NumberFormatException e) {
                return String.format("Erro na linha %d: O texto '%s' " +
                        "não é uma instrução válida.", i + 1, linha);
            }
        }
        return null; // Nulo indica que o carregamento foi bem-sucedido
    }

    /**
     * Reinicia o estado da máquina Simpletron.
     * Zera a memória, o acumulador e todos os registradores.
     */
    public void reiniciar() {
        Arrays.fill(memory, 0);
        Arrays.fill(comments, "");
        accumulator = 0;
        insctructionCounter = 0;
        instructionRegister = 0;
        operationCode = 0;
        operand = 0;
    }

    /**
     * Executa um único passo (uma instrução) da simulação.
     * Decodifica e executa a instrução apontada pelo contadorDeInstrucao.
     *
     * @return O código da operação executada. Retorna um valor negativo em caso de erro
     * (ex: -1 para divisão por zero, -2 para código inválido, -3 para estouro).
     * Retorna a constante PARE (43) se a execução terminar.
     */


    public int executarPasso() {
        if (insctructionCounter >= memory.length) {
            return HALT; // Para se o contador sair dos limites da memória
        }

        // Busca e decodifica a instrução
        instructionRegister = memory[insctructionCounter];
        operationCode = instructionRegister / 100;
        operand = instructionRegister % 100;

        boolean houveDesvio = false;

        switch (operationCode) {
            case READ: // A GUI cuida da leitura.
                break;
            case WRITE: // A GUI cuida da escrita.
                break;
            case LOAD:
                accumulator = memory[operand];
                break;
            case STORE:
                memory[operand] = accumulator;
                break;
            case ADD:
                accumulator += memory[operand];
                break;
            case SUBTRACT:
                accumulator -= memory[operand];
                break;
            case DIVIDE:
                if (memory[operand] == 0) return -1; // Sinal de erro: Divisão por zero
                accumulator /= memory[operand];
                break;
            case MULTIPLY:
                accumulator *= memory[operand];
                break;
            case BRANCH:
                insctructionCounter = operand;
                houveDesvio = true;
                break;
            case BRANCHNEG:
                if (accumulator < 0) {
                    insctructionCounter = operand;
                    houveDesvio = true;
                }
                break;
            case BRANCHZERO:
                if (accumulator == 0) {
                    insctructionCounter = operand;
                    houveDesvio = true;
                }
                break;
            case HALT:
                return HALT;
            default:
                return -2; // Sinal de erro: Código de operação inválido
        }

        // Incrementa o contador para a próxima instrução, a menos que um desvio tenha ocorrido
        if (!houveDesvio) {
            insctructionCounter++;
        }

        // Validação de estouro (overflow) do acumulador
        if (accumulator > 9999 || accumulator < -9999) {
            return -3; // Sinal de erro: Estouro do acumulador
        }

        return operationCode;
    }


    // Métodos "get" para a GUI poder ler os estados
    public int getAccumulator() {
        return accumulator;
    }

    public int getInstructionCounter() {
        return insctructionCounter;
    }

    public int getInstructionRegister() {
        return instructionRegister;
    }

    public String getCommentAt(int location) {
        if (location >= 0 && location < comments.length) {
            return comments[location];
        }
        return "";
    }

    public int getOperationCode() {
        return operationCode;
    }

    public int getOperand() {
        return operand;
    }

    public int[] getMemory() {
        return memory;
    }

    public int getMemoryAt(int location) {
        return memory[location];
    }

    public void setMemoryAt(int location, int value) {
        memory[location] = value;
    }
}
