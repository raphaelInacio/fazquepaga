# Bugs Identificados - Gift Card Integration

## 1. Loop de Redirecionamento no Login de Dependentes (CRÍTICO)
- **Descrição:** O `ChildLogin.tsx` não utiliza o `AuthContext.login()`, o que deixa o estado global de autenticação como `null`.
- **Severidade:** Alta (Bloqueante)
- **Status:** Corrigido.
- **Correção Aplicada:** O componente `ChildLogin.tsx` agora chama corretamente `login(token, childData, refreshToken)` do `AuthContext`.

## 2. Incompatibilidade de LocalStorage entre ChildAuthService e AuthProvider (MÉDIO)
- **Descrição:** `childAuthService` salva os dados do dependente na chave `fazquepaga_child`, enquanto o `AuthProvider` busca apenas na chave `user`. 
- **Severidade:** Média
- **Status:** Corrigido.
- **Correção Aplicada:** `childAuthService` foi atualizado para utilizar a chave `user`, garantindo compatibilidade com o `AuthProvider`.

## 3. Acesso Negado ao carregar dados do Dependente (ALTA)
- **Descrição:** O portal do dependente tenta atualizar os dados chamando o endpoint `/api/v1/children/{id}`, que está restrito a usuários com role `PARENT`.
- **Severidade:** Alta
- **Status:** Corrigido.
- **Correção Aplicada:** O `IdentityController.java` foi atualizado para permitir que o próprio dependente acesse seus dados.

## 4. Falha de Permissão no Firestore (BLOQUEANTE DE INFRA)
- **Descrição:** O backend está configurado para um projeto GCP (`gen-lang-client-0807030077`) onde a API do Firestore não está habilitada.
- **Severidade:** Crítica (Bloqueante)
- **Status:** Pendente de Infra.
- **Ação Recomendada:** Habilitar a API no GCP ou configurar o uso do Firestore Emulator localmente.

## 5. Falta de Traduções (BAIXA)
- **Descrição:** Diversas chaves de tradução no portal do dependente e na loja de Gift Cards estavam aparecendo como raw strings ou strings hardcoded.
- **Severidade:** Baixa
- **Status:** Corrigido.
- **Correção Aplicada:** Removidas strings hardcoded em `GiftCardStorePage.tsx` e adicionadas chaves faltantes (`redeem`, `requestedAt`, `requesting`, `priceLabel`) em `pt.json` e `en.json`.

## 6. Verificação de JWT exige Segredo em Hexadecimal (MÉDIO)
- **Descrição:** O `JwtService.java` utilizava `HexFormat.of().parseHex(secret)`, o que obriga que a variável de ambiente `JWT_SECRET` seja uma string hexadecimal válida.
- **Severidade:** Média
- **Status:** Corrigido.
- **Correção Aplicada:** O código foi atualizado para utilizar `Decoders.BASE64.decode(secret)`, seguindo o padrão da biblioteca jjwt.
