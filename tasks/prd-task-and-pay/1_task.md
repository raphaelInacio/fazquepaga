---
status: completed
---

# Tarefa 1.0: Configuração do Projeto e Ambiente de Desenvolvimento

## Visão Geral

Esta tarefa fundamental envolve a criação da estrutura inicial do projeto para o monólito modular e a configuração do ambiente de desenvolvimento local usando Docker. O objetivo é ter um ponto de partida limpo e um ambiente de desenvolvimento consistente e fácil de replicar para todos os desenvolvedores.

**LEITURA OBRIGATÓRIA**: Antes de iniciar, revise as regras do projeto em `docs/ai_guidance/rules/`.

## Requisitos

-   Criar a estrutura de diretórios para o projeto Java/Spring Boot.
-   Configurar um arquivo `docker-compose.yml` para orquestrar a aplicação e os serviços de emulador.
-   O ambiente Docker deve incluir a aplicação, o emulador do Firestore e o emulador do Pub/Sub.
-   Fornecer um `README.md` com instruções claras sobre como iniciar o ambiente de desenvolvimento.

## Subtarefas

- [ ] 1.1 Inicializar um novo projeto Spring Boot com as dependências necessárias (Web, Lombok, Firestore, Pub/Sub).
- [ ] 1.2 Criar a estrutura de pacotes para os módulos: `identity`, `tasks`, `allowance`, `ai`, `whatsapp`, `shared`.
- [ ] 1.3 Criar um `Dockerfile` para a aplicação Java.
- [ ] 1.4 Criar o arquivo `docker-compose.yml` definindo os serviços da aplicação, do emulador do Firestore e do emulador do Pub/Sub.
- [ ] 1.5 Adicionar um `README.md` na raiz do projeto com a seção "Como Rodar Localmente".
- [ ] 1.6 Implementar testes unitários básicos para garantir que a configuração do projeto está correta e o contexto do Spring Boot carrega.

## Detalhes da Implementação

Conforme a seção "Ambiente de Desenvolvimento" da especificação técnica, o `docker-compose.yml` é a peça central desta tarefa.

### Arquivos Relevantes

-   `pom.xml`
-   `Dockerfile`
-   `docker-compose.yml`
-   `README.md`
-   `src/main/java/com/fazquepaga/...`

## Critérios de Sucesso

-   Um novo desenvolvedor consegue clonar o repositório e executar `docker-compose up` para ter um ambiente funcional.
-   A aplicação Spring Boot inicia sem erros no contêiner.
-   Os emuladores do Firestore e Pub/Sub estão acessíveis a partir do contêiner da aplicação.
-   O código é revisado e aprovado.
-   Todos os testes passam.
