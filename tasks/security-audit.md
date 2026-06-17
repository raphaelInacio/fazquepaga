# Relatório de Auditoria de Segurança (Pentest) - FazQuePaga

**Data da Auditoria:** 17 de Junho de 2026
**Status:** ✅ RESOLVIDO (Verifique `tasks/security-audit-report.md` para detalhes das correções)

---

## 🔴 FALHAS CRÍTICAS (Ação Imediata)

### 1. Exposição de Chaves de API e Segredos
- **Descrição:** Chaves reais do Gemini e Asaas estão hardcoded no arquivo `backend/src/main/resources/application-cloud-local.properties`. O arquivo `.env` do frontend contendo chaves do Firebase também está commitado.
- **Impacto:** Roubo de recursos (créditos AI), fraude financeira e acesso total ao banco de dados.
- **Arquivos:** `backend/src/main/resources/application-cloud-local.properties`, `frontend/.env`.

### 2. Injeção de Identidade (Auth Spoofing) no Frontend
- **Descrição:** O cliente de API injeta o cabeçalho `X-User-Email` do localStorage como fallback se não houver token. Qualquer usuário pode forjar a identidade de outro apenas trocando um valor no localStorage.
- **Impacto:** Acesso total a qualquer conta de usuário sem senha.
- **Arquivo:** `frontend/src/lib/api.ts`.

### 3. Vulnerabilidade IDOR Sistêmica (Insecure Direct Object Reference)
- **Descrição:** Quase todos os controllers (Task, Identity, Allowance) aceitam IDs de recursos (parent_id, child_id) via parâmetros e não validam se o usuário autenticado é o dono do recurso.
- **Impacto:** Um usuário pode visualizar, modificar ou excluir dados de qualquer outro usuário.
- **Arquivos:** `TaskController.java`, `IdentityController.java`, `AllowanceController.java`.

### 4. Condição de Corrida (Race Condition) no Saldo e Créditos
- **Descrição:** `LedgerService.addTransaction` e `WithdrawalService.requestWithdrawal` não usam transações atômicas. Múltiplas requisições simultâneas permitem burlar o saldo (Negative Balance Bypass).
- **Impacto:** Fraude financeira massiva.
- **Arquivos:** `LedgerService.java`, `WithdrawalService.java`.

---

## 🟠 FALHAS DE SEVERIDADE ALTA

### 5. Vazamento de Dados Sensíveis (PII & Credentials)
- **Descrição:** A entidade `User` é retornada diretamente na API sem `@JsonIgnore` em campos como `password` (hash) e `accessCode`. PII também é armazenado desprotegido no localStorage.
- **Impacto:** Exposição de credenciais e dados pessoais.
- **Arquivos:** `User.java`, `AuthContext.tsx`.

### 6. Ausência de Proteção de Rotas no Frontend
- **Descrição:** Não existem `ProtectedRoute` ou Guards de rota. A verificação de auth é feita apenas dentro dos componentes, sendo facilmente burlável.
- **Impacto:** Acesso à interface administrativa/pessoal por usuários deslogados.
- **Arquivo:** `frontend/src/App.tsx`.

### 7. Configuração Insegura de Webhooks e CORS
- **Descrição:** Webhook do Asaas com CORS aberto (*) e validação baseada apenas em token estático fraco exposto no código.
- **Impacto:** Injeção de eventos falsos de pagamento.
- **Arquivo:** `AsaasWebhookController.java`, `SecurityConfig.java`.

---

## 🟡 FALHAS DE SEVERIDADE MÉDIA

### 8. Erro na Implementação de Autenticação JWT
- **Descrição:** `JwtService` tenta decodificar o segredo como Base64, mas o valor no properties está em Hex. Logout de crianças não limpa o JWT do localStorage.
- **Impacto:** Falhas intermitentes de login e sessões zumbis.
- **Arquivos:** `JwtService.java`, `childAuthService.ts`.

### 9. Dependências e Versões Suspeitas
- **Descrição:** Uso de versões futuras/instáveis de Spring Boot (3.5.7) e bibliotecas desatualizadas (JJWT).
- **Impacto:** Instabilidade e vulnerabilidades conhecidas em bibliotecas antigas.
- **Arquivo:** `backend/pom.xml`.

---

## 🟢 BAIXA / INFORMATIVO
- **Ordenação de Filtros:** O `RateLimitFilter` roda antes do `JwtAuthenticationFilter`, o que impede a identificação do usuário para limites específicos por conta.
- **Logs de Erro:** Exposição de objetos Axios completos no console em produção.

---

## 📋 BACKLOG DE MITIGAÇÃO RECOMENDADO

1. **[PATCH-001]** Remover segredos hardcoded e mover para variáveis de ambiente (Github Secrets/Vault).
2. **[PATCH-002]** Remover a injeção do header `X-User-Email` no frontend.
3. **[PATCH-003]** Implementar validação de Ownership em todos os controllers (verificar SecurityContext).
4. **[PATCH-004]** Refatorar `LedgerService` para usar `firestore.runTransaction()` ou `FieldValue.increment()`.
5. **[PATCH-005]** Adicionar `@JsonIgnore` nos campos sensíveis da entidade `User`.
6. **[PATCH-006]** Implementar `ProtectedRoute` no frontend.
7. **[PATCH-007]** Corrigir decodificação JWT e fluxo de logout.
