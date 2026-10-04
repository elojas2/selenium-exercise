package br.com.automationexercise;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;

/**
 * Test Case 1: Register User.
 *
 * Os casos foram derivados por particionamento em classes de equivalência e
 * análise de valor limite (ver tabela no README.md).
 */
public class RegistrarUsuarioTest extends BaseTest {

    private static final String SENHA = "Senha@123";

    // Signup
    private static final By TITULO_SIGNUP = By.xpath("//h2[normalize-space()='New User Signup!']");
    private static final By SIGNUP_NOME = By.cssSelector("[data-qa='signup-name']");
    private static final By SIGNUP_EMAIL = By.cssSelector("[data-qa='signup-email']");
    private static final By BOTAO_SIGNUP = By.cssSelector("[data-qa='signup-button']");
    private static final By MSG_EMAIL_EXISTENTE = By.xpath(
            "//form[@action='/signup']//p[normalize-space()='Email Address already exist!']");

    // Login (usado na limpeza do teste de e-mail já cadastrado)
    private static final By LOGIN_EMAIL = By.cssSelector("[data-qa='login-email']");
    private static final By LOGIN_SENHA = By.cssSelector("[data-qa='login-password']");
    private static final By BOTAO_LOGIN = By.cssSelector("[data-qa='login-button']");

    // Informações da conta
    private static final By TITULO_INFO_CONTA = By.xpath("//b[normalize-space()='Enter Account Information']");
    private static final By TITULO_SRA = By.id("id_gender2");
    private static final By SENHA_CONTA = By.cssSelector("[data-qa='password']");
    private static final By DIA = By.cssSelector("[data-qa='days']");
    private static final By MES = By.cssSelector("[data-qa='months']");
    private static final By ANO = By.cssSelector("[data-qa='years']");
    private static final By NEWSLETTER = By.id("newsletter");
    private static final By OFERTAS_PARCEIROS = By.id("optin");
    private static final By PRIMEIRO_NOME = By.cssSelector("[data-qa='first_name']");
    private static final By SOBRENOME = By.cssSelector("[data-qa='last_name']");
    private static final By EMPRESA = By.cssSelector("[data-qa='company']");
    private static final By ENDERECO = By.cssSelector("[data-qa='address']");
    private static final By ENDERECO2 = By.cssSelector("[data-qa='address2']");
    private static final By PAIS = By.cssSelector("[data-qa='country']");
    private static final By ESTADO = By.cssSelector("[data-qa='state']");
    private static final By CIDADE = By.cssSelector("[data-qa='city']");
    private static final By CEP = By.cssSelector("[data-qa='zipcode']");
    private static final By CELULAR = By.cssSelector("[data-qa='mobile_number']");
    private static final By BOTAO_CRIAR_CONTA = By.cssSelector("[data-qa='create-account']");

    // Resultado
    private static final By CONTA_CRIADA = By.cssSelector("[data-qa='account-created']");
    private static final By CONTA_DELETADA = By.cssSelector("[data-qa='account-deleted']");
    private static final By BOTAO_CONTINUAR = By.cssSelector("[data-qa='continue-button']");
    private static final By DELETAR_CONTA = By.cssSelector("a[href='/delete_account']");
    private static final By LOGOUT = By.cssSelector("a[href='/logout']");

    /** Nomes válidos: típico e valores-limite. */
    static Stream<Arguments> nomesValidos() {
        return Stream.of(
                Arguments.of("Nome típico", "Eloyse Teste"),
                Arguments.of("Nome com 1 caractere (limite mínimo)", "A"),
                Arguments.of("Nome com 50 caracteres", "N".repeat(50)),
                Arguments.of("Nome com acentos", "João Conceição"));
    }

    /** Entradas inválidas barradas pela validação HTML5 do formulário de signup. */
    static Stream<Arguments> signupInvalido() {
        return Stream.of(
                Arguments.of("Nome vazio", "", "valido.qa@teste.com"),
                Arguments.of("E-mail vazio", "Eloyse", ""),
                Arguments.of("E-mail sem @", "Eloyse", "emailsemarroba.com"),
                Arguments.of("E-mail sem parte local", "Eloyse", "@teste.com"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("nomesValidos")
    @DisplayName("Registrar usuário com dados válidos (Test Case 1 completo)")
    public void registrarUsuario(String descricao, String nome) {
        String email = emailUnico("qa.registro.");

        abrirHomeEVerificar();                                      // passos 1-3
        clicarSignupLogin();                                        // passo 4
        assertTrue(visivel(TITULO_SIGNUP),                          // passo 5
                "'New User Signup!' deveria estar visível");

        fazerSignup(nome, email);                                   // passos 6-7
        assertTrue(visivel(TITULO_INFO_CONTA),                      // passo 8
                "'ENTER ACCOUNT INFORMATION' deveria estar visível");

        preencherInformacoesDaConta();                              // passos 9-12
        clicar(BOTAO_CRIAR_CONTA);                                  // passo 13

        assertEquals("ACCOUNT CREATED!", texto(CONTA_CRIADA).toUpperCase()); // passo 14
        clicar(BOTAO_CONTINUAR);                                    // passo 15

        assertEquals(nome, texto(LOGADO_COMO));                     // passo 16

        clicar(DELETAR_CONTA);                                      // passo 17
        assertEquals("ACCOUNT DELETED!", texto(CONTA_DELETADA).toUpperCase()); // passo 18
        clicar(BOTAO_CONTINUAR);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("signupInvalido")
    @DisplayName("Signup com entrada inválida é barrado pela validação do formulário")
    public void signupComEntradaInvalida(String descricao, String nome, String email) {
        abrirHomeEVerificar();                                      // passos 1-3
        clicarSignupLogin();                                        // passo 4
        assertTrue(visivel(TITULO_SIGNUP));                         // passo 5

        preencher(SIGNUP_NOME, nome);                               // passo 6
        preencher(SIGNUP_EMAIL, email);

        assertFalse(formularioValido(SIGNUP_NOME), "O formulário deveria ser inválido");
        clicar(BOTAO_SIGNUP);                                       // passo 7

        assertTrue(driver.getCurrentUrl().contains("/login"), "Deveria continuar na página de login");
        assertFalse(existe(TITULO_INFO_CONTA), "Não deveria avançar para o cadastro"); // passo 8
    }

    @Test
    @DisplayName("Signup com e-mail já cadastrado exibe erro")
    public void signupComEmailJaCadastrado() {
        String nome = "Eloyse Duplicado";
        String email = emailUnico("qa.duplicado.");

        // Pré-condição: cria uma conta com o e-mail e faz logout
        abrirHomeEVerificar();                                      // passos 1-3
        clicarSignupLogin();                                        // passo 4
        fazerSignup(nome, email);                                   // passos 6-7
        assertTrue(visivel(TITULO_INFO_CONTA));                     // passo 8
        preencherInformacoesDaConta();                              // passos 9-12
        clicar(BOTAO_CRIAR_CONTA);                                  // passo 13
        assertTrue(visivel(CONTA_CRIADA));                          // passo 14
        clicar(BOTAO_CONTINUAR);                                    // passo 15
        clicar(LOGOUT);

        // Tenta registrar de novo com o mesmo e-mail
        assertTrue(visivel(TITULO_SIGNUP));                         // passo 5
        fazerSignup(nome, email);                                   // passos 6-7
        assertEquals("Email Address already exist!", texto(MSG_EMAIL_EXISTENTE)); // passo 8

        // Limpeza: entra com a conta e a deleta
        preencher(LOGIN_EMAIL, email);
        preencher(LOGIN_SENHA, SENHA);
        clicar(BOTAO_LOGIN);
        clicar(DELETAR_CONTA);
        assertTrue(visivel(CONTA_DELETADA));
    }

    /** Passos 6 e 7 do Test Case 1. */
    private void fazerSignup(String nome, String email) {
        preencher(SIGNUP_NOME, nome);
        preencher(SIGNUP_EMAIL, email);
        clicar(BOTAO_SIGNUP);
    }

    /** Passos 9 a 12 do Test Case 1. */
    private void preencherInformacoesDaConta() {
        clicar(TITULO_SRA);                                         // passo 9
        preencher(SENHA_CONTA, SENHA);
        selecionar(DIA, "10");
        selecionar(MES, "5");
        selecionar(ANO, "1998");

        clicar(NEWSLETTER);                                         // passo 10
        clicar(OFERTAS_PARCEIROS);                                  // passo 11

        preencher(PRIMEIRO_NOME, "Eloyse");                         // passo 12
        preencher(SOBRENOME, "Teste");
        preencher(EMPRESA, "Empresa Teste");
        preencher(ENDERECO, "Rua das Flores, 100");
        preencher(ENDERECO2, "Apto 201");
        selecionar(PAIS, "Canada");
        preencher(ESTADO, "Ontario");
        preencher(CIDADE, "Toronto");
        preencher(CEP, "M5V2T6");
        preencher(CELULAR, "+5521999999999");
    }
}
