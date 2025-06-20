package org.simpletronv1.gui;

import org.simpletronv1.logic.SimpletronLogic;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
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
 * A classe SimpletronGUI fornece uma interface gráfica para a simulação e controle da máquina Simpletron.
 * Esta classe permite carregar programas, executar instruções, e visualizar
 * o estado interno do Simpletron, incluindo memória, registradores e console.
 *
 * A interface é construída com base na biblioteca Swing, e oferece controle
 * de velocidade de execução, manipulação de arquivos e geração de relatórios da execução.
 *
 * Superclasse: javax.swing.JFrame
 *
 * Campos:
 * - simpletron: Um objeto que encapsula a lógica do Simpletron (uma instância de SimpletronLogic).
 * - temporizadorExecucao: Controla a execução contínua passo a passo com base no controle de tempo.
 * - areaDecodigo: Componente de texto para visualização ou edição do código SML.
 * - botaoCarregarArquivo: Botão para carregar um programa SML de um arquivo de texto/sml.
 * - botaoCarregarMemoria: Botão para carregar o código do campo de texto para a memória do Simpletron.
 * - botaoExecutar: Botõa para iniciar ou continuar a execução das instruções.
 * - botaoResetar: Botão para reinicializar o Simpletron para seu estado inicial.
 * - botaoSalvarRelatorio: Botão para salvar um relatório de execução em um arquivo externo.
 * - speedSlider: Controle deslizante para ajustar a velocidade de execução contínua.
 * - campoAcumulador: Campo de texto para exibir o valor do acumulador.
 * - campoContadorInstrucao: Campo de texto para exibir o índice da próxima instrução.
 * - campoRegistradorInstrucao: Campo de texto para exibir a instrução atual sendo executada.
 * - campoCodigoOperacao: Campo de texto para exibir o código de operação atual.
 * - campoOperando: Campo de texto para exibir o operando atual.
 * - camposMemoria: Campos de texto para exibição e edição das posições de memória do Simpletron.
 * - areaConsole: Componente de texto para exibição de mensagens e saídas do Simpletron.
 * - seletorArquivo: Componente que permite selecionar arquivos pelo sistema de diretórios.
 *
 * Construtores:
 * - SimpletronGUI(): Construtor que inicializa a interface gráfica e associa as ações aos componentes.
 *
 * Métodos:
 * - initComponents(): Configura os componentes gráficos, layouts e painéis da interface.
 * - criarCampoRegistrador(): Cria e retorna um campo de texto configurado para exibição de valores de registradores.
 * - initActions(): Associa eventos aos componentes da interface, como cliques de botões e ajustes do slider.
 * - executarPasso(): Executa uma única instrução do programa carregado na memória do Simpletron.
 * - erroFatal(String mensagem): Exibe uma mensagem de erro crítica e reinicia o Simpletron.
 * - definirControlesAtivos(boolean ativo): Habilita ou desabilita os componentes da interface.
 * - gerarRelatorioExecucao(): Gera e retorna um relatório textual detalhado sobre o estado atual do Simpletron.
 * - obterDumpComoString(): Retorna o estado atual da memória do Simpletron como uma string formatada.
 * - atualizarGUI(): Atualiza a interface refletindo o estado atual da memória, registradores e console.
 * - main(String[] args): Metodo principal que cria e exibe a interface da aplicação.
 */
public class SimpletronGUI extends JFrame {

    private final SimpletronLogic simpletron;
    private Timer temporizadorExecucao;

    // --- Componentes da GUI ---
    private JTextArea areaDecodigo;
    private JButton botaoCarregarArquivo;
    private JButton botaoCarregarMemoria;
    private JButton botaoExecutar;
    private JButton botaoResetar;
    private JButton botaoSalvarRelatorio;
    private JSlider speedSlider;

    private JTextField campoAcumulador, campoContadorInstrucao, campoRegistradorInstrucao, 
                      campoCodigoOperacao, campoOperando;
    private JTextField[] camposMemoria;
    //private JTextArea areaConsole;
    private JTextPane areaConsole;
    private Style estiloPadrao;
    private Style estiloSaida;
    private Style estiloErro;

    private JFileChooser seletorArquivo;

    public SimpletronGUI() {
        super("Simulador Simpletron");
        simpletron = new SimpletronLogic();

        initComponents();
        initActions();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 10));

        // PAINEL ESQUERDO (CÓDIGO E CONTROLES)
        JPanel painelEsquerdo = new JPanel(new BorderLayout(5, 5));

        areaDecodigo = new JTextArea(10, 10);
        areaDecodigo.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane painelRolagemCodigo = new JScrollPane(areaDecodigo);
        painelRolagemCodigo.setBorder(new TitledBorder("1. Editor de Código SML"));
        painelEsquerdo.add(painelRolagemCodigo, BorderLayout.CENTER);

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));

        JPanel painelControle = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        painelControle.setBorder(new TitledBorder("2. Controles de Execução"));

        botaoCarregarArquivo = new JButton("Carregar Arquivo...");
        botaoCarregarMemoria = new JButton("Carregar na Memória");
        botaoExecutar = new JButton("Executar");
        botaoResetar = new JButton("Reiniciar");
        botaoSalvarRelatorio = new JButton("Salvar Relatório...");

        botaoExecutar.setEnabled(false);
        botaoSalvarRelatorio.setEnabled(false);

        painelControle.add(botaoCarregarArquivo);
        painelControle.add(botaoCarregarMemoria);
        painelControle.add(botaoExecutar);
        painelControle.add(botaoSalvarRelatorio);
        painelControle.add(botaoResetar);

        JPanel painelSpeed = new JPanel(new BorderLayout());
        painelSpeed.setBorder(new TitledBorder("3. Controle de Velocidade (ms por passo)"));
        speedSlider = new JSlider(JSlider.HORIZONTAL, 20, 1000, 200); // Mínimo(rápido), Máximo(lento), Valor Inicial
        speedSlider.setMajorTickSpacing(200);
        speedSlider.setMinorTickSpacing(50);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        painelSpeed.add(new JLabel("Rápido"), BorderLayout.WEST);
        painelSpeed.add(speedSlider, BorderLayout.CENTER);
        painelSpeed.add(new JLabel("Lento"), BorderLayout.EAST);

        southPanel.add(painelControle);
        southPanel.add(painelSpeed); // Adiciona o painel de velocidade abaixo dos botões
        painelEsquerdo.add(southPanel, BorderLayout.SOUTH);
        //painelEsquerdo.add(painelControle, BorderLayout.SOUTH);

        add(painelEsquerdo, BorderLayout.WEST);

        // PAINEL DIREITO (REGISTRADORES E MEMÓRIA)
        JPanel painelDireito = new JPanel(new BorderLayout(5, 10));
        JPanel painelRegistradores = new JPanel(new GridLayout(5, 2, 5, 5));
        painelRegistradores.setBorder(new TitledBorder("Registradores"));
        
        campoAcumulador = criarCampoRegistrador();
        campoContadorInstrucao = criarCampoRegistrador();
        campoRegistradorInstrucao = criarCampoRegistrador();
        campoCodigoOperacao = criarCampoRegistrador();
        campoOperando = criarCampoRegistrador();

        painelRegistradores.add(new JLabel("Acumulador:"));
        painelRegistradores.add(campoAcumulador);
        painelRegistradores.add(new JLabel("Contador de Isntrução:"));
        painelRegistradores.add(campoContadorInstrucao);
        painelRegistradores.add(new JLabel("Registrador de Instrução:"));
        painelRegistradores.add(campoRegistradorInstrucao);
        painelRegistradores.add(new JLabel("Código de Operação:"));
        painelRegistradores.add(campoCodigoOperacao);
        painelRegistradores.add(new JLabel("Operando:"));
        painelRegistradores.add(campoOperando);

        painelDireito.add(painelRegistradores, BorderLayout.NORTH);

        JPanel painelMemoria = new JPanel(new GridLayout(10, 10, 2, 2));
        painelMemoria.setBorder(new TitledBorder("Memória"));
        camposMemoria = new JTextField[100];
        
        for (int i = 0; i < 100; i++) {
            camposMemoria[i] = new JTextField(5);
            camposMemoria[i].setEditable(false);
            camposMemoria[i].setHorizontalAlignment(JTextField.CENTER);
            camposMemoria[i].setFont(new Font("Monospaced", Font.BOLD, 12));
            painelMemoria.add(camposMemoria[i]);
        }
        
        painelDireito.add(painelMemoria, BorderLayout.CENTER);
        add(painelDireito, BorderLayout.CENTER);

        // PAINEL INFERIOR (CONSOLE)
        //areaConsole = new JTextArea(8, 50);
        areaConsole = new JTextPane();
        areaConsole.setEditable(false);
        areaConsole.setFont(new Font("Monospaced", Font.PLAIN, 12));

        StyledDocument doc = areaConsole.getStyledDocument();
        estiloPadrao = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        estiloSaida = doc.addStyle("EstiloSaida", estiloPadrao);
        StyleConstants.setForeground(estiloSaida, new Color(19, 122, 16)); // Cor azul
        StyleConstants.setBold(estiloSaida, true);
        StyleConstants.setFontSize(estiloSaida, 14);

        estiloErro = doc.addStyle("EstiloErro", estiloPadrao);
        StyleConstants.setForeground(estiloErro, new Color(168, 7, 7));
        StyleConstants.setBold(estiloErro, true);
        StyleConstants.setFontSize(estiloErro, 14);


        JScrollPane painelRolagemConsole = new JScrollPane(areaConsole);
        painelRolagemConsole.setPreferredSize(new Dimension(0, 150));
        painelRolagemConsole.setBorder(new TitledBorder("Console de Saída"));
        add(painelRolagemConsole, BorderLayout.SOUTH);

        atualizarGUI();
    }

    private JTextField criarCampoRegistrador() {
        JTextField campo = new JTextField();
        campo.setEditable(false);
        campo.setFont(new Font("Monospaced", Font.BOLD, 14));
        return campo;
    }

    private void initActions() {
        seletorArquivo = new JFileChooser(System.getProperty("user.dir"));
        FileNameExtensionFilter filtro = new FileNameExtensionFilter(
            "Arquivos Simpletron (*.sml, *.txt)", "sml", "txt");
        seletorArquivo.setFileFilter(filtro);

        botaoCarregarArquivo.addActionListener(e -> {
            int resultado = seletorArquivo.showOpenDialog(this);
            if (resultado == JFileChooser.APPROVE_OPTION) {
                File arquivoSelecionado = seletorArquivo.getSelectedFile();
                try {
                    String conteudo = Files.readString(arquivoSelecionado.toPath(), StandardCharsets.UTF_8);
                    areaDecodigo.setText(conteudo);
                    areaConsole.setText("Arquivo '" + arquivoSelecionado.getName() + "' carregado no editor.\n"
                            + "Clique em 'Carregar na Memória' para continuar.\n");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Erro ao ler o arquivo: " + ex.getMessage(),
                            "Erro de arquivo",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        botaoCarregarMemoria.addActionListener(e -> {
            String[] linhas = areaDecodigo.getText().lines().toArray(String[]::new);
            String mensagemErro = simpletron.carregarPrograma(linhas);

            atualizarGUI();
            // Não houve problemas ao carregar o programa na memória
            if (mensagemErro == null) {
                adicionarAoPainel("Programa carregado na memória com sucesso.\n", estiloPadrao);
                //areaConsole.append("Programa carregado na memória com sucesso.\n");
                botaoExecutar.setEnabled(true);
                botaoSalvarRelatorio.setEnabled(false);

                StringBuilder formattedCode = new StringBuilder();
                for (int i=0; i<linhas.length; i++) {
                    String linhaOriginal = linhas[i];
                    String parteIsntrucao = linhaOriginal;

                    if (linhaOriginal.contains("//")) {
                        parteIsntrucao = linhaOriginal.substring(0, linhaOriginal.indexOf("//"));
                    }

                    if (parteIsntrucao.trim().isEmpty()) {
                        formattedCode.append(linhaOriginal).append("\n");
                    } else {
                        int isntrucao = simpletron.getMemoryAt(i);
                        String comentario = simpletron.getCommentAt(i);

                        String formattedLine = String.format("%+05d", isntrucao);
                        if (comentario != null && !comentario.isEmpty()) {
                            formattedLine += String.format("  // %s", comentario);
                        }
                        formattedCode.append(formattedLine).append("\n");
                    }
                }
                areaDecodigo.setText(formattedCode.toString());
            } else {
                JOptionPane.showMessageDialog(this, mensagemErro, "Erro de Carregamento", JOptionPane.ERROR_MESSAGE);
                adicionarAoPainel("FALHA AO CARREGAR: " + mensagemErro + "\n", estiloErro);
                //areaConsole.append("FALHA AO CARREGAR: " + mensagemErro + "\n");
                botaoExecutar.setEnabled(false);
            }
            botaoSalvarRelatorio.setEnabled(false);
        });

        botaoExecutar.addActionListener(e -> {
            definirControlesAtivos(false);
            areaConsole.setText("");
            adicionarAoPainel("Iniciando execução...\n", estiloPadrao);
            //areaConsole.append("Iniciando execução...\n");
            temporizadorExecucao.start();
        });

        botaoResetar.addActionListener(e -> {
            if (temporizadorExecucao.isRunning()) {
                temporizadorExecucao.stop();
            }
            simpletron.reiniciar();
            atualizarGUI();
            areaDecodigo.setText("");
            areaConsole.setText("Simulador resetado.\n");
            definirControlesAtivos(true);
            botaoExecutar.setEnabled(false);
            botaoSalvarRelatorio.setEnabled(false);
        });

        botaoSalvarRelatorio.addActionListener(e -> {
            String nomeArquivo = "relatorio_execucao.log.txt";
            File arquivoRelatorio = new File(System.getProperty("user.dir"), nomeArquivo);

            try (PrintWriter saida = new PrintWriter(new FileWriter(arquivoRelatorio, true))) {
                saida.println(gerarRelatorioExecucao());
                saida.println("\n");

                JOptionPane.showMessageDialog(this,
                        "Relatório adicionado com sucesso ao arquivo:\n" + arquivoRelatorio.getAbsolutePath(),
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao salvar o arquivo de relatorio: " + ex.getMessage(),
                        "Erro de Arquivo",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        speedSlider.addChangeListener(e -> {
            if (temporizadorExecucao != null) {
                temporizadorExecucao.setDelay(speedSlider.getValue());
            }
        });

        temporizadorExecucao = new Timer(speedSlider.getValue(), e -> executarPasso());
        //temporizadorExecucao = new Timer(500, e -> executarPasso());


    }

    private void executarPasso() {
        int codigoOp = simpletron.executarPasso();
        if (codigoOp == SimpletronLogic.READ) {
            temporizadorExecucao.stop();
            String entrada = JOptionPane.showInputDialog(this, "Digite um valor para a instrução READ ");
            try {
                int valor = Integer.parseInt(entrada);
                // Validação de estouro do acumulador
                if (valor < -9999 || valor > 9999) {
                    erroFatal("Valor de entrada fora da faixa [-9999, 9999]");
                    return;
                }
                simpletron.setMemoryAt(simpletron.getOperand(), valor);
                temporizadorExecucao.start();
            } catch (NumberFormatException ex) {
                erroFatal("Entrada Inválida. A execução foi abortada. ");
            }
        }
        else if(codigoOp == SimpletronLogic.WRITE) {
            String mensagemDeSaida = "Saída: " + simpletron.getMemoryAt(simpletron.getOperand()) + "\n";
            adicionarAoPainel(mensagemDeSaida, estiloSaida);
            //adicionarAoPainel("Saída: ", estiloPadrao);
            //adicionarAoPainel(String.valueOf(simpletron.getMemoryAt(simpletron.getOperand())) + "\n", estiloSaida);
            //areaConsole.append("Saída: " + simpletron.getMemoryAt(simpletron.getOperand()) + "\n");
        }
        else if (codigoOp == SimpletronLogic.HALT) {
            temporizadorExecucao.stop();
            adicionarAoPainel("\n***Execução finalizada normalmente. ***\n", estiloPadrao);
            //areaConsole.append("\n***Execução finalizada normalmente. ***\n");
            definirControlesAtivos(true);
            botaoSalvarRelatorio.setEnabled(true);
            botaoExecutar.setEnabled(false);
        }
        else if (codigoOp < 0) {
            switch (codigoOp) {
                case -1:
                    erroFatal("Erro fatal: Tentativa de divisão por zero.");
                    break;
                case -2:
                    erroFatal("Erro fatal: Código de operação inválido.");
                    break;
                case -3:
                    erroFatal("Erro fatal: Estouro do acumulador. ");
                    break;
            }
            return;
        }
        atualizarGUI();
    }

    private void erroFatal(String mensagem) {
        temporizadorExecucao.stop();
        adicionarAoPainel("\n" + mensagem + "\n", estiloErro);
        //areaConsole.append("\n" + mensagem + "\n");
        JOptionPane.showMessageDialog(this, mensagem, "Erro Fatal", JOptionPane.ERROR_MESSAGE);
        definirControlesAtivos(true);
        botaoExecutar.setEnabled(false);
    }

    private void adicionarAoPainel(String msg, Style estilo) {
        StyledDocument doc = areaConsole.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), msg, estilo);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void definirControlesAtivos(boolean ativo) {
        botaoCarregarArquivo.setEnabled(ativo);
        botaoCarregarMemoria.setEnabled(ativo);
        botaoExecutar.setEnabled(ativo);
        botaoResetar.setEnabled(ativo);
        areaDecodigo.setEnabled(ativo);
    }

    private String gerarRelatorioExecucao() {
        StringBuilder relatorio = new StringBuilder();
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        relatorio.append("==============================================\n");
        relatorio.append("   RELATÓRIO DE EXECUÇÃO DO SIMPLETRON\n");
        relatorio.append("==============================================\n\n");
        relatorio.append("Data/Hora da Execução: ").append(formatador.format(LocalDateTime.now())).append("\n\n");

        relatorio.append("--- CÓDIGO SML EXECUTADO ---\n");
        relatorio.append(areaDecodigo.getText()).append("\n\n");

        relatorio.append("--- LOG DO CONSOLE (ENTRADA/SAÍDA) ---\n");
        relatorio.append(areaConsole.getText()).append("\n");

        relatorio.append("--- DUMP FINAL DA MÁQUINA ---\n");
        relatorio.append(obterDumpComoString());

        return relatorio.toString();
    }

    private String obterDumpComoString() {
        StringBuilder dump = new StringBuilder();

        dump.append("REGISTRADORES:\n");
        dump.append(String.format("acumulador:            %+05d\n", simpletron.getAccumulator()));
        dump.append(String.format("contadorDeInstrucao:   %02d\n", simpletron.getInstructionCounter()));
        dump.append(String.format("registradorDeInstrucao: %+05d\n", simpletron.getInstructionRegister()));
        dump.append(String.format("codigoDeOperacao:      %02d\n", simpletron.getOperationCode()));
        dump.append(String.format("operando:              %02d\n\n", simpletron.getOperand()));

        dump.append("MEMÓRIA:\n");
        dump.append("    ");
        for (int i = 0; i < 10; i++) {
            dump.append(String.format("   %d  ", i));
        }
        dump.append("\n");

        int[] memoria = simpletron.getMemory();
        for (int i = 0; i < 100; i++) {
            if (i % 10 == 0) {
                dump.append(String.format("%2d  ", i));
            }
            dump.append(String.format("%+05d ", memoria[i]));
            if ((i + 1) % 10 == 0) {
                dump.append("\n");
            }
        }
        return dump.toString();
    }

    private void atualizarGUI() {
        campoAcumulador.setText(String.format("%+05d", simpletron.getAccumulator()));
        campoContadorInstrucao.setText(String.format("%02d", simpletron.getInstructionCounter()));
        campoRegistradorInstrucao.setText(String.format("%+05d", simpletron.getInstructionRegister()));
        campoCodigoOperacao.setText(String.format("%02d", simpletron.getOperationCode()));
        campoOperando.setText(String.format("%02d", simpletron.getOperand()));
        
        int[] memoria = simpletron.getMemory();
        for (int i = 0; i < memoria.length; i++) {
            camposMemoria[i].setText(String.format("%+05d", memoria[i]));
            camposMemoria[i].setBackground(Color.WHITE);
        }
        
        int contadorInstrucao = simpletron.getInstructionCounter();
        if (contadorInstrucao >= 0 && contadorInstrucao < 100) {
            camposMemoria[contadorInstrucao].setBackground(Color.CYAN);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SimpletronGUI::new);
    }
}