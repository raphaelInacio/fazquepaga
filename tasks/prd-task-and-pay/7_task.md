---
status: pending
---

# Tarefa 7.0: Integração com Frontend (Web App dos Pais)

## Visão Geral

Esta tarefa representa o ponto de conexão entre o backend e a interface do usuário (frontend). Embora o desenvolvimento do frontend em si esteja fora do escopo deste conjunto de tarefas, é crucial garantir que a API do backend seja consumível, bem documentada e atenda às necessidades da aplicação web dos pais.

**LEITURA OBRIGATÓRIA**: Antes de iniciar, revise as regras do projeto em `docs/ai_guidance/rules/`.

## Requisitos

-   Validar que todos os endpoints da API necessários para a interface do usuário dos pais estão implementados e funcionais.
-   Garantir que a API tenha uma documentação clara para facilitar o consumo pelo time de frontend.
-   Configurar o CORS (Cross-Origin Resource Sharing) na API do backend para permitir requisições do domínio do frontend.
-   Realizar testes de ponta a ponta (manuais ou automatizados) para validar os principais fluxos de usuário.

## Subtarefas

- [ ] 7.1 Gerar documentação da API usando uma ferramenta como o SpringDoc (que gera uma especificação OpenAPI/Swagger).
- [ ] 7.2 Configurar o CORS na aplicação Spring Boot, permitindo o acesso do domínio onde o frontend será hospedado.
- [ ] 7.3 Realizar uma sessão de "dogfooding" ou teste manual completo dos fluxos da API usando uma ferramenta como o Postman ou Insomnia.
- [ ] 7.4 Simular o fluxo completo:
    -   Registrar um pai.
    -   Adicionar um filho.
    -   Criar uma tarefa para o filho.
    -   Obter sugestões de tarefas da IA.
    -   Simular o envio de comprovação via WhatsApp.
    -   Verificar a atualização da tarefa após a validação da IA.
    -   Aprovar a tarefa.
- [ ] 7.5 Criar uma coleção Postman com exemplos de requisições para todos os endpoints e compartilhá-la com a equipe de frontend.

## Detalhes da Implementação

A configuração do CORS é uma anotação simples ou uma configuração global no Spring Security/WebMVC. A geração de documentação OpenAPI pode ser feita adicionando a dependência `springdoc-openapi-ui` e configurando-a conforme necessário.

### Arquivos Relevantes

-   `WebConfig.java` ou `SecurityConfig.java` (para configuração de CORS)
-   `pom.xml` ou `build.gradle` (para adicionar dependência do SpringDoc)

## Critérios de Sucesso

-   A documentação da API está acessível em um endpoint (ex: `/swagger-ui.html`) e descreve todos os endpoints corretamente.
-   As requisições feitas de um cliente web (simulado) para a API não são bloqueadas por erros de CORS.
-   Uma coleção Postman é criada e valida que todos os endpoints funcionam conforme o esperado.
-   O time de frontend confirma que a API atende às suas necessidades.
-   O código é revisado e aprovado.
