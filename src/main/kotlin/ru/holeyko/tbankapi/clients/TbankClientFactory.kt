package ru.holeyko.tbankapi.clients

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.withTimeout
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.slf4j.LoggerFactory
import ru.holeyko.tbankapi.exceptions.TBankClientException
import ru.holeyko.tbankapi.extentions.findBy
import java.time.Duration

object TbankClientFactory {
    const val AUTH_URL = "https://www.tbank.ru/login"

    private val LOG = LoggerFactory.getLogger(TbankClientFactory::class.java)
    private val DEFAULT_CHECK_TIMEOUT = Duration.ofSeconds(1L)

    fun openConnect(
        options: ChromeOptions,
        phoneRetriever: () -> String,
        codeRetriever: () -> String,
        passwordRetriever: () -> String
    ): TBankClient {
        val driver = ChromeDriver(options)
        try {
            driver.setup(phoneRetriever, codeRetriever, passwordRetriever)
        } catch (e: Exception) {
            LOG.debug("Failed open connection", e)
            throw e
        }

        return TBankSeleniumClient(driver)
    }

    private fun ChromeDriver.setup(
        phoneRetriever: () -> String,
        codeRetriever: () -> String,
        passwordRetriever: () -> String,
    ) {
        LOG.debug("Переход на страницу входа T-Bank")
        get(AUTH_URL)

        passPhone(phoneRetriever)
        passSecretCode(codeRetriever)
        passPassword(passwordRetriever)
        skipFastCode()

        // if (checkLoginSuccess()) {
        if (true) {
            return
        } else {
            throw TBankClientException("Статус входа неопределен")
        }
    }

    private fun ChromeDriver.passPhone(phoneRetriever: () -> String) {
        val phone = phoneRetriever()
        val phoneInput = findBy(By.xpath("//input[@name='phone']"))
            ?: throw TBankClientException("This is not phone page")
        phoneInput.clear()
        phoneInput.sendKeys(phone)

        val submitButton = findBy(By.xpath("//button[@automation-id='button-submit']"))
            ?: throw TBankClientException("Can't find password submit button")
        submitButton.click()

        findBy(By.xpath("//p[@automation-id='server-error']"), DEFAULT_CHECK_TIMEOUT)?.let {
            throw TBankClientException("Incorrect phone number")
        }
    }

    private fun ChromeDriver.passSecretCode(codeRetriever: () -> String) {
        val secretCode = codeRetriever()
        LOG.debug("Введенный код: $secretCode")
        if (secretCode.isBlank()) {
            throw IllegalArgumentException("Secret code must be contains 4 numbers")
        }

        val codeInput = findBy(By.xpath("//input[@name='otp']"))
            ?: throw TBankClientException("This isn't secret code page")
        codeInput.clear()
        codeInput.sendKeys(secretCode)

        findBy(By.xpath("//p[@automation-id='server-error']"), DEFAULT_CHECK_TIMEOUT)?.let {
            throw TBankClientException("Secret code is incorrect")
        }
    }

    private fun ChromeDriver.passPassword(passwordRetriever: () -> String) {
        val passwordForm = findBy(By.xpath("//input[@name='password']"))
            ?: run {
                LOG.debug("Skip password form")
                return
            }

        val password = passwordRetriever()
        if (password.isBlank()) {
            throw IllegalArgumentException("Secret code must be contains 4 numbers")
        }

        passwordForm.clear()
        passwordForm.sendKeys(password)

        val submitButton = findBy(By.xpath("//button[@automation-id='button-submit']"))
            ?: throw TBankClientException("Can't find password submit button")
        submitButton.click()

        findBy(By.xpath("//p[@automation-id='server-error']"), DEFAULT_CHECK_TIMEOUT)?.let {
            throw TBankClientException("Secret code is incorrect")
        }
    }

    private fun ChromeDriver.skipFastCode() {
        val title = findBy(By.xpath("//h1[@id='form-title']")) ?: return
        if (title.text != "Придумайте код") {
            LOG.debug("Fast code page doesn't exist")
            return
        }

        val skipButton = findBy(By.xpath("//button[@automation-id='cancel-button']"))
            ?: throw TBankClientException("Can't find skip button")
        skipButton.click()
    }

    private fun ChromeDriver.checkLoginSuccess(): Boolean {
        return runBlocking {
            withTimeout(DEFAULT_CHECK_TIMEOUT) {
                currentUrl?.contains("/mybank") == true
            }
        }
    }
}
