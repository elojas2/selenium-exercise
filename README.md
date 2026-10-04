# Exercício – Selenium (automationexercise.com)

Testes end-to-end com **Selenium + JUnit 5** para duas funcionalidades do site
[automationexercise.com](https://automationexercise.com/):

- **Test Case 3** – Login com e-mail e senha incorretos (`LoginIncorretoTest`)
- **Test Case 1** – Registrar usuário (`RegistrarUsuarioTest`)

## Como executar

Pré-requisitos: Java 11+, Maven e Google Chrome instalados.
O WebDriverManager baixa o ChromeDriver automaticamente.

```bash
mvn test                    # abre o Chrome
mvn test -Dheadless=true    # sem abrir janela
```

## Projeto dos casos de teste

Os casos foram pensados com **particionamento em classes de equivalência** e
**análise de valor limite**.

### Login (Test Case 3)

| Entrada | Classes válidas | Classes inválidas |
|---|---|---|
| E-mail | formato `local@dominio` (V1) | vazio (I1), sem `@` (I2), sem domínio (I3) |
| Senha | qualquer texto não vazio (V2) | vazia (I4) |
| Credencial | — | combinação não cadastrada (I5) |

Limites considerados: menor e-mail com formato válido (`a@b.co`), parte local com
64 caracteres (limite da RFC 5321) e 65 caracteres (acima do limite), senha com
1 caractere e senha com 256 caracteres.

| ID | E-mail | Senha | Classe / limite | Resultado esperado |
|---|---|---|---|---|
| CT01 | `nao.cadastrado.qa@teste.com` | `Senha@123` | V1, V2, I5 | "Your email or password is incorrect!" |
| CT02 | `a@b.co` | `Senha@123` | limite mínimo do formato | mensagem de erro |
| CT03 | 64 × `a` + `@teste.com` | `x` | limite da parte local; senha 1 caractere | mensagem de erro |
| CT04 | 65 × `a` + `@teste.com` | `Senha@123` | acima do limite | mensagem de erro |
| CT05 | `nao.cadastrado.qa@teste.com` | 256 caracteres | senha longa | mensagem de erro |
| CT06 | `nao.cadastrado.qa@teste.com` | `"   "` | senha só com espaços | mensagem de erro |
| CT07 | vazio | `Senha@123` | I1 | formulário barrado pelo navegador |
| CT08 | `nao.cadastrado.qa@teste.com` | vazia | I4 | formulário barrado pelo navegador |
| CT09 | `emailsemarroba.com` | `Senha@123` | I2 | formulário barrado pelo navegador |
| CT10 | `usuario@` | `Senha@123` | I3 | formulário barrado pelo navegador |
| CT11 | vazio | vazia | I1 + I4 | formulário barrado pelo navegador |

### Registrar usuário (Test Case 1)

| Entrada | Classes válidas | Classes inválidas |
|---|---|---|
| Nome | texto não vazio (V3) | vazio (I6) |
| E-mail | formato válido e ainda não cadastrado (V4) | vazio (I7), formato inválido (I8), já cadastrado (I9) |

Limites considerados: nome com 1 caractere e nome com 50 caracteres.

| ID | Nome | E-mail | Classe / limite | Resultado esperado |
|---|---|---|---|---|
| CT12 | `Eloyse Teste` | e-mail único | V3, V4 | conta criada, "Logged in as", conta deletada |
| CT13 | `A` | e-mail único | limite mínimo do nome | fluxo completo com sucesso |
| CT14 | 50 × `N` | e-mail único | nome longo | fluxo completo com sucesso |
| CT15 | `João Conceição` | e-mail único | caracteres acentuados | fluxo completo com sucesso |
| CT16 | vazio | `valido.qa@teste.com` | I6 | formulário barrado pelo navegador |
| CT17 | `Eloyse` | vazio | I7 | formulário barrado pelo navegador |
| CT18 | `Eloyse` | `emailsemarroba.com` | I8 | formulário barrado pelo navegador |
| CT19 | `Eloyse` | `@teste.com` | I8 | formulário barrado pelo navegador |
| CT20 | `Eloyse Duplicado` | e-mail já cadastrado | I9 | "Email Address already exist!" |

Os e-mails "únicos" são gerados com o timestamp da execução para não colidir
com contas existentes, e toda conta criada é deletada ao final do teste.
