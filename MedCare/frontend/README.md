# MedCare — Primeira sprint

Protótipo visual em HTML, CSS e JavaScript puro, sem dependências. Preserva as telas de Pacientes, Profissionais, Consultas, Internações, Quartos e Histórico, com dados fictícios estáticos diretamente no HTML.

## Executar

Abra `assents/templates/index.html` no navegador. Opcionalmente, na raiz `MedCare`:

```bash
python3 -m http.server 8080 --bind 127.0.0.1 --directory frontend
```

Acesse http://localhost:8080/assents/templates/index.html.

## Escopo

- Menu para navegar entre as seis telas.
- “Visualizar formulário” expande a prévia de cada formulário.
- Tabelas, ocupação, vagas e histórico são exemplos fixos.
- Campos de formulário, busca, seleção de paciente e botões de operação estão desativados e são apenas visuais.
- Layout responsivo, com rolagem interna nas tabelas.

Não há cadastro, edição, exclusão, busca, envio de formulários, validação de negócio, persistência, atualização de dados ou integração com API. Dados salvos pela versão anterior no navegador não são lidos nem modificados.

## Arquivos

- `assents/templates/index.html`: todas as telas, tabelas e formulários estáticos.
- `assents/css/style.css`: layout e aparência responsiva.
- `assents/js/app.js`: somente navegação por hash e indicação da tela ativa.

Foram removidos `data.js`, `repository.js` e `tests/repository.test.cjs`, que pertenciam à demonstração funcional anterior. Nenhum arquivo do back-end foi alterado.

## Conferência manual

Navegue pelas seis áreas e expanda os cinco formulários. Confira que todos os controles de operação estão desativados. Em uma janela pequena, verifique a rolagem horizontal das tabelas e os campos em uma coluna. Voltar e avançar do navegador devem acompanhar a navegação entre as telas.
