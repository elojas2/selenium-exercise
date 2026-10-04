package br.com.automationexercise;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;

/**
 * Test Case 3: Login User with incorrect email and password.
 *
 * Os casos foram derivados por particionamento em classes de equivalência e
 * análise de valor limite (ver tabela no README.md).
 */
public class LoginIncorretoTest extends BaseTest {

    private static final By TITULO_LOGIN = By.xpath("//h2[normalize-space()='Login to your account']");
    private static final By CAMPO_EMAIL = By.cssSelector("[data-qa='login-email']");
    private static final By CAMPO_SENHA = By.cssSelector("[data-qa='login-password']");
    private static final By BOTAO_LOGIN = By.cssSelector("[data-qa='login-button']");
    private static final By MSG_ERRO = By.xpath(
            "//form[@action='/login']//p[normalize-space()='Your email or password is incorrect!']");

    /**
     * Entradas que passam na validação do navegador e chegam ao servidor,
     * que deve responder com a mensagem de erro.
     */
    static Stream<Arguments> credenciaisIncorretas() {
        return Stream.of(
                Arguments.of("E-mail com formato válido, não cadastrado",
                        "nao.cadastrado.qa@teste.com", "Senha@123"),
                Arguments.of("E-mail no limite mínimo de formato (a@b.co)",
                        "a@b.co", "Senha@123"),
                Arguments.of("Parte local com 64 caracteres (limite RFC) e senha de 1 caractere",
                        "a".repeat(64) + "@teste.com", "x"),
                Arguments.of("Parte local com 65 caracteres (acima do limite)",
                        "a".repeat(65) + "@teste.com", "Senha@123"),
                Arguments.of("Senha muito longa (256 caracteres)",
                        "nao.cadastrado.qa@teste.com", "s".repeat(256)),
                Arguments.of("Senha só com espaços em branco",
                        "nao.cadastrado.qa@teste.com", "   "));
    }

    /**
     * Entradas inválidas que são barradas pela validação HTML5 do formulário
     * (campos required e type=email): o login nem é enviado.
     */
    static Stream<Arguments> entradasBarradasPeloNavegador() {
        return Stream.of(
                Arguments.of("E-mail vazio", "", "Senha@123"),
                Arguments.of("Senha vazia", "nao.cadastrado.qa@teste.com", ""),
                Arguments.of("E-mail sem @", "emailsemarroba.com", "Senha@123"),
                Arguments.of("E-mail sem domínio", "usuario@", "Senha@123"),
                Arguments.of("E-mail e senha vazios", "", ""));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("credenciaisIncorretas")
    @DisplayName("Login com e-mail/senha incorretos exibe mensagem de erro")
    public void loginComCredenciaisIncorretas(String descricao, String email, String senha) {
        abrirHomeEVerificar();                              // passos 1-3
        clicarSignupLogin();                                // passo 4
        assertTrue(visivel(TITULO_LOGIN),                   // passo 5
                "'Login to your account' deveria estar visível");

        preencher(CAMPO_EMAIL, email);                      // passo 6
        preencher(CAMPO_SENHA, senha);
        clicar(BOTAO_LOGIN);                                // passo 7

        assertEquals("Your email or password is incorrect!", texto(MSG_ERRO)); // passo 8
        assertFalse(existe(LOGADO_COMO),
                "O usuário não deveria estar logado");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("entradasBarradasPeloNavegador")
    @DisplayName("Login com entrada inválida é barrado pela validação do formulário")
    public void loginComEntradaInvalida(String descricao, String email, String senha) {
        abrirHomeEVerificar();                              // passos 1-3
        clicarSignupLogin();                                // passo 4
        assertTrue(visivel(TITULO_LOGIN));                  // passo 5

        preencher(CAMPO_EMAIL, email);                      // passo 6
        preencher(CAMPO_SENHA, senha);

        assertFalse(formularioValido(CAMPO_EMAIL), "O formulário deveria ser inválido");
        clicar(BOTAO_LOGIN);                                // passo 7

        assertTrue(driver.getCurrentUrl().contains("/login"), "Deveria continuar na página de login");
        assertFalse(existe(MSG_ERRO), "A requisição não deveria chegar ao servidor"); // passo 8
        assertFalse(existe(LOGADO_COMO));
    }
}
