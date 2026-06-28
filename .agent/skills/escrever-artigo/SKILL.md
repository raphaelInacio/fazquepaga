---
name: escrever-artigo
description: Gera e otimiza artigos de blog em HTML para o blog do TaskAndPay em português do Brasil. Aplica regras rígidas de SEO e Answer Engine Optimization (AEO/AGO) como caixas de Resposta Rápida, FAQ visual e marcação JSON-LD FAQPage. Atualiza automaticamente o índice de blogs após a criação.
---

# Procedimento de Escrita de Artigos (TaskAndPay)

Siga este procedimento passo a passo para criar novos artigos de blog para a plataforma TaskAndPay.

## Passo 1: Informações Básicas do Artigo
Defina as seguintes variáveis antes de iniciar a escrita do conteúdo:
- **Título**: Amigável, atraente, com a palavra-chave principal (ex: "Mesada infantil: como definir o valor certo para cada idade").
- **Meta Description**: 150-160 caracteres contendo a palavra-chave e um Call-to-Action.
- **Slug**: Kebab-case, sem acentos ou caracteres especiais (ex: `como-definir-valor-certa-da-mesada`).
- **Imagem de Capa**: URL real do Unsplash relacionada ao tema (formato: `https://images.unsplash.com/photo-XXXXXXXXX?auto=format&fit=crop&q=80&w=800`).
- **Data de Publicação**: No formato "DD MMM AAAA" (ex: "28 Jun 2026").

## Passo 2: Estruturação de Conteúdo Completo (Mínimo 1200 palavras)
Escreva o texto real do artigo (dentro da tag de conteúdo principal) contendo:
1.  **Introdução envolvente**: Apresentando a dor dos pais ou a curiosidade das crianças.
2.  **Seção de Destaque AEO**: Uma caixa visual com a resposta rápida à pergunta principal do post:
    ```html
    <div class="bg-purple-50 border-l-4 border-primary p-4 my-6 rounded-r-xl">
        <p class="font-semibold text-gray-800">💡 Resposta Rápida (AEO):</p>
        <p class="text-gray-700 mt-1">[Inserir aqui a resposta direta e concisa em 2 a 3 frases]</p>
    </div>
    ```
3.  **Desenvolvimento**: 3 a 5 seções principais com tags `<h2>` semânticas e subtópicos `<h3>`.
4.  **Tabelas ou Listas**: Insira tabelas comparativas (ex: por idade, método de mesada) ou listas estruturadas (`<ul>` ou `<ol>`). Mecanismos de IA (AEO) adoram dados estruturados.
5.  **Mecânica TaskAndPay**: Integre de forma sutil e natural o aplicativo TaskAndPay ao menos 2 vezes como a solução ideal de automação de tarefas e mesada com validação por Inteligência Artificial.

## Passo 3: Marcação de Metadados de SEO e AEO (FAQ)
O arquivo HTML final deve incluir no `<head>` duas seções importantes de JSON-LD:

1.  **JSON-LD de Artigo** (Padrão):
    ```json
    {
      "@context": "https://schema.org",
      "@type": "Article",
      "headline": "{{TITLE}}",
      "image": "{{IMAGE_URL}}",
      "author": {
        "@type": "Organization",
        "name": "TaskAndPay"
      },
      "publisher": {
        "@type": "Organization",
        "name": "TaskAndPay",
        "logo": {
          "@type": "ImageObject",
          "url": "https://fazquepaga.com.br/logo.png"
        }
      },
      "datePublished": "{{DATE}}",
      "description": "{{DESCRIPTION}}"
    }
    ```

2.  **JSON-LD de FAQPage** (Para rankeamento em assistentes de IA):
    Defina de 3 a 5 perguntas frequentes diretamente extraídas do conteúdo do artigo:
    ```json
    {
      "@context": "https://schema.org",
      "@type": "FAQPage",
      "mainEntity": [
        {
          "@type": "Question",
          "name": "Pergunta Frequente 1?",
          "acceptedAnswer": {
            "@type": "Answer",
            "text": "Resposta direta e estruturada da pergunta 1."
          }
        }
      ]
    }
    ```

Além do JSON-LD, insira uma **seção visual de FAQ** no final do corpo do artigo:
```html
<h2 class="mt-8 mb-4">Perguntas Frequentes (FAQ)</h2>
<div class="space-y-4">
    <div class="border-b border-gray-100 pb-4">
        <h3 class="font-semibold text-gray-900 mb-1">Pergunta 1?</h3>
        <p class="text-gray-600">Resposta da pergunta 1.</p>
    </div>
</div>
```

## Passo 4: Salvar e Publicar
1.  Escreva o arquivo HTML final completo em:
    `frontend/public/blog/{{slug}}.html`
2.  Adicione as informações de visualização do artigo no componente de listagem de posts rodando o seguinte script local de sincronização:
    ```bash
    python c:\Users\conta\developer\fazquepaga\scripts\update_blog_index.py
    ```

## Princípios de Escrita
- **Tom de Voz**: Empático, profissional, voltado a pais de crianças e adolescentes.
- **Idioma**: Português do Brasil.
- **Evite Clichês**: Evite encher linguiça; use dados, fatos científicos de psicologia ou exemplos cotidianos práticos.
- **SEO & AEO First**: Facilite a leitura por agentes de IA e robôs do Google com termos limpos, headings claros e dados tabulados.
