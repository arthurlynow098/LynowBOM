# LynowBOM — direção do produto

## Proposta

**"Importe a estrutura que a engenharia já tem. Escolha o produto e a quantidade. Saia com a lista de compras e o preço."**

O cliente não compra um banco de dados de itens; compra a certeza de que o orçamento de 10 quadros considera os 200 parafusos que aparecem em três montagens diferentes, a perda de 12% da chapa e o custo de montagem. O valor está em eliminar a multiplicação manual e o "esqueci um nível".

## Identidade

O castor é o construtor da natureza: junta peças pequenas em uma estrutura maior, exatamente o que uma lista de materiais descreve. Gravura em marfim sobre verde-esmeralda `#14A06B`, logotipo com `Lynow` em marfim e `BOM` em `#1FBF80`, seguindo LynowCut (tigre, laranja), LynowCatch (polvo, azul) e LynowMic (beija-flor, violeta).

No painel, o resumo do orçamento é desenhado como o carimbo de uma folha de desenho técnico, linguagem visual que o público de engenharia reconhece de imediato.

## Diferenciais a desenvolver

Estas são propostas; não são recursos já entregues.

| Prioridade | Recurso | Benefício concreto |
|---|---|---|
| 1 | Revisões da estrutura | Guardar a versão A, B, C de cada montagem e saber qual revisão gerou cada orçamento. |
| 2 | Configuração por opções | "Quadro com 12, 18 ou 24 circuitos" escolhendo variantes, sem cadastrar um produto por combinação. |
| 3 | Comparação de orçamentos | Mostrar o que mudou em materiais e custo entre duas emissões ou duas revisões. |
| 4 | Lista de compras por fornecedor | Agrupar materiais por fornecedor e lote mínimo de compra. |
| 5 | Proposta em PDF | Gerar a proposta comercial com o carimbo, sem expor custos internos. |
| 6 | Atualização de preços em lote | Importar uma tabela de preços de fornecedor sem reenviar a estrutura. |

## Caminho até uso em produção

1. Autenticação e perfis (engenharia edita estrutura; comercial emite orçamento).
2. Migrações versionadas com Flyway no lugar de `ddl-auto`.
3. Testes com planilhas reais da empresa: nomes de colunas, células mescladas, fórmulas.
4. Revisões da estrutura antes de qualquer integração com ERP.

## Fora do escopo inicial

Planejamento de produção (MRP), controle de estoque e roteiro de fabricação. O LynowBOM responde "quanto material e quanto custa"; "quando comprar e quando produzir" é outro produto.
