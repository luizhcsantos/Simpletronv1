package org.simpletronv1.logic;

import java.util.Arrays;

public class SimpletronLogic {
    public static final int READ = 10, WRITE = 11, LOAD = 20, STORE = 21;
    public static final int ADD = 30, SUBTRACT = 31, DIVIDE = 32, MULTIPLY = 33;
    public static final int BRANCH = 40, BRANCHNEG = 41, BRANCHZERO = 42, HALT = 43;

    private final int[] memory;
    private int accumulator;
    private int instructionCounter;
    private int instructionRegister;
    private int operationCode;
    private int operand;

    public SimpletronLogic() {
        this.memory = new int[100];
        reset();
    }

    /**
     * Loads a program into the Simpletron's memory.
     * Validates the input for acceptable instruction range and memory size, then
     * stores each valid instruction into memory. If errors are found during
     * loading, the process halts and an error message is returned.
     *
     * @param programLines an array of strings where each string represents an
     *                     instruction or blank line of the program to be loaded.
     *                     Instructions are expected to be integer values within
     *                     the range of [-9999, 9999].
     * @return a string containing an error message if the program is invalid,
     *         such as exceeding memory size, non-numeric instructions, or
     *         instructions out of the valid range. Returns null if the program
     *         is successfully loaded into memory.
     */
    public String loadProgram(String[] programLines) {
        reset();

        if (programLines.length > memory.length) {
            return "Erro: o programa [e muito grande! Máximo de 100 instruções permitidas";
        }

        for (int i = 0; i < programLines.length; i++) {
            String line = programLines[i].trim();

            if (line.isEmpty()) {
                continue;
            }

            try {
                int instruction = Integer.parseInt(programLines[i]);

                if (instruction < -9999 || instruction > 9999) {
                    return String.format("Erro na linha %d: A instrução '%s' está fora " +
                            "do intervalo permitido [-9999, 9999].\",\n"
                            + i + 1, programLines[i]);
                }
                memory[i] = instruction;
            } catch (NumberFormatException e) {
                return String.format("Erro na linha %d: O texto '%s' " +
                                "não é uma instrução válida.",
                        i + 1, programLines[i]);
            }
        }
        return null;
    }

    public void reset() {
        Arrays.fill(memory, 0);
        accumulator = 0;
        instructionCounter = 0;
        instructionRegister = 0;
        operationCode = 0;
        operand = 0;
    }

    public int executeStep() {
        if (instructionCounter >= memory.length) {
            return HALT; // Para se o contador sair da memória
        }

        instructionRegister = memory[instructionCounter];
        operationCode = instructionRegister / 100;
        operand = instructionRegister % 100;

        // Incrementa o contador para a próxima instrução, exceto para desvios
        boolean branched = false;

        switch (operationCode) {
            case READ:
                // A GUI cuidará da leitura. Aqui apenas sinalizamos.
                break;
            case WRITE:
                // A GUI cuidará da escrita.
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
                if (memory[operand] == 0) return -1; // Sinal de erro
                accumulator /= memory[operand];
                break;
            case MULTIPLY:
                accumulator *= memory[operand];
                break;
            case BRANCH:
                instructionCounter = operand;
                branched = true;
                break;
            case BRANCHNEG:
                if (accumulator < 0) {
                    instructionCounter = operand;
                    branched = true;
                }
                break;
            case BRANCHZERO:
                if (accumulator == 0) {
                    instructionCounter = operand;
                    branched = true;
                }
                break;
            case HALT:
                return HALT;
            default:
                return -2; // Código de operação inválido
        }

        if (!branched) {
            instructionCounter++;
        }

        // Validação de estouro do acumulador
        if (accumulator > 9999 || accumulator < -9999) {
            return -3; // Sinal de erro de estouro
        }

        return operationCode;
    }

    // Métodos "get" para a GUI poder ler os estados
    public int getAccumulator() {
        return accumulator;
    }

    public int getInstructionCounter() {
        return instructionCounter;
    }

    public int getInstructionRegister() {
        return instructionRegister;
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
