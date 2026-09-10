# Catálogo de Regras de Negócio — SIFAP Legado

> **Trilha:** [Kit do Time](../README.md) › [Estágio 1](README.md) › **Catálogo de Regras de Negócio**

**Artefato preenchido pelo time durante o Estágio 1.** Cada dupla extrai as regras dos programas `.NSP` e `.NSN` que recebeu e as registra aqui, com rastreabilidade obrigatória até o programa de origem.

| Campo | Valor |
|---|---|
| **Público-alvo** | Todas as duplas — cada dupla preenche a seção dos seus programas |
| **Pré-requisitos** | Ler os programas `.NSP` e `.NSN` atribuídos |
| **Estágio** | Estágio 1 — Arqueologia |
| **Resultado esperado** | Catálogo com `Programa de origem` preenchido para cada regra candidata |

> [!NOTE]
> Cada regra cita o programa de origem com um intervalo de linhas (`arquivo.NSP:Linicio-Lfim` ou `arquivo.NSN:Linicio-Lfim`) e é classificada como **Confirmada** (corroborada pela documentação histórica em `legacy-sifap/legacy-docs/`), **Inferida** (só a partir do código) ou **Mistério** (questão em aberto — registre-a também em [`mysteries-found.md`](mysteries-found.md) com evidência `path:line`, hipótese não confirmada, responsável e status).

> [!IMPORTANT]
> Guia passo a passo: [`GUIDE.md`](GUIDE.md).

**Time**: <!-- preencher -->
**Data da extração**: 2026-09-10
**Documento de confronto**: [BUSINESS-RULES-2012.md](legacy-sifap/legacy-docs/BUSINESS-RULES-2012.md) (levantamento parcial de 2012, `status: INCOMPLETO`, validação técnica pendente)

> [!NOTE]
> **Cobertura desta extração.** Foram lidos os **24 membros Natural**, os **4 DDMs** e a **listagem FDT do arquivo 150**. O cruzamento com os DDMs promoveu regras antes Inferidas e revelou 11 novos mistérios — veja [Cruzamento com DDMs e FDT](#cruzamento-com-ddms-e-fdt).

---

## Regras de `LDASIFAP.NSL` (área de dados compartilhada)

Não contém lógica condicional — contém **tabelas de parâmetro** que outras regras consomem. Registrado aqui porque os valores são a regra.

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | O sistema deve aplicar um fator regional por código de região, em tabela de 27 posições, sendo 26 e 27 reservadas com fator 1,0000. | Ubíqua — `O sistema DEVE manter uma tabela de fator regional com 27 posições.` | `LDASIFAP.NSL:38-40` | Inferida | Tabela declarada como referência oficial. |
| 2 | O sistema deve tratar fevereiro como tendo 29 dias em qualquer ano. | Ubíqua — `O sistema DEVE usar 29 dias para fevereiro.` | `LDASIFAP.NSL:78-79` | Mistério | <!-- mystery: comentário diz "FEBRUARY HAS BEEN LOADED WITH 29 SINCE 1997 - SEE TICKET 3120". Não há tratamento de ano bissexto em lugar nenhum. Aceita 29/02 de ano não bissexto? --> |
| 3 | Ao ler datas históricas em `YYMMDD`, o sistema deve resolver o século pela janela de pivô 50: `AA < 50` → 20XX; `AA >= 50` → 19XX. | Orientada a estado — `ENQUANTO lê arquivo histórico em YYMMDD, o sistema DEVE expandir o século pela janela de pivô 50.` | `LDASIFAP.NSL:88-95` | Inferida | Remediação Y2K de 1998. Ativa em `BATCHPGT.NSP:339-347`; comentada nos demais módulos. |
| 4 | O sistema deve reconhecer 27 UFs válidas. | Ubíqua — `O sistema DEVE aceitar somente as 27 UFs da tabela.` | `LDASIFAP.NSL:66-70` | Mistério | <!-- mystery: a tabela de UF válida (LDASIFAP:66-70) e a tabela UF do fator regional (LDASIFAP:44-47) NÃO contêm o mesmo conjunto: a segunda tem 'ZZ' em 3 posições e omite AL, PB, RN. Qual é a autoritativa? --> |
| 5 | O sistema deve classificar a contribuição social em 4 faixas de valor bruto com alíquotas de 3%, 5%, 7% e 9%. | Ubíqua — `O sistema DEVE manter 4 faixas de contribuição social.` | `LDASIFAP.NSL:60-64` | Inferida | Comentário: "MPS ORDINANCE 12/2004 NOT REVIEWED - TICKET 5510 - OPEN SINCE 2016". |

---

## Regras de `CCVALCPF.NSC` (copycode — rotina padrão de CPF)

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | Se o CPF contiver qualquer caractere não numérico, o sistema deve rejeitá-lo. | Indesejada — `SE o CPF contiver caractere não numérico, ENTÃO o sistema DEVE rejeitar o documento.` | `CCVALCPF.NSC:45-48` | Inferida | Teste `MASK(NNNNNNNNNNN)`. |
| 2 | Se os 11 dígitos do CPF forem todos iguais, o sistema deve rejeitá-lo. | Indesejada — `SE o CPF tiver todos os dígitos iguais, ENTÃO o sistema DEVE rejeitar o documento.` | `CCVALCPF.NSC:80-91` | Inferida | Sem exceção para prefixo `000`. Compare com a regra 4 de `VALBENEF`. |
| 3 | O sistema deve validar o CPF por módulo 11 com pesos 10→2 (1º DV) e 11→2 (2º DV); resto menor que 2 produz dígito 0. | Ubíqua — `O sistema DEVE validar o CPF por módulo 11 conforme o algoritmo padrão.` | `CCVALCPF.NSC:94-127` | Confirmada | Doc §1.1 RN-001 (CPF válido por dígito verificador). Usa `DIVIDE ... REMAINDER`. |

> [!CAUTION]
> Cabeçalho do próprio copycode, `CCVALCPF.NSC:29-34`: *"INLINE COPIES OF THIS ROUTINE EXIST IN OTHER SIFAP MODULES WITH DIFFERENT BEHAVIOR... THEY ARE NOT EQUIVALENT"*. Existem **4 implementações distintas de CPF** no sistema — veja a seção [Divergências entre implementações](#divergências-entre-implementações-de-cpf).

---

## Regras de `SUBVALCP.NSN` (subprograma corporativo de CPF)

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | Se o tipo de documento informado não for `C`, o sistema deve recusar a chamada com código 1004. | Indesejada — `SE o tipo de documento não for C, ENTÃO o sistema DEVE retornar 1004.` | `SUBVALCP.NSN:50-54` | Inferida | Contrato de PDA compartilhado. |
| 2 | Se o CPF estiver em branco ou for `00000000000`, o sistema deve retornar 1002. | Indesejada — `SE o CPF não for informado, ENTÃO o sistema DEVE retornar 1002.` | `SUBVALCP.NSN:56-60` | Inferida | — |
| 3 | Se o CPF contiver caractere não numérico, o sistema deve retornar 1005. | Indesejada — `SE o CPF contiver caractere não numérico, ENTÃO o sistema DEVE retornar 1005.` | `SUBVALCP.NSN:62-66` | Inferida | — |
| 4 | Se o dígito verificador não conferir, o sistema deve retornar 1001. | Indesejada — `SE o dígito verificador do CPF não conferir, ENTÃO o sistema DEVE retornar 1001.` | `SUBVALCP.NSN:74-78` | Confirmada | Doc §1.1 RN-001. Delega a `CCVALCPF`. |
| 5 | Se ocorrer erro Natural, o sistema deve retornar 9999 sem abortar o chamador. | Indesejada — `SE ocorrer erro Natural, ENTÃO o sistema DEVE retornar 9999.` | `SUBVALCP.NSN:85-90` | Inferida | `ESCAPE ROUTINE`, sem `TERMINATE`. |

---

## Regras de `SUBVALNI.NSN` (subprograma corporativo de NIS/PIS/PASEP)

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | Se o tipo de documento não for `N`, o sistema deve recusar a chamada com 1004. | Indesejada — `SE o tipo de documento não for N, ENTÃO o sistema DEVE retornar 1004.` | `SUBVALNI.NSN:58-62` | Inferida | — |
| 2 | Se o NIS estiver em branco ou for `00000000000`, o sistema deve retornar 1010. | Indesejada — `SE o NIS não for informado, ENTÃO o sistema DEVE retornar 1010.` | `SUBVALNI.NSN:64-68` | Inferida | — |
| 3 | O sistema deve validar o NIS por módulo 11 com pesos fixos 3,2,9,8,7,6,5,4,3,2; resto menor que 2 produz DV 0. | Ubíqua — `O sistema DEVE validar o NIS por módulo 11 com os pesos 3,2,9,8,7,6,5,4,3,2.` | `SUBVALNI.NSN:44`, `SUBVALNI.NSN:130-148` | Inferida | Doc §1.1 RN-001 cita "VALNISN", nome que **não existe** na base. Confirmação apenas parcial. |
| 4 | Se um caractere do NIS não for numérico, a rotina de extração deve tratá-lo como zero. | Indesejada — `SE o NIS contiver caractere não numérico, ENTÃO o sistema DEVE ...` | `SUBVALNI.NSN:132-134` (ramo `NONE VALUE`) | Mistério | <!-- mystery: contradição interna. A linha 70 rejeita NIS não numérico com 1012, mas o ramo NONE VALUE da extração converte o caractere para 0 em vez de rejeitar. Qual caminho realmente executa? Existe entrada que chegue à extração sem passar pelo MASK? --> |

---

## Regras de `VALBENEF.NSN` (validação cadastral)

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | O sistema deve acumular todos os erros de validação e devolver `V` (válido) ou `I` (inválido) com a lista de mensagens. | Ubíqua — `O sistema DEVE acumular todos os erros de validação cadastral.` | `VALBENEF.NSN:120-124`, `VALBENEF.NSN:126-180` | Inferida | Não interrompe no primeiro erro. |
| 2 | Se a data de nascimento tiver ano fora de 1900 até o ano corrente, mês fora de 1-12 ou dia acima do limite do mês, o sistema deve marcar data inválida. | Indesejada — `SE a data de nascimento for inconsistente, ENTÃO o sistema DEVE registrar erro de data.` | `VALBENEF.NSN:302-312` | Inferida | Limite de dias vem de tabela com fevereiro = 29 (`VALBENEF.NSN:104`). |
| 3 | Se o nome não contiver ao menos um espaço a partir da posição 2, o sistema deve marcar nome inválido. | Indesejada — `SE o nome não contiver sobrenome, ENTÃO o sistema DEVE registrar erro de nome.` | `VALBENEF.NSN:316-330` | Inferida | Regra de "nome + sobrenome" implementada como presença de espaço. |
| 4 | Se o CPF tiver todos os dígitos iguais **e** começar com `000`, o sistema deve considerá-lo válido. | Indesejada — `SE o CPF tiver todos os dígitos iguais e prefixo 000, ENTÃO o sistema DEVE aceitá-lo.` | `VALBENEF.NSN:237-244` | Mistério | <!-- mystery: comentário no código diz "GOVERNMENT TEST". Aceita 00000000000 como CPF válido, contrariando CCVALCPF:80-91 e SUBVALCP:56-60, que o rejeitam explicitamente. Quem criou, quando e quantos registros de produção usam esse prefixo? --> |
| 5 | O sistema deve aceitar apenas as situações cadastrais `A`, `S`, `C`, `I` e `D`. | Ubíqua — `O sistema DEVE aceitar somente as situações A, S, C, I e D.` | `VALBENEF.NSN:174-179` | Mistério | <!-- mystery: doc §1.3 RN-011 afirma que exclusão lógica usa BN-CD-SIT = 'E'. O valor 'E' não é aceito por nenhum módulo lido. O código usa 'C'/'D'. Qual é o domínio real do campo? --> |
| 6 | O sistema deve validar a UF contra uma tabela de 27 valores, somente quando a UF for informada. | Opcional — `ONDE a UF for informada, o sistema DEVE validá-la contra a tabela de 27 UFs.` | `VALBENEF.NSN:156-170` | Inferida | UF em branco passa sem erro. |

---

## Regras de `VALDOCS.NSP` (validação de documentos)

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | Se o RG tiver menos de 5 caracteres ou estiver em branco, o sistema deve marcá-lo inválido. | Indesejada — `SE o RG tiver menos de 5 caracteres, ENTÃO o sistema DEVE registrar erro de RG.` | `VALDOCS.NSP:207-224` | Inferida | Comprimento medido pela posição do primeiro espaço. |
| 2 | Se o NIS informado for inválido, o sistema deve acrescentar erro e marcar o resultado como inválido. | Indesejada — `SE o NIS for inválido, ENTÃO o sistema DEVE registrar erro.` | `VALDOCS.NSP:109-117` | Inferida | Única validação de NIS que **bloqueia**; `CADBENEF` só avisa. |
| 3 | Se os 3 primeiros dígitos do CPF forem `000`, `001`, `002`, `010`, `011`, `099`, `100` ou `999`, o sistema deve marcar o documento como válido e **zerar todos os erros já acumulados**. | Indesejada — `SE o CPF tiver prefixo especial, ENTÃO o sistema DEVE ...` | `VALDOCS.NSP:58-65`, `VALDOCS.NSP:228-242` | Mistério | <!-- mystery: CHECK-DOC-SPECIAL não apenas aceita o CPF: faz MOVE 'V' TO #RESULT e MOVE 0 TO #QTY-ERRS, apagando inclusive o erro de RG inválido detectado antes. Um CPF com prefixo 999 e RG vazio passa como válido. É intencional? Qual a origem dos 8 prefixos? --> |
| 4 | O sistema deve validar o NIS **depois** da verificação de documento especial. | Ubíqua — `O sistema DEVE validar o NIS após a verificação de documento especial.` | `VALDOCS.NSP:98-117` | Inferida | Comentário: "BY DECISION OF THE BENEFITS DEPARTMENT - TICKET 6621/2011". Ordem faz o erro de NIS sobreviver ao reset da regra 3. |

---

## Regras de `VALELEG.NSN` (elegibilidade)

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | Se o beneficiário não existir no arquivo 150, o sistema deve retornar 2001 e encerrar. | Indesejada — `SE o beneficiário não for encontrado, ENTÃO o sistema DEVE retornar 2001.` | `VALELEG.NSN:85-89` | Inferida | — |
| 2 | Se o programa social não existir, o sistema deve retornar 2003 e encerrar. | Indesejada — `SE o programa social não for encontrado, ENTÃO o sistema DEVE retornar 2003.` | `VALELEG.NSN:101-105` | Inferida | — |
| 3 | Se o programa social não estiver ativo, o sistema deve retornar 2004 e encerrar. | Indesejada — `SE o programa não estiver ativo, ENTÃO o sistema DEVE retornar 2004.` | `VALELEG.NSN:114-118` | Confirmada | Doc §1.1 RN-003 (vínculo com programa ativo). |
| 4 | Se a região do beneficiário for 99, o sistema deve declará-lo elegível e encerrar **antes** de qualquer outra verificação. | Indesejada — `SE a região for 99, ENTÃO o sistema DEVE ...` | `VALELEG.NSN:123-128` | Mistério | <!-- mystery: bypass total de elegibilidade. Doc §4.2 nota e §5 tabela registram "Bypass de elegibilidade da região 99 - identificado no código, sem explicação conhecida - prioridade Alta". Marcos Antônio disse "é o bypass do Roberto". Quem autorizou, quando e quantos beneficiários têm região 99 em produção? --> |
| 5 | Enquanto a situação do beneficiário não for `A`, o sistema deve marcá-lo inelegível com o motivo correspondente (`S` suspenso, `C`/`D` cancelado, `I` inativo). | Estado — `ENQUANTO a situação do beneficiário não for A, o sistema DEVE marcá-lo inelegível.` | `VALELEG.NSN:133-151` | Confirmada | Doc §4.2 (situação cadastral ativa). |
| 6 | Onde o programa definir idade mínima maior que zero, o sistema deve reprovar beneficiário com idade inferior. | Opcional — `ONDE o programa definir idade mínima, o sistema DEVE reprovar idade inferior.` | `VALELEG.NSN:156-162` | Inferida | Idade mínima 0 desliga a regra. |
| 7 | Onde o programa definir idade máxima maior que zero, o sistema deve reprovar beneficiário com idade superior. | Opcional — `ONDE o programa definir idade máxima, o sistema DEVE reprovar idade superior.` | `VALELEG.NSN:163-169` | Inferida | — |
| 8 | Onde o programa definir renda máxima maior que zero, o sistema deve reprovar renda familiar superior ao teto. | Opcional — `ONDE o programa definir renda máxima, o sistema DEVE reprovar renda superior.` | `VALELEG.NSN:174-180` | Confirmada | Doc §4.2 (renda per capita nas faixas do programa). |
| 9 | Para programa tipo `A` (assistência), se a renda for maior que 600,00 **e** não houver dependentes, o sistema deve reprovar; e deve exigir documentação completa (`IND-DOCS-OK = 'S'`). | Indesejada — `SE o programa for do tipo A e a renda exceder 600,00 sem dependentes, ENTÃO o sistema DEVE reprovar.` | `VALELEG.NSN:186-200` | Mistério | <!-- mystery: o literal 600,00 está cravado no código, fora de qualquer tabela de parâmetro, e não aparece na documentação. Coincide com o limite da faixa 2 de renda em LDASIFAP:55. É o mesmo parâmetro duplicado? Quem atualiza quando a faixa muda? --> |
| 10 | Para programa tipo `P` (pensão), o sistema deve reprovar idade inferior a 60 anos. | Indesejada — `SE o programa for do tipo P e a idade for inferior a 60, ENTÃO o sistema DEVE reprovar.` | `VALELEG.NSN:201-207` | Inferida | Literal fixo, ignora `AGE-MIN` do programa. |
| 11 | Para programa tipo `T` (trabalho), o sistema deve reprovar idade fora do intervalo de 16 a 65 anos. | Indesejada — `SE o programa for do tipo T e a idade estiver fora de 16-65, ENTÃO o sistema DEVE reprovar.` | `VALELEG.NSN:208-214` | Inferida | Coexiste com as regras 6 e 7, que podem reprovar por outro limite. |
| 12 | Se o tipo do programa não for `A`, `P` ou `T`, o sistema deve reprovar por tipo desconhecido. | Indesejada — `SE o tipo de programa for desconhecido, ENTÃO o sistema DEVE reprovar.` | `VALELEG.NSN:215-218` | Inferida | Ramo `NONE`. |
| 13 | Onde o 1º caractere do código de elegibilidade for `R`, o sistema deve exigir NIS cadastrado diferente de zero. | Opcional — `ONDE o código de elegibilidade iniciar por R, o sistema DEVE exigir NIS cadastrado.` | `VALELEG.NSN:251-258` | Inferida | Verifica apenas `NUM-NIS = 0`, sem validar dígito. |
| 14 | Onde o 2º caractere do código de elegibilidade for `D`, o sistema deve exigir ao menos um dependente. | Opcional — `ONDE o código de elegibilidade tiver D na 2ª posição, o sistema DEVE exigir dependentes.` | `VALELEG.NSN:259-266` | Inferida | Código `A5`; posições 3 a 5 nunca são lidas. |
| 15 | Se o beneficiário for inelegível, o sistema deve retornar 2010 e devolver **apenas o primeiro** dos motivos acumulados. | Indesejada — `SE o beneficiário for inelegível, ENTÃO o sistema DEVE retornar 2010 com o primeiro motivo.` | `VALELEG.NSN:231-237` | Mistério | <!-- mystery: o array #REASON acumula até 10 motivos, mas só #REASON(1) é devolvido pelo PDA. Os demais motivos são descartados sem log. O beneficiário reprovado por 4 razões recebe explicação de 1. Perda intencional? --> |

---

## Regras de `CALCBENF.NSN` (cálculo do benefício)

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | Se o mês do período for menor que 1 ou maior que 12, o sistema deve retornar 2020 e encerrar. | Indesejada — `SE o período for inválido, ENTÃO o sistema DEVE retornar 2020.` | `CALCBENF.NSN:158-162` | Inferida | Não valida o ano. |
| 2 | Se a situação do beneficiário não for `A`, o sistema deve retornar 2002 e não calcular. | Indesejada — `SE o beneficiário não estiver ativo, ENTÃO o sistema DEVE retornar 2002.` | `CALCBENF.NSN:180-186` | Confirmada | Doc §5.1 (apenas beneficiários ativos são processados). |
| 3 | O sistema deve aplicar fator regional da tabela quando a região estiver entre 1 e 25; caso contrário, fator 1,0000. | Ubíqua — `O sistema DEVE aplicar o fator regional correspondente à região.` | `CALCBENF.NSN:200-205` | Mistério | <!-- mystery: as posições 26 e 27 da tabela existem e valem 1,0000, mas o IF só cobre 1-25. Região 26 ou 27 cai no ELSE. A tabela de 27 posições e o teste de 25 discordam. Qual é o intervalo válido? --> |
| 4 | O sistema deve calcular o fator familiar: 0 dependentes → 1,0000; 1-2 → 1,0000 + 0,05 por dependente; 3-4 → 1,1000 + 0,03 por dependente acima de 2; 5 ou mais → 1,1600 + 0,02 por dependente acima de 4. | Ubíqua — `O sistema DEVE calcular o fator familiar em três faixas de dependentes.` | `CALCBENF.NSN:207-221` | Mistério | <!-- mystery: doc §2.1 RN-013 descreve acréscimo ADITIVO por dependente (VALOR-BASE + ACRESCIMO * QT-DEPEND). O código aplica fator MULTIPLICATIVO. As duas formas produzem valores diferentes. Qual vigora? --> |
| 5 | O sistema deve determinar o fator de renda pela primeira faixa cujo teto seja maior ou igual à renda familiar. | Ubíqua — `O sistema DEVE aplicar o fator da primeira faixa de renda que comporte a renda declarada.` | `CALCBENF.NSN:344-352` | Confirmada | Doc §2.2 RN-018 (ordem crescente, primeira faixa cujo limite superior seja maior ou igual). |
| 6 | O sistema deve aplicar fator etário: 65 anos ou mais → 1,1500; 60 a 64 → 1,1000; menos de 18 → 1,0500; demais → 1,0000. | Ubíqua — `O sistema DEVE aplicar o fator etário conforme a faixa de idade.` | `CALCBENF.NSN:240-254` | Inferida | Não documentado em 2012. |
| 7 | O sistema deve calcular o valor bruto como `base × fator_regional × fator_familiar × fator_renda × fator_etário`, e depois multiplicá-lo por `(1 + fator_de_ajuste do programa)`. | Ubíqua — `O sistema DEVE calcular o benefício pelo produto dos cinco fatores e do ajuste do programa.` | `CALCBENF.NSN:258-262` | Mistério | <!-- mystery: doc §2.1 RN-013 registra fórmula aditiva completamente diferente e a nota admite "pelo menos mais 3 variações no CALCBENF" não validadas. Esta é a 1ª variação; BATCHPGT:430-432 tem a 2ª (idêntica, duplicada). Onde estão as demais? --> |
| 8 | O sistema deve truncar valores monetários em 2 casas decimais, sem arredondamento. | Ubíqua — `O sistema DEVE truncar valores monetários em duas casas decimais.` | `CALCBENF.NSN:265-266`, `279-280`, `287-288`, `305-306` | Confirmada | Doc §2.1 RN-014 (truncamento, não arredondamento matemático). Truncamento por `#AMT-TEMP (N11)`. |
| 9 | Quando o mês de referência for dezembro, o sistema deve marcar o pagamento como tipo `D` e somar o 13º, calculado por `base × fator_regional × fator_etário`. | Evento — `QUANDO o mês de referência for dezembro, o sistema DEVE calcular e somar o 13º benefício.` | `CALCBENF.NSN:275-281` | Mistério | <!-- mystery: doc §6 lista "Cálculo do 13º benefício - não foi possível acessar a rotina - prioridade Alta". O comentário do código (linha 271) descreve a fórmula como AMT_BASE * FACTOR_REGION * (ACTIVE_MONTHS/12), mas o código executado não usa meses ativos nenhum: usa FACTOR_AGE. Comentário e código divergem. Qual é a regra? --> |
| 10 | Quando o mês for dezembro e o programa for do tipo `A`, o sistema deve somar abono de 15% sobre o valor mensal. | Evento — `QUANDO for dezembro e o programa for do tipo A, o sistema DEVE somar abono de 15%.` | `CALCBENF.NSN:284-292` | Inferida | Doc §6 registra o abono como não documentado. |
| 11 | O sistema deve aplicar desconto de contribuição social de 3% quando o valor bruto exceder 500,00. | Evento — `QUANDO o valor bruto exceder 500,00, o sistema DEVE aplicar 3% de contribuição social.` | `CALCBENF.NSN:358-366` | Mistério | <!-- mystery: rotina CALC-DISC do CALCBENF aplica alíquota ÚNICA de 3%. O CALCDSCT (:198-203) aplica 4 faixas progressivas (3/5/7/9%). O mesmo pagamento recebe descontos diferentes conforme quem calcula. O comentário admite "SIMPLIFIED (SEE CALCDSCT FOR FULL VERSION)". Qual prevalece em produção? --> |
| 12 | Se o valor líquido resultar negativo, o sistema deve gravar zero. | Indesejada — `SE o valor líquido for negativo, ENTÃO o sistema DEVE gravá-lo como zero.` | `CALCBENF.NSN:299-303` | Inferida | Silencioso: não registra o fato. |
| 13 | Ao concluir o cálculo, o sistema deve gravar um pagamento com situação `G` e confirmar a transação. | Evento — `QUANDO o cálculo for concluído, o sistema DEVE gravar o pagamento com situação G.` | `CALCBENF.NSN:308-320` | Mistério | <!-- mystery: doc §5.1 diz que o registro é gravado com situação 'P' (pendente). O código grava 'G'. Além disso o DDM PAYMENT (:73-75) documenta P=PENDING e G=GENERATED como estados distintos. Divergência entre doc, DDM e código. --> |

---

## Regras de `CALCDSCT.NSP` (descontos)

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | Se o pagamento informado não existir ou não pertencer ao CPF informado, o sistema deve encerrar sem calcular. | Indesejada — `SE o pagamento não pertencer ao CPF informado, ENTÃO o sistema DEVE encerrar.` | `CALCDSCT.NSP:80-91` | Inferida | — |
| 2 | O sistema deve aplicar contribuição social obrigatória pela primeira faixa cujo teto comporte o valor bruto (3%, 5%, 7% ou 9%). | Ubíqua — `O sistema DEVE aplicar a contribuição social pela faixa correspondente ao valor bruto.` | `CALCDSCT.NSP:198-205` | Inferida | Doc §3 RN-022 lista "contribuição previdenciária" como código 03, sem alíquotas. |
| 3 | O sistema deve limitar o total de descontos a 30% do valor bruto. | Ubíqua — `O sistema DEVE limitar o total de descontos a 30% do valor bruto.` | `CALCDSCT.NSP:107-111`, `CALCDSCT.NSP:170-174` | Confirmada | Doc §3 RN-021. |
| 4 | Descontos do tipo `J` (judicial) não se sujeitam ao limite de 30%. | Opcional — `ONDE o desconto for judicial, o sistema DEVE ignorar o limite de 30%.` | `CALCDSCT.NSP:128-137`, `CALCDSCT.NSP:170-174` | Confirmada | Doc §3 nota registra a exceção como relatada por Marcos Antônio e **não confirmada em 2012**; o código a confirma. |
| 5 | Se o desconto tiver valor fixo maior que zero, o sistema deve usá-lo; caso contrário, deve aplicar o percentual sobre o valor bruto. | Indesejada — `SE o desconto não tiver valor fixo, ENTÃO o sistema DEVE aplicar o percentual sobre o bruto.` | `CALCDSCT.NSP:130-136` (J), `140-146` (P), `158-164` (A) | Inferida | Vale para `J`, `P` e `A`; `I` é sempre percentual e `S` é sempre 1%. |
| 6 | Desconto sindical deve ser de 1% do valor bruto, ignorando valor e percentual cadastrados. | Ubíqua — `O sistema DEVE aplicar 1% do valor bruto como desconto sindical.` | `CALCDSCT.NSP:152-155` | Inferida | Literal fixo. |
| 7 | O sistema deve ignorar desconto cuja data de fim seja anterior a hoje ou cuja data de início seja posterior a hoje. | Estado — `ENQUANTO o desconto estiver fora do período de vigência, o sistema DEVE ignorá-lo.` | `CALCDSCT.NSP:117-124` | Inferida | Data fim 0 significa vigência indeterminada. |
| 8 | O sistema deve ignorar silenciosamente desconto de tipo desconhecido. | Indesejada — `SE o tipo de desconto for desconhecido, ENTÃO o sistema DEVE ...` | `CALCDSCT.NSP:166-167` (`NONE ... IGNORE`) | Mistério | <!-- mystery: o DDM PAYMENT (:47) documenta os tipos como IR/JD/CS/PA/EM/TX/OU/EX (2 caracteres) e o campo é A3. O CALCDSCT compara com C/I/J/S/P/A (1 caractere) via #TYPE-DISC (A1). Se o dado gravado segue o DDM, NENHUM tipo casa e todos caem em IGNORE, zerando os descontos itemizados. Verificar dados reais. --> |
| 9 | Quando o limite de 30% é atingido, o sistema deve substituir o total acumulado pelo teto. | Evento — `QUANDO o total de descontos exceder o teto, o sistema DEVE reduzi-lo ao teto.` | `CALCDSCT.NSP:170-174` | Mistério | <!-- mystery: doc §3 RN-021 diz que descontos acima do limite são REJEITADOS e o benefício é processado SEM descontos, com auditoria. O código faz o oposto: trunca o total no teto e mantém os descontos. Doc §3 RN-023 descreve descarte por prioridade; o código não tem prioridade nenhuma, processa na ordem do grupo PE. --> |

---

## Regras de `CALCCORR.NSP` (correção retroativa)

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | Se o período inicial for maior que o final, o sistema deve encerrar sem processar. | Indesejada — `SE o período inicial for maior que o final, ENTÃO o sistema DEVE encerrar.` | `CALCCORR.NSP:151-154` | Inferida | — |
| 2 | O sistema deve validar o CPF pela rotina corporativa antes de corrigir qualquer pagamento. | Ubíqua — `O sistema DEVE validar o CPF pela rotina corporativa antes da correção.` | `CALCCORR.NSP:157-168` | Inferida | `CALLNAT 'SUBVALCP'`. |
| 3 | O sistema deve ignorar pagamento já corrigido (`IND-CORR = 'S'`). | Estado — `ENQUANTO o pagamento já estiver corrigido, o sistema DEVE ignorá-lo.` | `CALCCORR.NSP:186-188` | Inferida | Torna a correção idempotente. |
| 4 | O sistema deve corrigir o valor por índice IPCA acumulado do mês de referência. | Ubíqua — `O sistema DEVE corrigir o valor pelo índice IPCA do período.` | `CALCCORR.NSP:191-201`, `CALCCORR.NSP:230-239` | Mistério | <!-- mystery: a tabela IPCA só tem os anos 2010, 2011 e 2012 carregados (#YEAR-TAB 1-3 de 10). Para qualquer outro ano o FOR não encontra o ano e #INDEX-ACCUM permanece 1,000000: a correção resulta zero e o pagamento é silenciosamente ignorado (a diferença não é > 0). Comentário diz "LAST LOAD: 2014" mas 2013 e 2014 não estão na tabela. --> |
| 5 | O sistema deve aplicar o índice de **um único mês**, não o acumulado entre o mês de referência e hoje. | Ubíqua — `O sistema DEVE aplicar o índice do mês de referência do pagamento.` | `CALCCORR.NSP:230-239` | Mistério | <!-- mystery: a sub-rotina chama-se CALC-INDEX-ACCUM e a variável é #INDEX-ACCUM, mas o FOR aplica exatamente um índice mensal e sai (ESCAPE BOTTOM). Não há acumulação entre períodos. Nome e comportamento divergem. Correção de 5 anos usa índice de 1 mês? --> |
| 6 | O sistema deve gravar a correção somente quando a diferença for positiva, marcando `IND-CORR = 'S'` e registrando trilha de auditoria. | Evento — `QUANDO a diferença for positiva, o sistema DEVE gravar a correção e registrar auditoria.` | `CALCCORR.NSP:204-218` | Inferida | Diferença zero ou negativa não gera registro nem log. |
| 7 | O sistema deve manter desabilitada a correção do Plano Verão para o período 01/1989 a 01/1991. | Ubíqua — `O sistema DEVE ...` | `CALCCORR.NSP:129-143` (bloco comentado) | Mistério | <!-- mystery: bloco comentado "DO NOT REMOVE (HISTORICAL)" com fatores 2,7500 e 1,4289 e marcação IND-CORR = 'V'. Está desativado desde quando? Existem pagamentos com IND-CORR = 'V' na base? A regra ainda tem efeito jurídico? --> |

---

## Regras de `BATCHPGT.NSP` (folha mensal)

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | Se o período não for informado via `CMSYNIN`, o sistema deve derivá-lo da data corrente. | Indesejada — `SE o período não for informado, ENTÃO o sistema DEVE derivá-lo da data corrente.` | `BATCHPGT.NSP:171-173` | Confirmada | `SIFAPJ01.jcl:70-76` fornece o período na 3ª linha do `CMSYNIN`. |
| 2 | O sistema deve processar os beneficiários em ordem de CPF. | Ubíqua — `O sistema DEVE processar os beneficiários em ordem crescente de CPF.` | `BATCHPGT.NSP:250` | Mistério | <!-- mystery: doc §5.1 nota afirma que a ordenação real é ALFABÉTICA POR NOME (BN-NM-BENEF, descritor principal do FNR 150) e adverte que mudar a ordem quebra os totalizadores. O código lê BY NUM-CPF. Comentário do código (:245-248) diz "DOWNSTREAM SYSTEMS DEPEND ON THIS ORDERING". Quem depende, e de qual ordem? --> |
| 3 | Se o CPF lido for igual ao anterior, o sistema deve ignorar o registro como duplicado. | Indesejada — `SE o CPF for igual ao anterior, ENTÃO o sistema DEVE ignorar o registro.` | `BATCHPGT.NSP:256-261` | Inferida | Deduplicação só funciona porque a leitura é ordenada por CPF. |
| 4 | Se o beneficiário não estiver com situação `A`, o sistema deve ignorá-lo. | Estado — `ENQUANTO o beneficiário não estiver ativo, o sistema DEVE ignorá-lo na folha.` | `BATCHPGT.NSP:263-267` | Confirmada | Doc §5.1. |
| 5 | Se o CPF gravado no cadastro for inválido, o sistema deve rejeitar o beneficiário e registrá-lo no log `CMWKF02`. | Indesejada — `SE o CPF cadastrado for inválido, ENTÃO o sistema DEVE rejeitá-lo e registrá-lo no log de rejeitados.` | `BATCHPGT.NSP:276-289` | Inferida | Comentário: até 2011 a folha não validava CPF (ticket 6620/2011). |
| 6 | Se já existir pagamento para o mesmo CPF e período, o sistema deve ignorar o beneficiário. | Indesejada — `SE já existir pagamento para o CPF e período, ENTÃO o sistema DEVE ignorá-lo.` | `BATCHPGT.NSP:292-298` | Confirmada | `SIFAPJ01.jcl:27-29` (restart não duplica). Usa o superdescritor `S1` do `PAYMENT.ddm:163-164`. |
| 7 | Se o programa do beneficiário não for encontrado, o sistema deve rejeitá-lo; se o programa não estiver ativo, deve ignorá-lo. | Indesejada — `SE o programa não for encontrado, ENTÃO o sistema DEVE rejeitar o beneficiário.` | `BATCHPGT.NSP:302-325` | Inferida | Rejeição e ignorância são contadas separadamente. |
| 8 | Se o ano de nascimento lido for menor que 100, o sistema deve expandi-lo pela janela de século de pivô 50. | Indesejada — `SE o ano de nascimento tiver 2 dígitos, ENTÃO o sistema DEVE expandi-lo pela janela de pivô 50.` | `BATCHPGT.NSP:339-348` | Inferida | Único módulo com a janela Y2K **ativa**; comentário afirma que registros não convertidos ainda chegam. |
| 9 | Se `VALELEG` retornar código diferente de zero, o sistema deve ignorar o beneficiário. | Indesejada — `SE a elegibilidade for reprovada, ENTÃO o sistema DEVE ignorar o beneficiário.` | `BATCHPGT.NSP:369-379` | Inferida | Motivo detalhado não é gravado no log de rejeitados. |
| 10 | Após chamar `CALCBENF`, o sistema deve recalcular o benefício inline com a mesma fórmula e gravar um segundo pagamento. | Evento — `QUANDO o cálculo corporativo terminar, o sistema DEVE ...` | `BATCHPGT.NSP:381-388` e `BATCHPGT.NSP:390-488` | Mistério | <!-- mystery: CRÍTICO. CALCBENF (:308-320) executa STORE PAYMENT-V + END TRANSACTION. BATCHPGT ignora #PC-AMT-* devolvido, recalcula tudo inline e executa outro STORE PAYMENT-V (:488). Dois registros de pagamento para o mesmo CPF/período na mesma iteração. O comentário (:363-367) admite: "THE INLINE CALCULATION BELOW REMAINS ACTIVE PENDING A DECISION - TICKET 6622/2011 OPEN". Confirmar em produção quantos pares duplicados existem. --> |
| 11 | O sistema deve gerar registro de extrato bancário de 240 posições por pagamento, com tipo `3`. | Evento — `QUANDO um pagamento for gravado, o sistema DEVE gerar o registro de remessa correspondente.` | `BATCHPGT.NSP:494-502` | Inferida | `SIFAPJ01.jcl:56-60` aloca `CMWKF01` com `LRECL=240`. |
| 12 | O sistema deve encerrar com RC 8 quando nada for gerado, RC 4 quando houver rejeitados e RC 0 quando não houver. | Evento — `QUANDO o processamento terminar, o sistema DEVE devolver o código de retorno correspondente ao resultado.` | `BATCHPGT.NSP:558-566` | Confirmada | `SIFAPJ01.jcl:104-110`. |
| 13 | Se ocorrer erro Natural, o sistema deve desfazer a transação, fechar os arquivos e encerrar com RC 12. | Indesejada — `SE ocorrer erro Natural, ENTÃO o sistema DEVE desfazer a transação e encerrar com RC 12.` | `BATCHPGT.NSP:572-582` | Confirmada | `SIFAPJ01.jcl:95-97` (STEP030 dispara aviso). |
| 14 | O sistema deve interromper o processamento ao exceder 100 erros. | Indesejada — `SE a quantidade de erros exceder o máximo, ENTÃO o sistema DEVE interromper.` | — (ausente no código) | Mistério | <!-- mystery: doc §5.2 afirma "Se a quantidade de erros exceder o parâmetro MAX-ERROS (padrão: 100), o processamento é interrompido com ABEND U4038". Não existe MAX-ERROS nem ABEND no BATCHPGT. A regra foi removida, nunca implementada, ou vive em outro módulo? --> |

---

## Regras de `BATCHCON.NSP` (conciliação bancária CNAB 240)

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | O sistema deve processar apenas registros CNAB de tipo `3` (detalhe). | Estado — `ENQUANTO o registro não for do tipo 3, o sistema DEVE ignorá-lo.` | `BATCHCON.NSP:145-148` | Inferida | Layout Banco do Brasil. |
| 2 | O sistema deve converter o valor do retorno de centavos para reais dividindo por 100. | Ubíqua — `O sistema DEVE converter o valor do retorno bancário de centavos para reais.` | `BATCHCON.NSP:158-164` | Inferida | Comentário: até 2017 o A15 ia direto para campo decimal (ticket 8112/2017). |
| 3 | O sistema deve considerar conciliado quando a diferença absoluta entre o valor SIFAP e o do banco for de até 0,01. | Ubíqua — `O sistema DEVE considerar conciliado quando a diferença for de até um centavo.` | `BATCHCON.NSP:188-193` | Inferida | Tolerância de 1 centavo, não documentada. |
| 4 | Se houver divergência de valor, o sistema deve registrar auditoria de divergência e **não** atualizar a situação do pagamento. | Indesejada — `SE houver divergência de valor, ENTÃO o sistema DEVE registrar auditoria e não atualizar o pagamento.` | `BATCHCON.NSP:193-201` | Inferida | Doc §6 registra a conciliação como área não levantada. |
| 5 | Quando o código de retorno for `00`, o sistema deve marcar o pagamento como `P` e gravar a data de crédito; `01` → `D`; `02` → `E`. | Evento — `QUANDO o retorno bancário for recebido, o sistema DEVE atualizar a situação do pagamento conforme o código.` | `BATCHCON.NSP:204-231` | Mistério | <!-- mystery: o DDM PAYMENT (:73-75) define P=PENDING, D=RETURNED, E=ISSUED, C=CONFIRMED, X=CANCELED. O código usa P como PAGO e E como ESTORNADO, e BATCHREL (:177-190) traduz P=PAID, D=RETURNED, E=REVERSED. Três vocabulários para o mesmo campo. Qual é o domínio real? --> |
| 6 | Se o código de retorno for desconhecido, o sistema deve apenas registrar a ocorrência no log e contar o item como conciliado. | Indesejada — `SE o código de retorno for desconhecido, ENTÃO o sistema DEVE ...` | `BATCHCON.NSP:227-231` | Mistério | <!-- mystery: o contador #QTY-RECONCILED já foi incrementado (:203) antes do DECIDE. Um retorno desconhecido não atualiza o pagamento mas conta como conciliado, e o RC final (:290) não o considera pendência. Item invisível na conferência. --> |
| 7 | O sistema deve gravar auditoria de conciliação com ação `CO`. | Evento — `QUANDO um item for conciliado, o sistema DEVE gravar auditoria.` | `BATCHCON.NSP:311-324` | Mistério | <!-- mystery: CCAUDIT (:57-60) documenta "ACTION 'CO' (QUERY) MUST NOT BE RECORDED SINCE 2010 DUE TO VOLUME - PORT. CGTI 213/2010". Aqui 'CO' significa CONCILIAÇÃO, e RELAUDIT (:170-172) interpreta 'CO' como RECONCILIAÇÃO enquanto CONSBENF (:172) grava 'CO' como CONSULTA. O mesmo código de ação tem 3 significados. --> |
| 8 | Se houver divergências ou registros não encontrados, o sistema deve encerrar com RC 4. | Indesejada — `SE houver pendências na conciliação, ENTÃO o sistema DEVE encerrar com RC 4.` | `BATCHCON.NSP:290-294` | Inferida | Cabeçalho registra que o job não é automatizado (ticket 8110/2017). |

---

## Regras de `BATCHREL.NSP` (relatório consolidado)

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | Se o período não for informado, o sistema deve encerrar com RC 12. | Indesejada — `SE o período não for informado, ENTÃO o sistema DEVE encerrar com RC 12.` | `BATCHREL.NSP:119-122` | Inferida | Diferente de `BATCHPGT`, que assume a data corrente. |
| 2 | O sistema deve agrupar os totais em 5 macrorregiões pelo código de região: 1-5 Norte, 6-10 Nordeste, 11-15 Sudeste, 16-20 Sul, demais Centro-Oeste. | Ubíqua — `O sistema DEVE consolidar os totais por macrorregião.` | `BATCHREL.NSP:145-165` | Mistério | <!-- mystery: o ELSE final joga TUDO que não for 1-20 no Centro-Oeste, incluindo região 99 (especial), 0 (beneficiário não encontrado, :141-144) e 26-27 (reservadas). Valores de região 99 aparecem somados ao Centro-Oeste no relatório oficial. --> |
| 3 | O sistema deve **arredondar** os valores brutos ao acumular por região, somando 0,005 antes de truncar. | Ubíqua — `O sistema DEVE arredondar o valor bruto ao consolidar.` | `BATCHREL.NSP:166-170` | Mistério | <!-- mystery: o próprio comentário (:166) admite "ROUNDING DIFFERS FROM CALCBENF (ROUND VS TRUNCATE)". O relatório consolidado não bate com a soma dos pagamentos gravados. Divergência conhecida e mantida. Contraria doc §2.1 RN-014 (truncamento). --> |
| 4 | O sistema deve classificar pagamentos de situação desconhecida como `Gerado`. | Indesejada — `SE a situação do pagamento for desconhecida, ENTÃO o sistema DEVE ...` | `BATCHREL.NSP:177-192` (`NONE` → índice 1) | Mistério | <!-- mystery: situações fora de G/P/C/D/E são somadas silenciosamente ao total de "GERADO", inflando esse grupo sem qualquer alerta. --> |
| 5 | Se não houver pagamentos no período, o sistema deve encerrar com RC 4. | Indesejada — `SE não houver pagamentos no período, ENTÃO o sistema DEVE encerrar com RC 4.` | `BATCHREL.NSP:203-206` | Inferida | — |
| 6 | O sistema deve gerar cópia de arquivamento do relatório, com retenção de 10 anos. | Ubíqua — `O sistema DEVE gerar cópia de arquivamento do relatório mensal.` | `BATCHREL.NSP:226-232`, cabeçalho `BATCHREL.NSP:19` | Inferida | Cabeçalho cita "10-YEAR RETENTION - LAW 8159 ART 14". |

---

## Regras de `CADBENEF.NSP` (cadastro de beneficiário)

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | Se a operação não for `I` ou `A`, o sistema deve recusar. | Indesejada — `SE a operação for diferente de I ou A, ENTÃO o sistema DEVE recusá-la.` | `CADBENEF.NSP:139-143` | Inferida | — |
| 2 | O sistema deve exigir CPF, nome, data de nascimento e sexo `M` ou `F`. | Ubíqua — `O sistema DEVE exigir CPF, nome, data de nascimento e sexo válidos.` | `CADBENEF.NSP:145-186` | Confirmada | Doc §1.1 RN-001, RN-006. |
| 3 | Se o CPF for inválido pela rotina interna, o sistema deve recusar o cadastro. | Indesejada — `SE o CPF for inválido, ENTÃO o sistema DEVE recusar o cadastro.` | `CADBENEF.NSP:152`, `CADBENEF.NSP:164-168` | Mistério | <!-- mystery: o programa chama SUBVALCP (:161) e IGNORA o resultado #PV-COD-RETURN; o IF que bloqueia (:164) testa #CPF-VALID, produzido pela rotina INTERNA (:344-414), que NÃO rejeita CPF com dígitos iguais. A chamada corporativa é decorativa. Comentário admite: "INTERNAL ROUTINE ABOVE RETAINED PENDING BENEFITS DEPARTMENT REVIEW". --> |
| 4 | Se o NIS for inválido, o sistema deve apenas emitir aviso e permitir a gravação. | Indesejada — `SE o NIS for inválido, ENTÃO o sistema DEVE avisar sem bloquear a gravação.` | `CADBENEF.NSP:196-202` | Mistério | <!-- mystery: doc §1.1 RN-001 exige NIS válido para inclusão. O código só avisa. Comentário: "WARNING MODE - DOES NOT BLOCK SAVING PENDING BENEFITS DEPARTMENT REVIEW" desde 2011. A revisão nunca ocorreu? --> |
| 5 | Se a operação for inclusão e o CPF já existir, o sistema deve recusar; se for alteração e não existir, também deve recusar. | Indesejada — `SE o CPF já existir em uma inclusão, ENTÃO o sistema DEVE recusá-la.` | `CADBENEF.NSP:213-223` | Mistério | <!-- mystery: doc §1.1 RN-002 diz que a unicidade vale só para CPF em situação ATIVA e que beneficiário excluído pode ser reincluído. O código bloqueia qualquer CPF existente, independentemente da situação. Reinclusão é impossível pela tela. --> |
| 6 | Em uma inclusão, o sistema deve atribuir situação inicial `A`. | Evento — `QUANDO um beneficiário for incluído, o sistema DEVE atribuir a situação A.` | `CADBENEF.NSP:245-247` | Inferida | — |
| 7 | Se a idade do beneficiário for maior que 75 anos, o sistema deve gravar a situação como `S` (suspenso). | Indesejada — `SE a idade for superior a 75 anos, ENTÃO o sistema DEVE ...` | `CADBENEF.NSP:250-252` | Mistério | <!-- mystery: regra não documentada em lugar nenhum. Aplica-se TAMBÉM na alteração (o IF está fora do IF de inclusão): qualquer alteração cadastral de pessoa com mais de 75 anos a suspende automaticamente, tornando-a inelegível em VALELEG:134-137. Mudança de telefone suspende o benefício de idoso. Intencional? --> |
| 8 | Se a validação cadastral corporativa reprovar, o sistema deve apenas avisar e gravar assim mesmo. | Indesejada — `SE a validação cadastral reprovar, ENTÃO o sistema DEVE avisar sem bloquear.` | `CADBENEF.NSP:263-269` | Inferida | `CALLNAT 'VALBENEF'` em modo aviso desde 2011. |
| 9 | Toda inclusão e alteração deve gerar registro de auditoria com ação `IN` ou `AL`. | Evento — `QUANDO um cadastro for incluído ou alterado, o sistema DEVE registrar auditoria.` | `CADBENEF.NSP:295-310`, `CADBENEF.NSP:318-332` | Confirmada | Doc §1.2 RN-010. Doc cita subprograma "LOGAUDIT", que **não existe**; o código usa o copycode `CCAUDIT`. |
| 10 | O sistema deve gravar o endereço informado (80 posições) em campo de 60 posições. | Ubíqua — `O sistema DEVE gravar o endereço no campo de logradouro.` | `CADBENEF.NSP:290-296` | Mistério | <!-- mystery: comentário do próprio código: "TICKET 4471/2003 - A80 ADDRESS SAVED IN A60 STREET FIELD - LAST 20 BYTES ARE LOST". Perda de dados conhecida e aberta desde 2003. --> |
| 11 | Na alteração, o sistema não deve atualizar data de nascimento, sexo, programa, região nem NIS. | Ubíqua — `O sistema DEVE preservar dados estruturais do beneficiário na alteração.` | `CADBENEF.NSP:313-330` | Mistério | <!-- mystery: os campos existem na tela e são lidos pelo INPUT, mas o ramo 'A' do DECIDE não os move para a view. O operador digita, o sistema aceita e descarta em silêncio. É regra de negócio (imutabilidade) ou defeito? --> |

---

## Regras de `CADDEPEN.NSP` (cadastro de dependentes)

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | Se o beneficiário titular estiver com situação `C` ou `D`, o sistema deve impedir a inclusão de dependentes. | Indesejada — `SE o titular estiver cancelado ou desligado, ENTÃO o sistema DEVE impedir a inclusão de dependentes.` | `CADDEPEN.NSP:110-113` | Inferida | Titular suspenso (`S`) ou inativo (`I`) **não** é bloqueado. |
| 2 | Se o titular já tiver mais de 5 dependentes, o sistema deve recusar novas inclusões. | Indesejada — `SE o titular já tiver atingido o limite de dependentes, ENTÃO o sistema DEVE recusar a inclusão.` | `CADDEPEN.NSP:117-120` | Mistério | <!-- mystery: doc §1.1 RN-004 e a matriz §7 fixam o máximo em 3, e a nota do próprio documento suspeita de alteração para 5 sem confirmação. O código testa > 5, o que permite chegar a 6 dependentes. O grupo PE da view aceita 10. Três limites diferentes: 3 (doc), 6 (efeito do código), 10 (estrutura). --> |
| 3 | O sistema deve exigir nome do dependente e grau de parentesco `FI`, `CO`, `IR` ou `OU`. | Ubíqua — `O sistema DEVE exigir nome e grau de parentesco válido para o dependente.` | `CADDEPEN.NSP:147-157` | Inferida | View documenta apenas FI/CO/IR (`CADDEPEN.NSP:21`); `OU` aparece só no código. |
| 4 | Se o CPF do dependente for inválido, o sistema deve apenas registrar mensagem e prosseguir com a inclusão. | Indesejada — `SE o CPF do dependente for inválido, ENTÃO o sistema DEVE avisar sem bloquear.` | `CADDEPEN.NSP:168-171` | Mistério | <!-- mystery: a mensagem é movida para #MSG e nunca exibida nem testada; #ERR não é marcado. O aviso é invisível ao operador. Modo "warning" declarado desde 2011, sem revisão. --> |
| 5 | Se o CPF do dependente já constar no grupo do titular, o sistema deve recusar a inclusão como duplicada. | Indesejada — `SE o dependente já estiver cadastrado, ENTÃO o sistema DEVE recusar a duplicidade.` | `CADDEPEN.NSP:174-184` | Inferida | Só compara CPF; dependente sem CPF nunca é detectado como duplicado. |
| 6 | Ao incluir um dependente, o sistema deve incrementar a quantidade de dependentes do titular e registrar auditoria. | Evento — `QUANDO um dependente for incluído, o sistema DEVE atualizar o contador e registrar auditoria.` | `CADDEPEN.NSP:190-213` | Inferida | O contador alimenta o fator familiar de `CALCBENF.NSN:207-221`. |
| 7 | O sistema deve descartar documento e sexo do dependente informados na tela. | Ubíqua — `O sistema DEVE ...` | `CADDEPEN.NSP:198-200` (bloco comentado) | Mistério | <!-- mystery: comentário: "TICKET 4471/2003 - DOC-DEPEND AND SEX-DEPEND DO NOT EXIST IN FILE 150. FIELDS REMAIN ON SCREEN BUT ARE NOT SAVED." Coleta de dado que nunca é gravado, aberto desde 2003. --> |

---

## Regras de `CADPROG.NSP` (cadastro de programa social)

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | Se o código do programa já existir, o sistema deve recusar a inclusão. | Indesejada — `SE o programa já existir, ENTÃO o sistema DEVE recusar a inclusão.` | `CADPROG.NSP:110-121` | Inferida | — |
| 2 | O sistema deve gravar o valor-base ajustado por um fator K igual a `1,00 + (fator_de_ajuste × 0,347215)`. | Ubíqua — `O sistema DEVE ajustar o valor-base pelo fator K antes de gravá-lo.` | `CADPROG.NSP:124-125` | Mistério | <!-- mystery: doc §6 lista "Fator K (multiplicador de cálculo) - Marcos Antônio não soube explicar - prioridade Alta". A constante 0,347215 não tem origem documentada. Efeito colateral grave: o valor digitado pelo gestor NÃO é o valor gravado, e CALCBENF (:258) multiplica esse valor já ajustado pelo mesmo FACTOR-ADJUST outra vez (:262). Duplo ajuste. --> |
| 3 | Todo programa incluído deve receber situação `A` (ativo), independentemente da data de encerramento informada. | Evento — `QUANDO um programa for incluído, o sistema DEVE atribuir a situação A.` | `CADPROG.NSP:134` | Mistério | <!-- mystery: a tela coleta DT-END (data de encerramento) e o valor é gravado, mas nenhum módulo lido compara DT-CLOSURE com a data corrente. Um programa com encerramento no passado continua ativo e pagando (VALELEG:114 só testa STAT-PROGRAM). --> |
| 4 | O sistema não deve oferecer alteração nem exclusão de programa social. | Ubíqua — `O sistema DEVE permitir apenas inclusão e consulta de programa social.` | `CADPROG.NSP:84-87` | Inferida | Operações aceitas: `I` e `C`. Alterar exige intervenção fora do sistema. |

---

## Regras de `CONSBENF.NSP` (consulta online)

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | O sistema deve permitir busca por CPF ou por NIS, assumindo CPF quando o tipo não for informado. | Opcional — `ONDE o tipo de busca não for informado, o sistema DEVE assumir busca por CPF.` | `CONSBENF.NSP:123-126`, `CONSBENF.NSP:147-166` | Inferida | — |
| 2 | Se o CPF informado for inválido, o sistema deve devolver a tela com a mensagem e o cursor no campo. | Indesejada — `SE o CPF informado for inválido, ENTÃO o sistema DEVE devolver a tela com a mensagem de erro.` | `CONSBENF.NSP:132-144` | Inferida | Único módulo em que `SUBVALCP` é a **única** verificação de CPF (comentário :129-131). |
| 3 | O sistema deve mascarar o CPF exibido no formato `***.***.XXX-XX`. | Ubíqua — `O sistema DEVE mascarar o CPF na exibição.` | `CONSBENF.NSP:295-312` | Mistério | <!-- mystery: comentário do código: "KNOWN INCONSISTENCY - SOMETIMES SHOWS THE FIRST 3 DIGITS INSTEAD OF THE LAST ONES. DO NOT CORRECT WITHOUT AUDIT APPROVAL." Quando o CPF tem menos de 11 dígitos, o ramo :300-303 expõe os 3 PRIMEIROS dígitos. Exposição de dado pessoal conhecida e deliberadamente não corrigida. --> |
| 4 | O sistema deve exibir no máximo os 12 pagamentos mais recentes do beneficiário. | Ubíqua — `O sistema DEVE exibir os 12 últimos pagamentos do beneficiário.` | `CONSBENF.NSP:270-284` | Inferida | Leitura `BY NUM-CPF`, sem ordenação por período: os "12 primeiros lidos" podem não ser os 12 mais recentes. |
| 5 | Toda consulta a dados de beneficiário deve gerar registro de auditoria com ação `CO`. | Evento — `QUANDO um cadastro for consultado, o sistema DEVE registrar auditoria de acesso.` | `CONSBENF.NSP:168-180` | Mistério | <!-- mystery: CCAUDIT nota 1 (:57-60) diz que ação 'CO' (consulta) NÃO deve ser gravada desde 2010 por volume (Port. CGTI 213/2010), e que o bloqueio é responsabilidade do chamador. CONSBENF grava assim mesmo, a cada consulta, dentro do loop. Contradição direta entre norma citada e implementação. --> |
| 6 | A trilha de auditoria deve ser gravada mesmo quando nenhum beneficiário for encontrado. | Evento — `QUANDO uma consulta for encerrada, o sistema DEVE registrar auditoria.` | `CONSBENF.NSP:147-180` | Mistério | <!-- mystery: o bloco de auditoria está FORA do DECIDE. Em caso de REINPUT o fluxo não chega lá; mas quando chega, #CPF-A11 pode conter o valor da consulta anterior (busca por NIS não o atualiza). Auditoria pode registrar o CPF errado. --> |

---

## Regras de `RELPGT.NSP` (relatório analítico de pagamentos)

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | Onde um código de programa for informado, o sistema deve filtrar o relatório por ele; zero significa todos. | Opcional — `ONDE o código de programa for informado, o sistema DEVE filtrar o relatório por ele.` | `RELPGT.NSP:135-139` | Inferida | — |
| 2 | O sistema deve emitir subtotal a cada mudança de programa e um total geral ao fim. | Evento — `QUANDO o código de programa mudar, o sistema DEVE emitir o subtotal do programa anterior.` | `RELPGT.NSP:142-150`, `RELPGT.NSP:225-231` | Mistério | <!-- mystery: quebra MANUAL por comparação com #PROG-PREV (comentário :140-141: "RETAINED SINCE 1997 - TICKET 2210/2004"), enquanto a leitura é ordenada por YEAR-MONTH-REF, não por COD-PROGRAM. Com mais de um programa no período, o mesmo programa quebra várias vezes e gera subtotais parciais repetidos. --> |
| 3 | O sistema deve mascarar o CPF no relatório exibindo os dígitos 4 a 11. | Ubíqua — `O sistema DEVE mascarar o CPF no relatório impresso.` | `RELPGT.NSP:163-168` | Mistério | <!-- mystery: a máscara oculta apenas os 3 primeiros dígitos e imprime os 8 restantes (***.456.789-01), enquanto CONSBENF oculta 6. Dois padrões de mascaramento no mesmo sistema; o do relatório impresso é o mais permissivo. --> |
| 4 | Se o beneficiário do pagamento não existir, o sistema deve imprimir a linha com nome em branco. | Indesejada — `SE o beneficiário não for encontrado, ENTÃO o sistema DEVE imprimir a linha sem o nome.` | `RELPGT.NSP:153-161` | Inferida | `IGNORE` no `NO RECORDS FOUND`; pagamento órfão não é sinalizado. |

---

## Regras de `RELAUDIT.NSP` (relatório da trilha de auditoria)

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | Onde a data inicial não for informada, o sistema deve assumir 01/01/1997; onde a final não for informada, deve assumir a data corrente. | Opcional — `ONDE o período não for informado, o sistema DEVE assumir 01/01/1997 até a data corrente.` | `RELAUDIT.NSP:103-108` | Inferida | 1997 é o ano de origem do sistema. |
| 2 | O sistema **nunca** deve exibir eventos de auditoria com ação `EX` (exclusão). | Ubíqua — `O sistema DEVE omitir do relatório os eventos de exclusão.` | `RELAUDIT.NSP:127-134` | Mistério | <!-- mystery: filtro incondicional, anterior a qualquer filtro do usuário, sem parâmetro para desligá-lo. O relatório oficial de auditoria (IN-TCU 63/2010) esconde exatamente as exclusões. Quem decidiu, e quando? O contador #QTY-FILTERED soma essas ocorrências com as filtradas pelo usuário, sem distinguir. --> |
| 3 | O sistema deve contabilizar eventos por tipo de ação: `IN` inclusão, `AL` alteração, `CO` conciliação, `CN` consulta, `DV` divergência. | Ubíqua — `O sistema DEVE contabilizar os eventos por tipo de ação.` | `RELAUDIT.NSP:163-182` | Mistério | <!-- mystery: RELAUDIT espera 'CN' para consulta, mas CONSBENF (:172) grava consulta como 'CO', e BATCHCON (:318) também grava 'CO' para conciliação. O contador de consultas fica sempre zerado e o de conciliações mistura consultas com conciliações. O relatório de auditoria está estruturalmente errado. --> |
| 4 | Onde a saída for impressora, o sistema deve emitir subtotal por dia e histograma de ocorrências por data. | Opcional — `ONDE a saída for impressora, o sistema DEVE emitir subtotal diário e histograma.` | `RELAUDIT.NSP:215-224`, `RELAUDIT.NSP:255-266` | Inferida | Saída em tela omite subtotais deliberadamente (ticket 7734/2017). |

---

## Regras de `CCAUDIT.NSC` (copycode — trilha de auditoria)

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | Todo módulo que altera dados deve gravar trilha de auditoria no arquivo 153. | Ubíqua — `O sistema DEVE registrar trilha de auditoria em toda alteração de dados.` | `CCAUDIT.NSC:7-10` | Confirmada | Doc §1.2 RN-010. Cabeçalho cita IN-TCU 63/2010. |
| 2 | O sistema deve obter a semente da sequência de auditoria uma única vez por execução, lendo o maior número existente. | Ubíqua — `O sistema DEVE gerar o número de auditoria a partir do maior número existente.` | `CCAUDIT.NSC:71-78` | Mistério | <!-- mystery: geração de chave por "ler o último + 1" sem bloqueio. Duas execuções simultâneas (ex.: CONSBENF online durante BATCHPGT) produzem o mesmo NUM-AUDIT. BATCHCON (:280) tenta contornar herdando #SEQ-AUDIT manualmente. Quantos registros de auditoria se perderam por chave duplicada? --> |
| 3 | O sistema deve truncar a hora do evento de `HHMMSST` para `HHMMSS` dividindo por 10. | Ubíqua — `O sistema DEVE gravar a hora do evento com precisão de segundos.` | `CCAUDIT.NSC:81-84` | Inferida | `*TIMN` devolve N7; o DDM armazena N6. |
| 4 | Onde a ação for `BT` (batch), o sistema deve preencher o nome do job e a situação do lote. | Opcional — `ONDE a ação for do tipo batch, o sistema DEVE registrar o job de origem.` | `CCAUDIT.NSC:97-101` | Inferida | — |
| 5 | O sistema não deve preencher o código de perfil do usuário na auditoria. | Ubíqua — `O sistema DEVE ...` | `CCAUDIT.NSC:62-63` | Mistério | <!-- mystery: nota 2 do copycode: "THE DDM FIELD COD-PROFILE IS NOT POPULATED BY THIS ROUTINE. TICKET 7742 - OPEN". Doc §1.2 RN-009 exige autorização de perfil SUPERVISOR para alterar CPF — sem o perfil na trilha, essa autorização não é auditável. --> |

---

## Regras consolidadas por shard

Seção mantida por máquina. O conteúdo entre os marcadores é regenerado por `python3 .github/scripts/merge_rules_shards.py` a partir de [`rules/`](rules/), populado por [`/fanout-rules`](../.github/prompts/stage-archaeologist-orchestrator-fanout-rules.prompt.md). As seções acima são escritas à mão e nunca são tocadas pelo merge.

> [!WARNING]
> Não edite regras dentro dos marcadores: a próxima consolidação as sobrescreve. Corrija o shard de origem em `rules/<PROGRAMA>.rules.json`.

<!-- rules:begin -->

> [!NOTE]
> Nenhum shard em [`rules/`](rules/) ainda. Execute `/fanout-rules` para gerar o primeiro lote.

<!-- rules:end -->

---

## Regras de `SIFAPJ01.jcl` e `SIFAPJ02.jcl` (agendamento e orquestração)

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | O sistema deve gerar a folha mensal no 1º dia útil do mês, às 22:00, em janela de 4 horas. | Evento — `QUANDO for o 1º dia útil do mês às 22:00, o sistema DEVE executar a geração da folha.` | `SIFAPJ01.jcl:14-21` | Confirmada | Doc §5.1 (1º dia útil, 22:00). Agendamento Control-M. |
| 2 | O sistema deve emitir os relatórios mensais no 2º dia útil, às 06:00, somente se a folha tiver terminado com RC 0. | Evento — `QUANDO a folha terminar com RC 0, o sistema DEVE liberar a emissão dos relatórios no 2º dia útil.` | `SIFAPJ02.jcl:14-22`, `SIFAPJ01.jcl:19-20` | Confirmada | Condição `SIFAP-PGT-OK`; predecessor `SIFAPJ01 (RC=0)`. |
| 3 | Se a folha for reexecutada, o sistema deve reprocessar o mesmo período informado, sem duplicar pagamentos. | Indesejada — `SE a folha for reexecutada, ENTÃO o sistema DEVE reprocessar o mesmo período sem duplicar pagamentos.` | `SIFAPJ01.jcl:27-37` | Mistério | <!-- mystery: o JCL garante que "BATCHPGT CHECKS WHETHER A PAYMENT ALREADY EXISTS FOR THE PERIOD AND DOES NOT CREATE A DUPLICATE", apoiado no superdescritor S1. Mas o duplo STORE de BATCHPGT:488 + CALCBENF:319 cria DOIS pagamentos na PRIMEIRA execução, antes de qualquer restart. A garantia de restart não cobre a duplicação intra-execução. --> |
| 4 | O sistema deve proibir a reexecução da folha com período diferente sem abertura de chamado. | Ubíqua — `O sistema DEVE exigir chamado formal para reprocessar a folha com período diferente.` | `SIFAPJ01.jcl:34-36` | Confirmada | Procedimento ITSM-SIFAP-002. |
| 5 | O sistema deve copiar o extrato de pagamento para a área de transmissão apenas quando a folha terminar com RC menor ou igual a 4. | Evento — `QUANDO a folha terminar com RC até 4, o sistema DEVE copiar o extrato para a área de transmissão.` | `SIFAPJ01.jcl:80-90` | Confirmada | `IEBGENER`, `COND=(4,LT,STEP010)`. |
| 6 | Se a folha falhar, o sistema deve acionar o passo de aviso de falha. | Indesejada — `SE a folha falhar, ENTÃO o sistema DEVE acionar o aviso de falha.` | `SIFAPJ01.jcl:92-97` | Confirmada | STEP030 executa com RC maior que 4. |
| 7 | O sistema deve arquivar a cópia do relatório consolidado por 10 anos. | Ubíqua — `O sistema DEVE reter a cópia do relatório consolidado por 10 anos.` | `SIFAPJ02.jcl:62-67` | Confirmada | Lei 8159, art. 14. Casa com `BATCHREL.NSP:19`. |
| 8 | O sistema deve enviar à coordenação (SENARC) duas cópias do resumo do relatório detalhado. | Evento — `QUANDO o relatório detalhado for emitido, o sistema DEVE enviar duas cópias à coordenação.` | `SIFAPJ02.jcl:92-94` | Mistério | <!-- mystery: o JCL aloca CMPRT02 (DEST=SENARC, COPIES=2), mas RELPGT.NSP:110 declara apenas DEFINE PRINTER (1). Nada é escrito na impressora lógica 2. A SENARC recebe 2 cópias de um relatório vazio desde 1999. O próprio JCL adverte sobre o erro inverso (:29-31), não sobre este. --> |

---

## Cruzamento com DDMs e FDT

Leitura de [BENEFIC.ddm](legacy-sifap/adabas-ddms/BENEFIC.ddm), [SOCPROG.ddm](legacy-sifap/adabas-ddms/SOCPROG.ddm), [PAYMENT.ddm](legacy-sifap/adabas-ddms/PAYMENT.ddm), [AUDIT.ddm](legacy-sifap/adabas-ddms/AUDIT.ddm) e [FDT-150-BENEFICIARY.txt](legacy-sifap/adabas-ddms/FDT-150-BENEFICIARY.txt). O cruzamento **resolveu** a dúvida sobre números de arquivo e **abriu** divergências novas de domínio de dados.

### Números de arquivo — resolvido

Os DDMs são autoritativos: **150 = BENEFIC**, **151 = SOCPROG**, **152 = PAYMENT**, **153 = AUDIT** (`BENEFIC.ddm:33`, `SOCPROG.ddm:21`, `PAYMENT.ddm:26`, `AUDIT.ddm:25`). Os JCLs confirmam: `SIFAPJ01.jcl:49` usa `FNR=150`, `SIFAPJ02.jcl:50` usa `FNR=152`.

Os **comentários dos programas estão errados** e apontam para arquivos que existem com outro conteúdo:

| Comentário do programa | Aponta para | O que 155 e 154 realmente são |
|---|---|---|
| `VALELEG.NSN:11` — "FILES 150/155" | 155 | **Partição histórica de auditoria 2005-2009** (`AUDIT.ddm:181-184`) |
| `CALCBENF.NSN:12` — "FILES 150/160/155" | 160 | Não existe DDM publicado |
| `BATCHCON.NSP:9` — "FILES 160/170" | 170 | Não existe DDM publicado |

<!-- mystery: os comentários de 6 programas citam os arquivos 155, 160 e 170 de forma consistente entre si, o que sugere uma numeração ANTERIOR à atual, não um erro de digitação. Houve renumeração de FNR em algum momento? Se sim, quando, e algum programa ainda aponta para o FNR antigo em tempo de execução (via DDM antigo em outra biblioteca)? -->

### Regras e divergências reveladas pelos DDMs

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | O sistema deve determinar a faixa de cálculo pela **renda per capita** do beneficiário. | Ubíqua — `O sistema DEVE aplicar a faixa correspondente à renda per capita.` | `BENEFIC.ddm:96-99`, `SOCPROG.ddm:71` | Mistério | <!-- mystery: CRÍTICO E FINANCEIRO. O DDM tem DOIS campos: CH AMT-FAMILY-INCOME (renda total declarada) e CJ IND-PERCAP-INCOME (renda per capita CALCULADA). Doc §2.2 RN-018 e §4.2 exigem a per capita (BN-VL-RENDA-PC). CALCBENF:174 e VALELEG:92 leem CH, a renda TOTAL, e a comparam contra MAX-PERCAP-INCOME (SOCPROG CA), um teto per capita. Família de 5 pessoas com renda total de 1.200 é tratada como se a renda per capita fosse 1.200 em vez de 240. Nenhum programa lido escreve ou lê CJ. --> |
| 2 | O sistema deve aplicar o fator regional pelo código de região do beneficiário, cujo domínio é `01` a `05` ou `99`. | Ubíqua — `O sistema DEVE aplicar o fator regional da macrorregião do beneficiário.` | `BENEFIC.ddm:80`, `SOCPROG.ddm:104-110` | Mistério | <!-- mystery: CRÍTICO E FINANCEIRO. O DDM define COD-REGION como MACRORREGIÃO (01-05 = N/NE/CO/SE/S, 99 = especial) e SOCPROG tem GRP-REGIONAL-PARAM (1:6) com FACTOR-REGIONAL por macrorregião. Mas LDASIFAP:38-47 e CALCBENF:99-125 indexam uma tabela de 27 posições por UF com esse código. Região 01 (Norte) recebe o fator do Acre (1,3500); região 05 (Sul) recebe o fator de Rondônia (1,3100). O fator regional está sistematicamente errado, e a tabela paramétrica do DDM nunca é lida. Note ainda que SOCPROG:105 ordena 3=CO 4=SE 5=S enquanto BATCHREL:145-165 usa 3=Sudeste 4=Sul 5=Centro-Oeste. --> |
| 3 | O sistema deve obter as faixas de cálculo do cadastro do programa social. | Ubíqua — `O sistema DEVE obter as faixas de cálculo do cadastro do programa.` | `SOCPROG.ddm:76-83` | Mistério | <!-- mystery: SOCPROG tem GRP-CALC-BAND (1:5) com INCOME-START, INCOME-END, FACTOR-MULTIPLIER, AMT-ADDITIONAL e IND-ACCUM — exatamente a parametrização descrita em doc §2.2 RN-017 ("até 10 faixas", aqui 5). CALCBENF:127-137 e BATCHPGT:236-246 ignoram o grupo e usam faixas CRAVADAS no código. Alterar a faixa de um programa pela tela não muda o cálculo. O campo AMT-ADDITIONAL é o "ACRESCIMO-DEPEND" da fórmula aditiva de RN-013 e nunca é lido. --> |
| 4 | O sistema deve respeitar o teto e o piso de benefício definidos pelo programa. | Ubíqua — `O sistema DEVE limitar o benefício ao teto e ao piso do programa.` | `SOCPROG.ddm:66-69` | Mistério | <!-- mystery: AMT-CEILING-BENEF (BC) e AMT-FLOOR-BENEF (BD) existem no cadastro do programa e NENHUM programa lido os consulta. O único limite aplicado no cálculo é o piso zero de CALCBENF:300. Benefício calculado acima do teto legal do programa é pago integralmente. --> |
| 5 | O sistema deve aplicar o fator K armazenado no cadastro do programa. | Ubíqua — `O sistema DEVE aplicar o fator K do programa no cálculo.` | `SOCPROG.ddm:55-60` | Mistério | <!-- mystery: o campo BG FACTOR-K existe no DDM, marcado ">>> UNDOCUMENTED <<< INSERTED AUG/2008 BY ADILSON - FULFILLS SENARC REQUEST - NO FURTHER DETAILS IN THE TICKET", e o cabeçalho do DDM adverte "DO NOT CHANGE WITHOUT AUTHORIZATION FROM BENEFITS COORDINATION". CADPROG:124 calcula um #FACTOR-K local com a constante 0,347215, aplica-o ao valor-base e **nunca grava BG**. O campo do banco e a constante do programa são coisas diferentes com o mesmo nome. Doc §6 lista o fator K como prioridade Alta e não explicado. --> |
| 6 | O sistema deve impedir pagamento a beneficiário com indicativo de óbito. | Indesejada — `SE o beneficiário tiver indicativo de óbito, ENTÃO o sistema DEVE impedir o pagamento.` | `BENEFIC.ddm:145-146` | Mistério | <!-- mystery: CRÍTICO. IA IND-DEATH ("S/N - SISOBI CROSS-CHECK 2001", descritor) e IB DT-DEATH existem desde 2001. NENHUM dos 24 membros lidos consulta IND-DEATH. BATCHPGT:263 filtra apenas STAT-BENEFICIARY = 'A'. Um beneficiário falecido cujo cruzamento SISOBI marcou IND-DEATH = 'S' mas cuja situação continua 'A' é pago normalmente. Quem transforma IND-DEATH em mudança de situação? --> |
| 7 | O sistema deve impedir pagamento a beneficiário com bloqueio administrativo ou judicial. | Indesejada — `SE o beneficiário estiver bloqueado, ENTÃO o sistema DEVE impedir o pagamento.` | `BENEFIC.ddm:147-149` | Mistério | <!-- mystery: IC COD-REASON-BLOCK (descritor), ID IND-JUDICIAL e IE NUM-CASE existem e não são lidos por nenhum módulo. Bloqueio por decisão judicial registrado no cadastro não impede a folha. --> |
| 8 | O sistema deve controlar concorrência de atualização cadastral por número de versão. | Ubíqua — `O sistema DEVE controlar a concorrência das atualizações cadastrais.` | `BENEFIC.ddm:130` | Mistério | <!-- mystery: GG NUM-VERSION está documentado como "CONCURRENCY CONTROL". CADBENEF:318 e CADDEPEN:203 executam UPDATE sem ler nem incrementar o campo. Duas telas alterando o mesmo beneficiário sobrescrevem-se em silêncio (lost update). --> |
| 9 | O sistema deve registrar o grau de parentesco do dependente entre `FI`, `CJ`, `NT` e `TU`. | Ubíqua — `O sistema DEVE aceitar somente os graus de parentesco definidos no cadastro.` | `BENEFIC.ddm:110-111` | Mistério | <!-- mystery: os domínios não têm interseção além de FI. DDM: FI=filho, CJ=cônjuge, NT=neto, TU=tutelado. CADDEPEN:152-156 aceita FI, CO, IR e OU. O comentário da view no próprio CADDEPEN:21 diz "FI=CHILD CO=SPOUSE IR=SIBLING", um TERCEIRO domínio. Valores CO/IR/OU gravados hoje são inválidos segundo o cadastro; CJ/NT/TU nunca são gravados. --> |
| 10 | O sistema deve manter a situação de cada dependente e considerar apenas os ativos na contagem. | Estado — `ENQUANTO o dependente não estiver ativo, o sistema DEVE excluí-lo da contagem.` | `BENEFIC.ddm:112`, `BENEFIC.ddm:99` | Mistério | <!-- mystery: DF STAT-DEPEND (A=ativo I=inativo D=desligado) existe e CADDEPEN:190-202 nunca o preenche — dependentes entram com situação em branco. CK QTY-DEPEND é documentado como "ACTIVE DEPENDENT COUNT", mas CADDEPEN:192 apenas incrementa o contador sem filtrar situação. Esse contador alimenta o fator familiar de CALCBENF:207-221 e a regra de elegibilidade de VALELEG:261. Dependente desligado continua aumentando o benefício. --> |
| 11 | O sistema deve registrar o representante legal de beneficiário sob curatela. | Opcional — `ONDE houver curatela, o sistema DEVE registrar o representante legal.` | `BENEFIC.ddm:152-154`, `FDT-150-BENEFICIARY.txt:44-79` | Mistério | <!-- mystery: JA IND-LEGAL-REPRESENTATIVE e JB CPF-REPRESENTATIVE constam no DDM (adicionados em 2009) mas NÃO EXISTEM na FDT física, cuja última alteração é de 19/11/2015. O DDM descreve campos que o arquivo não tem. Qualquer acesso a eles falha em tempo de execução. O campo IG COD-PAYER-AGENCY também está no DDM e na FDT, mas nenhum módulo o usa. --> |
| 12 | O sistema deve registrar ações de auditoria com códigos do domínio `IN`, `AL`, `EX`, `CO`, `LG`, `LO`, `BT`, `ER`, `AU` e `RE`. | Ubíqua — `O sistema DEVE registrar a auditoria com um código de ação válido.` | `AUDIT.ddm:38-48` | Confirmada | Confirma o mistério do código `CO`: o domínio oficial define `CO` = **consulta**. Os códigos `DV` (`BATCHCON.NSP:318`) e `CN` (`RELAUDIT.NSP:173`) **não existem** no domínio. |
| 13 | O sistema deve reter a auditoria por no mínimo 10 anos, sem alteração nem exclusão de registros. | Ubíqua — `O sistema DEVE manter a trilha de auditoria imutável por 10 anos.` | `AUDIT.ddm:14-18` | Confirmada | IN-TCU 63/2010 e Lei 8159, art. 14. |
| 14 | O relatório de auditoria deve cobrir os eventos desde 01/01/1997. | Ubíqua — `O sistema DEVE permitir consultar a auditoria desde a origem do sistema.` | `RELAUDIT.NSP:103-105`, `AUDIT.ddm:180-184` | Mistério | <!-- mystery: RELAUDIT assume 19970101 como data inicial padrão, mas lê apenas o FNR 153. O DDM nota 3 informa que a auditoria é PARTICIONADA: FNR 154 (2010-2014), 155 (2005-2009) e 156 (1997-2004), "AND HAVE NO DDM PUBLISHED IN LIB SIFAPPRD". O relatório oferece 1997 e devolve apenas o que estiver na partição corrente, sem avisar. --> |

### Confirmações trazidas pelos DDMs

O cruzamento **promoveu a Confirmada** o que antes era inferência:

- **Tipos de desconto**: `PAYMENT.ddm:47` e `SOCPROG.ddm:88-93` definem o domínio `IR/JD/CS/PA/EM/TX/OU/EX` em campo `A3`, em dois arquivos independentes. `CALCDSCT.NSP:127-167` compara com `C/I/J/S/P/A` em `A1`. A divergência da regra 8 de `CALCDSCT` está **confirmada nos dois lados**: se os dados seguem o cadastro, todo desconto itemizado cai no ramo `IGNORE`.
- **Domínio de situação do beneficiário**: `BENEFIC.ddm:92-93` fixa `A/S/C/I/D`, confirmando `VALBENEF.NSN:174-179` e refutando o `'E'` de doc §1.3 RN-011.
- **Limite físico de dependentes**: `BENEFIC.ddm:104` e `FDT-150-BENEFICIARY.txt:98` fixam o grupo periódico em **10 ocorrências**. O limite de negócio (3 no documento, 6 no código) é menor que o limite físico — nenhum dos dois é imposto pela estrutura.
- **Dependente sem CPF é previsto**: `BENEFIC.ddm:108` define `CPF-DEPEND` como "CPF OR 00000000000", confirmando que a verificação de duplicidade de `CADDEPEN.NSP:174-184` deixa de fora um caso esperado por projeto.
- **Dados bancários existem**: `BENEFIC.ddm:136-141` tem `COD-BANK`, `COD-BRANCH`, `NUM-ACCOUNT` e `TYPE-ACCOUNT`, confirmando doc §1.1 RN-007. `CADBENEF.NSP:117-136` **não coleta nenhum deles**: a tela de cadastro não tem campos bancários.

---

## Divergências entre implementações de CPF

Quatro rotinas de validação de CPF coexistem, com **comportamentos diferentes** para a mesma entrada. O copycode padrão adverte explicitamente que as cópias não são equivalentes (`CCVALCPF.NSC:29-34`, ticket 6620/2011 aberto).

| Implementação | Rejeita não numérico | Rejeita dígitos iguais | Exceção de prefixo | Cálculo do resto | Quem usa |
|---|---|---|---|---|---|
| `CCVALCPF.NSC:38-129` (padrão) | Sim (`MASK`) | Sim, sempre | Nenhuma | `DIVIDE ... REMAINDER` | `SUBVALCP`, `CADDEPEN` |
| `VALBENEF.NSN:196-281` | Sim (`NONE VALUE`) | Sim, **exceto prefixo `000`** | `000` → válido | `#SUM - ((#SUM / 11) * 11)` | `CADBENEF` (modo aviso) |
| `VALDOCS.NSP:137-205` | Sim (`NONE VALUE`) | **Não verifica** | 8 prefixos zeram todos os erros | `#SUM - ((#SUM / 11) * 11)` | `VALDOCS` |
| `CADBENEF.NSP:344-414` | Sim (`NONE VALUE`) | **Não verifica** | Nenhuma | `#SUM - ((#SUM / 11) * 11)` | `CADBENEF` (bloqueante) |

<!-- mystery: o CPF 11111111111 é REJEITADO por SUBVALCP e ACEITO por CADBENEF (rotina interna, que é a que bloqueia). O CPF 00000000000 é rejeitado por SUBVALCP:56, aceito por VALBENEF:239 e aceito por VALDOCS:233. Qual comportamento é o correto para o negócio? Quantos registros do arquivo 150 têm CPF que falharia na rotina padrão? -->

---

## Divergências entre código e documentação de 2012

Confronto sistemático com [BUSINESS-RULES-2012.md](legacy-sifap/legacy-docs/BUSINESS-RULES-2012.md). **Nenhuma destas divergências foi resolvida** — todas devem ser levadas à validação humana.

| Doc 2012 | Código | Evidência |
|---|---|---|
| RN-004: máximo de **3** dependentes | Teste `> 5`, permitindo 6 | `CADDEPEN.NSP:117` |
| RN-013: fórmula **aditiva** (`base + acréscimo × dependentes`) | Produto de 5 fatores | `CALCBENF.NSN:258-262` |
| RN-021: descontos acima de 30% são **rejeitados**, benefício sem descontos | Total é **truncado** no teto e mantido | `CALCDSCT.NSP:170-174` |
| RN-023: descarte por **ordem de prioridade** dos descontos | Sem prioridade; ordem do grupo PE | `CALCDSCT.NSP:113-177` |
| RN-011: exclusão lógica usa situação `'E'` | Domínio aceito é `A/S/C/I/D` | `VALBENEF.NSN:174-179` |
| RN-002: unicidade só para CPF **ativo**; reinclusão permitida | Bloqueia qualquer CPF existente | `CADBENEF.NSP:213-217` |
| RN-001: subprogramas `VALCPF` e `VALNISN` | Módulos chamam-se `SUBVALCP` e `SUBVALNI` | `SUBVALCP.NSN:1`, `SUBVALNI.NSN:1` |
| RN-010: auditoria pelo subprograma `LOGAUDIT` | Copycode `CCAUDIT`; `LOGAUDIT` não existe | `CCAUDIT.NSC:1` |
| §5.1: ordenação **alfabética por nome** | `READ ... BY NUM-CPF` | `BATCHPGT.NSP:250` |
| §5.1: pagamento gravado com situação `'P'` | Grava `'G'` | `CALCBENF.NSN:317`, `BATCHPGT.NSP:483` |
| §5.2: aborta com `ABEND U4038` acima de 100 erros | Não existe limite de erros | `BATCHPGT.NSP:558-566` |
| §3: módulo `CALCDSCT` implementado em **2015** | Cabeçalho registra criação em **1999** | `CALCDSCT.NSP:5` |
| RN-018: faixa determinada pela renda **per capita** (`BN-VL-RENDA-PC`) | Usa a renda familiar **total** | `CALCBENF.NSN:174`, `BENEFIC.ddm:96-99` |
| RN-017: faixas **parametrizadas** no cadastro do programa | Faixas cravadas no código; `GRP-CALC-BAND` nunca lido | `CALCBENF.NSN:127-137`, `SOCPROG.ddm:76-83` |
| RN-005: região válida de **01 a 27** (estados + DF) | DDM define **01 a 05** (macrorregiões) + 99; código indexa tabela de 27 UFs | `BENEFIC.ddm:80`, `CALCBENF.NSN:200-205` |
| §1.1 RN-007: dados bancários obrigatórios e validados | Campos existem no cadastro; a tela não os coleta e nenhum módulo os valida | `BENEFIC.ddm:136-141`, `CADBENEF.NSP:117-136` |
| §4.2: máximo de 2 programas simultâneos (`BN-QT-PROG`) | Campo não existe no DDM nem em view alguma; regra ausente | `BENEFIC.ddm:36-154`, `VALELEG.NSN:15-60` |
| §4.2: bloqueio por atualização cadastral acima de 24 meses | Regra ausente do código, embora `DT-LAST-UPDATE` exista | `BENEFIC.ddm:127`, `VALELEG.NSN:110-227` |
| §4.2: bloqueio por ocorrência de auditoria tipo `'B'` | Regra ausente; o domínio de `COD-ACTION` não tem `'B'` | `AUDIT.ddm:38-48`, `VALELEG.NSN:110-227` |

> [!IMPORTANT]
> A dúvida sobre **números de arquivo está resolvida** pelos DDMs e pelos JCLs — 150/151/152/153. Os comentários de cabeçalho de 6 programas citam 155, 160 e 170, e o **155 é uma partição histórica de auditoria**. Detalhes em [Números de arquivo — resolvido](#números-de-arquivo--resolvido).

---

## Resumo geral

| Métrica | Valor |
|---|---:|
| Membros Natural lidos | 24 de 24 |
| DDMs cruzados | 4 de 4 + listagem FDT do arquivo 150 |
| Regras candidatas extraídas | 133 |
| Regras confirmadas | 27 |
| Regras inferidas | 61 |
| Mistérios | 45 |
| Divergências código × doc 2012 | 20 |
| Divergências código × DDM | 11 |
| Implementações divergentes de CPF | 4 |

> [!WARNING]
> Os 45 mistérios acima **não estão resolvidos** e não devem ser tratados como decididos no Estágio 2. Cada um precisa virar registro em [`mysteries-found.md`](mysteries-found.md) via `/catalog-mysteries`, com evidência `path:line`, impacto, hipótese não confirmada, responsável e status. A dupla deve escolher os **4 mistérios canônicos** conforme [`mysteries-checklist.md`](mysteries-checklist.md).

### Mistérios com impacto financeiro direto

Os quatro que alteram valor pago e, por isso, são os candidatos mais fortes a mistério canônico:

| Mistério | Efeito | Evidência |
|---|---|---|
| Pagamento gravado duas vezes | Dois registros por CPF/período na mesma execução | `CALCBENF.NSN:319`, `BATCHPGT.NSP:488` |
| Renda total usada como renda per capita | Faixa e teto de elegibilidade errados para toda família com mais de 1 membro | `CALCBENF.NSN:174`, `BENEFIC.ddm:96-99` |
| Código de macrorregião indexando tabela de UF | Fator regional errado para toda a base | `BENEFIC.ddm:80`, `CALCBENF.NSN:200-205` |
| Beneficiário com indicativo de óbito não é bloqueado | Pagamento a falecido com situação ainda `A` | `BENEFIC.ddm:145-146`, `BATCHPGT.NSP:263` |

---

## Definição de pronto

- [x] Todo bloco condicional dos programas atribuídos foi examinado.
- [x] Toda regra cita `arquivo:linha`.
- [x] Os 24 membros Natural foram lidos.
- [x] Os 4 DDMs e a listagem FDT foram cruzados.
- [ ] Toda questão em aberto está registrada em `mysteries-found.md` sem conclusão. — **pendente**: os 45 marcadores `<!-- mystery: -->` deste catálogo ainda não foram transcritos para [`mysteries-found.md`](mysteries-found.md). Execute `/catalog-mysteries`.

---

### Continue lendo

| Anterior | Próximo |
|---|---|
| [Inventário](inventory.md)<br/><sub>Passo 1 — varredura de arquivos.</sub> | [Mapa de Dependências](dependency-map.md)<br/><sub>Passo 3 — grafo de chamadas e acessos.</sub> |

<sub>[Voltar ao índice do kit](../README.md)</sub>
