+1015  // 00: READ -> Leia um número e guarde na posição 15 (num_lido)
+2015  // 01: LOAD -> Carregue o número lido no acumulador
+4208  // 02: BRANCHZERO -> Se o número for 0, PULE PARA A POSIÇÃO 07 (fim do laço)
+2016  // 03: LOAD -> Carregue a soma atual (da posição 16) no acumulador
+3015  // 04: ADD  -> Adicione o número lido (da posição 15) à soma
+2116  // 05: STORE -> Guarde a nova soma na posição 16
+4000  // 06: BRANCH -> Volte incondicionalmente para o início do laço (posição 00)
// --- Fim do Laço ---
+1116  // 07: WRITE -> Imprima a soma final, que está na posição 16
+4300  // 08: HALT  -> Fim do programa
// --- Área de Variáveis ---
+0000  // 15: Variável para armazenar o número lido (num_lido)
+0000  // 16: Variável para armazenar a soma total