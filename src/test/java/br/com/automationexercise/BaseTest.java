package br.com.automationexercise;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Classe base com a configuração do driver e passos comuns aos dois cenários
 * (abrir a home, verificar que ela carregou e ir para "Signup / Login").
 */
public abstract class BaseTest {

    protected static final String BASE_URL = "https://automationexercise.com";

    /** Link "Logged in as <nome>" do menu; o &lt;b&gt; contém o nome do usuário. */
    protected static final By LOGADO_COMO = By.xpath("//a[contains(.,'Logged in as')]/b");

    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeEach
    public void createDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1366,900");
        // O site exibe anúncios do Google que às vezes cobrem botões e abrem
        // um "vignette" em tela cheia. Bloqueamos esses domínios para o teste ficar estável.
        options.addArguments("--host-resolver-rules="
                + "MAP googleads.g.doubleclick.net 127.0.0.1,"
                + "MAP pagead2.googlesyndication.com 127.0.0.1,"
                + "MAP tpc.googlesyndication.com 127.0.0.1,"
                + "MAP fundingchoicesmessages.google.com 127.0.0.1");
        // Rodar sem abrir janela: mvn test -Dheadless=true
        if (Boolean.getBoolean("headless")) {
            options.addArguments("--headless=new");
        }
        driver = WebDriverManager.chromedriver().capabilities(options).create();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    public void quitDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    /** Passos 1 a 3: abrir o navegador, acessar a URL e verificar a home. */
    protected void abrirHomeEVerificar() {
        driver.get(BASE_URL);
        fecharConsentimentoSeHouver();
        assertEquals("Automation Exercise", driver.getTitle());
        assertTrue(visivel(By.id("slider")), "A home page deveria estar visível");
    }

    /** Passo 4: clicar em "Signup / Login". */
    protected void clicarSignupLogin() {
        clicar(By.cssSelector("a[href='/login']"));
        if (driver.getCurrentUrl().contains("google_vignette")) {
            driver.get(BASE_URL + "/login");
        }
        wait.until(ExpectedConditions.urlContains("/login"));
    }

    // ----------------------------------------------------------------- utilitários

    protected boolean visivel(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
    }

    protected boolean existe(By locator) {
        List<WebElement> elementos = driver.findElements(locator);
        return !elementos.isEmpty() && elementos.get(0).isDisplayed();
    }

    protected String texto(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText().trim();
    }

    protected void preencher(By locator, String valor) {
        WebElement campo = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        campo.clear();
        campo.sendKeys(valor);
    }

    /** Seleciona uma opção de um &lt;select&gt; pelo atributo value. */
    protected void selecionar(By locator, String valor) {
        WebElement campo = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        new Select(campo).selectByValue(valor);
    }

    /** Clica rolando até o elemento; se algo estiver por cima (anúncio), usa JavaScript. */
    protected void clicar(By locator) {
        WebElement elemento = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        js().executeScript("arguments[0].scrollIntoView({block: 'center'});", elemento);
        try {
            wait.until(ExpectedConditions.elementToBeClickable(elemento)).click();
        } catch (Exception e) {
            js().executeScript("arguments[0].click();", elemento);
        }
    }

    /** Retorna true se o formulário que contém o elemento passa na validação HTML5. */
    protected boolean formularioValido(By campoDoFormulario) {
        WebElement campo = driver.findElement(campoDoFormulario);
        return (Boolean) js().executeScript("return arguments[0].form.checkValidity();", campo);
    }

    protected JavascriptExecutor js() {
        return (JavascriptExecutor) driver;
    }

    private void fecharConsentimentoSeHouver() {
        List<WebElement> botoes = driver.findElements(By.cssSelector("button.fc-cta-consent"));
        if (!botoes.isEmpty() && botoes.get(0).isDisplayed()) {
            botoes.get(0).click();
        }
    }

    /** Gera um e-mail único para não colidir com contas já existentes no site. */
    protected static String emailUnico(String prefixo) {
        return prefixo + System.currentTimeMillis() + "@teste.com";
    }
}
