package org.simpletronv1.gui;

import org.simpletronv1.logic.SimpletronLogic;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The SimpletronGUI class represents the graphical user interface for the Simpletron simulator.
 * It is responsible for displaying the editor, controls, memory, and execution console, and
 * facilitating user interactions with the simulation.
 *
 * This class provides methods to initialize GUI components, handle user actions for loading
 * and executing programs, and communicate with the SimpletronLogic backend to simulate the
 * behavior of the Simpletron machine.
 */
public class SimpletronGUI extends JFrame {

    private final SimpletronLogic simpletron;
    private Timer executionTimer;

    // --- Componentes da GUI ---
    private JTextArea codeArea;
    private JButton loadFileButton;
    private JButton loadMemoryButton;
    private JButton runButton;
    private JButton resetButton;
    private JButton saveReportButton;

    private JTextField accumulatorField, instructionCounterField, instructionRegisterField, operationCodeField, operandField;
    private JTextField[] memoryFields;
    private JTextArea consoleArea;

    private JFileChooser fileChooser;

    public SimpletronGUI() {
        super("Simulador Simpletron");
        simpletron = new SimpletronLogic();

        initCompotents();
        initActions();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initCompotents() {
        setLayout(new BorderLayout(10, 10));

        // --- PAINEL ESQUERDO (CÓDIGO E CONTROLES) ---
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));

        codeArea = new JTextArea(10, 10);
        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane codeScrollPane = new JScrollPane(codeArea);
        codeScrollPane.setBorder(new TitledBorder("1. Editor de Código SML"));
        leftPanel.add(codeScrollPane, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.setBorder(new TitledBorder("2. Controles"));

        loadFileButton = new JButton("Carregar de Arquivo..."); // NOVO
        loadMemoryButton = new JButton("Carregar na Memória");
        runButton = new JButton("Executar");
        resetButton = new JButton("Resetar");
        saveReportButton = new JButton("Salvar Relatório...");

        runButton.setEnabled(false);
        saveReportButton.setEnabled(false);

        controlPanel.add(loadFileButton);
        controlPanel.add(loadMemoryButton);
        controlPanel.add(runButton);
        controlPanel.add(saveReportButton);
        controlPanel.add(resetButton);
        leftPanel.add(controlPanel, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);

        // --- PAINEL DIREITO (REGISTRADORES E MEMÓRIA) ---
        JPanel rightPanel = new JPanel(new BorderLayout(5, 10));
        JPanel registersPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        registersPanel.setBorder(new TitledBorder("Registradores"));
        accumulatorField = createRegisterField();
        instructionCounterField = createRegisterField();
        instructionRegisterField = createRegisterField();
        operationCodeField = createRegisterField();
        operandField = createRegisterField();
        registersPanel.add(new JLabel("Acumulador:"));
        registersPanel.add(accumulatorField);
        registersPanel.add(new JLabel("Contador de Instrução:"));
        registersPanel.add(instructionCounterField);
        registersPanel.add(new JLabel("Registrador de Instrução:"));
        registersPanel.add(instructionRegisterField);
        registersPanel.add(new JLabel("Código de Operação:"));
        registersPanel.add(operationCodeField);
        registersPanel.add(new JLabel("Operando:"));
        registersPanel.add(operandField);
        rightPanel.add(registersPanel, BorderLayout.NORTH);
        JPanel memoryPanel = new JPanel(new GridLayout(10, 10, 2, 2));
        memoryPanel.setBorder(new TitledBorder("Memória"));
        memoryFields = new JTextField[100];
        for (int i = 0; i < 100; i++) {
            memoryFields[i] = new JTextField(5);
            memoryFields[i].setEditable(false);
            memoryFields[i].setHorizontalAlignment(JTextField.CENTER);
            memoryFields[i].setFont(new Font("Monospaced", Font.BOLD, 12));
            memoryPanel.add(memoryFields[i]);
        }
        rightPanel.add(memoryPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.CENTER);

        // --- PAINEL INFERIOR (CONSOLE) ---
        // (Esta parte não muda)
        consoleArea = new JTextArea(8, 50);
        consoleArea.setEditable(false);
        consoleArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane consoleScrollPane = new JScrollPane(consoleArea);
        consoleScrollPane.setBorder(new TitledBorder("Console de Saída"));
        add(consoleScrollPane, BorderLayout.SOUTH);

        updateGUI();
    }


    private JTextField createRegisterField() {
        JTextField field = new JTextField();
        field.setEditable(false);
        field.setFont(new Font("Monospaced", Font.BOLD, 14));
        return field;
    }

    private void initActions() {
        // --- Configuração do seletor de arquivos
        fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Arquivos Simpletron (*.sml, *.txt)", "sml", "txt");
        fileChooser.setFileFilter(filter);

        loadFileButton.addActionListener(e -> {
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    String content = Files.readString(selectedFile.toPath(), StandardCharsets.UTF_8);
                    codeArea.setText(content);
                    consoleArea.setText("Aarquivo '" + selectedFile.getName() + "' carregado no editor.\n"
                            + "Clique em 'CArregar na Memória para continuar.\n");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Erro ao ler o arquivo: " + ex.getMessage(),
                            "Erro de arquivo",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // --- Ação do Botão CARREGAR NA MEMÓRIA ---
        loadMemoryButton.addActionListener(e -> {
            String[] lines = codeArea.getText().lines().toArray(String[]::new);
            String errorMessage = simpletron.loadProgram(lines);

            updateGUI();// Atualiza a GUI para mostrar o carregamento parcial (se houver)
            if (errorMessage == null) {
                // Sucesso!
                consoleArea.append("Programa carregado na memória com sucesso.\n");
                runButton.setEnabled(true); // Habilita a execução
            } else {
                // Falha! Exibe o erro para o usuário.
                JOptionPane.showMessageDialog(this, errorMessage, "Erro de Carregamento", JOptionPane.ERROR_MESSAGE);
                consoleArea.append("FALHA AO CARREGAR: " + errorMessage + "\n");
                runButton.setEnabled(false); // Garante que o botão de execução permaneça desabilitado
            }
            saveReportButton.setEnabled(false);
        });

        runButton.addActionListener(e -> {
            setControlsEnabled(false);
            consoleArea.setText(""); // Limpa o console antes de uma nova execução
            consoleArea.append("Iniciando execução...\n");
            executionTimer.start();
        });

        resetButton.addActionListener(e -> {
            if (executionTimer.isRunning()) {
                executionTimer.stop();
            }
            simpletron.reset();
            updateGUI();
            codeArea.setText("");
            consoleArea.setText("Simulador resetado.\n");
            setControlsEnabled(true);
            runButton.setEnabled(false);
            saveReportButton.setEnabled(false);
        });

        // --- Ação do Botão SALVAR RELATÓRIO ---
        saveReportButton.addActionListener(e -> {
            // Define um caminho fixo na raiz do projeto.
            String fileName = "relatorio_execucao.log.txt";
            File reportFile = new File(System.getProperty("user.dir"), fileName);

            try {
                // Abre o FileWriter em modo de "append" (o segundo parâmetro 'true').
                // O PrintWriter garantirá que a escrita seja feita de forma eficiente.
                // O try-with-resources garante que o arquivo será fechado corretamente.
                try (PrintWriter out = new PrintWriter(new FileWriter(reportFile, true))) {
                    out.println(generateExecutionReport());
                    out.println("\n"); // Adiciona uma linha extra para separar os relatórios

                    JOptionPane.showMessageDialog(this,
                            "Relatório adicionado com sucesso ao arquivo:\n" + reportFile.getAbsolutePath(),
                            "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao salvar o arquivo de relatório: " + ex.getMessage(),
                        "Erro de Arquivo",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        executionTimer = new Timer(50, e -> stepExecution());
    }

    private void stepExecution() {
        int opCode = simpletron.executeStep();
        if (opCode == SimpletronLogic.READ) {
            executionTimer.stop();
            String input = JOptionPane.showInputDialog(this, "Digite um valor para a instrução READ ");
            try {
                int value = Integer.parseInt(input);
                if (value < -9999 || value > 9999) {
                    erroFatal("Valor de entrada fora da faixa [-9999, 9999]");
                    return;
                }
                simpletron.setMemoryAt(simpletron.getOperand(), value);
                executionTimer.start();
            } catch (NumberFormatException ex) {
                erroFatal("Entrada Inválida. A execução foi abortada. ");
            }
        }
        else if(opCode == SimpletronLogic.WRITE) {
            consoleArea.append("Saída: " + simpletron.getMemoryAt(simpletron.getOperand()) + "\n");
        }else if (opCode == SimpletronLogic.HALT) {
            executionTimer.stop();
            consoleArea.append("\n***Execução finalizada normalmente. ***\n");
            setControlsEnabled(true);
            saveReportButton.setEnabled(true);
            runButton.setEnabled(false);
        }else if (opCode < 0) {
            switch (opCode) {
                case -1: erroFatal("Erro fatal: Tentativa de divisão por zero.");
                case -2: erroFatal("Erro fatal: Código de operação inválido.");
                case -3: erroFatal("Erro fatal: Estouro do acumulador. ");
            }
            return;
        }
        updateGUI();
    }

    private void erroFatal(String message) {
        executionTimer.stop();
        consoleArea.append("\n" + message + "\n");
        JOptionPane.showMessageDialog(this, message, "Erro Fatal", JOptionPane.ERROR_MESSAGE);
        setControlsEnabled(true);
        runButton.setEnabled(false);
    }

    private void setControlsEnabled(boolean enabled) {
        loadFileButton.setEnabled(enabled);
        loadMemoryButton.setEnabled(enabled);
        runButton.setEnabled(enabled);
        resetButton.setEnabled(enabled);
        codeArea.setEnabled(enabled);
    }

    private String generateExecutionReport() {
        StringBuilder report = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        report.append("==============================================\n");
        report.append("   RELATÓRIO DE EXECUÇÃO DO SIMPLETRON\n");
        report.append("==============================================\n\n");
        report.append("Data/Hora da Execução: ").append(dtf.format(LocalDateTime.now())).append("\n\n");

        report.append("--- CÓDIGO SML EXECUTADO ---\n");
        report.append(codeArea.getText()).append("\n\n");

        report.append("--- LOG DO CONSOLE (ENTRADA/SAÍDA) ---\n");
        report.append(consoleArea.getText()).append("\n");

        report.append("--- DUMP FINAL DA MÁQUINA ---\n");
        report.append(getDumpAsString()); // Usa um novo metodo auxiliar

        return report.toString();
    }

    /**
     * Versão do "dump" que retorna uma String em vez de imprimir no console.
     */
    private String getDumpAsString() {
        StringBuilder dump = new StringBuilder();

        dump.append("REGISTRADORES:\n");
        dump.append(String.format("acumulador:            %+05d\n", simpletron.getAccumulator()));
        dump.append(String.format("contadorDeInstrucao:   %02d\n", simpletron.getInstructionCounter()));
        dump.append(String.format("registradorDeInstrucao: %+05d\n", simpletron.getInstructionRegister()));
        dump.append(String.format("codigoDeOperacao:      %02d\n", simpletron.getOperationCode()));
        dump.append(String.format("operando:              %02d\n\n", simpletron.getOperand()));

        dump.append("MEMÓRIA:\n");
        // Cabeçalho da coluna
        dump.append("    ");
        for (int i = 0; i < 10; i++) {
            dump.append(String.format("   %d  ", i));
        }
        dump.append("\n");

        int[] memory = simpletron.getMemory();
        for (int i = 0; i < 100; i++) {
            if (i % 10 == 0) {
                dump.append(String.format("%2d  ", i));
            }
            dump.append(String.format("%+05d ", memory[i]));
            if ((i + 1) % 10 == 0) {
                dump.append("\n");
            }
        }
        return dump.toString();
    }

    private void updateGUI() {
        accumulatorField.setText(String.format("%+05d", simpletron.getAccumulator()));
        instructionCounterField.setText(String.format("%02d", simpletron.getInstructionCounter()));
        instructionRegisterField.setText(String.format("%+05d", simpletron.getInstructionRegister()));
        operationCodeField.setText(String.format("%02d", simpletron.getOperationCode()));
        operandField.setText(String.format("%02d", simpletron.getOperand()));
        int[] memory = simpletron.getMemory();
        for (int i=0; i<memory.length; i++) {
            memoryFields[i].setText(String.format("%+05d", memory[i]));
        }
        for (int i = 0; i < 100; i++) {
            memoryFields[i].setBackground(Color.WHITE);
        }
        int ic = simpletron.getInstructionCounter();
        if (ic >= 0 && ic < 100) {
            memoryFields[ic].setBackground(Color.CYAN);
        }
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(SimpletronGUI::new);
    }
}
