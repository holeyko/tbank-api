package ru.holeyko.tbankapi.clients

import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.slf4j.LoggerFactory
import ru.holeyko.tbankapi.exceptions.TBankClientElementNotFoundException
import ru.holeyko.tbankapi.exceptions.TBankClientIIllegalArgumentException
import ru.holeyko.tbankapi.exceptions.TBankClientIllegalStateException
import ru.holeyko.tbankapi.extentions.DEFAULT_CHECK_TIMEOUT
import ru.holeyko.tbankapi.extentions.findBy

object TBankClientFactory {
    const val AUTH_URL = "https://www.tbank.ru/login"

    private val LOG = LoggerFactory.getLogger(TBankClientFactory::class.java)

    fun defaultOptions(): ChromeOptions = ChromeOptions()
        .addArguments(
            "--headless=new",
            "--window-size=1280,720",
            "--enable-webgl"
        )
        .addArguments(
            "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36"
        )

    fun openConnect(
        phoneRetriever: () -> String,
        codeRetriever: () -> String,
        passwordRetriever: () -> String,
        options: ChromeOptions = defaultOptions()
    ): TBankClient {
        val driver = ChromeDriver(options)

        try {
            driver.setup(phoneRetriever, codeRetriever, passwordRetriever)
        } catch (e: Exception) {
            LOG.debug("Failed open connection due exception.", e)
            driver.close()
            throw e
        }

        return TBankSeleniumClient(driver)
    }

    private fun ChromeDriver.setup(
        phoneRetriever: () -> String,
        codeRetriever: () -> String,
        passwordRetriever: () -> String,
    ) {
        LOG.debug("Go to auth url: $AUTH_URL")
        get(AUTH_URL)

        passPhone(phoneRetriever)
        passSecretCode(codeRetriever)
        passPassword(passwordRetriever)
        skipFastCode()

        // if (checkLoginSuccess()) {
        // TODO: fix it
        if (true) {
            return
        } else {
            throw TBankClientIllegalStateException("Failed login attempt")
        }
    }

    private fun ChromeDriver.passPhone(phoneRetriever: () -> String) {
        LOG.debug("Try to submit phone")

        val phone = phoneRetriever()
        val phoneInput = findBy(By.xpath("//input[@automation-id='phone-input']"))
            ?: throw TBankClientIllegalStateException("This is not phone page [url=$currentUrl]")
        phoneInput.clear()
        phoneInput.sendKeys(phone)

        val submitButton = findBy(By.xpath("//button[@automation-id='button-submit']"))
            ?: throw TBankClientElementNotFoundException("Can't find password submit button")
        submitButton.click()

        findBy(By.xpath("//p[@automation-id='server-error']"), DEFAULT_CHECK_TIMEOUT)?.let {
            throw TBankClientIIllegalArgumentException("Incorrect phone number")
        }

        findBy(By.xpath("//h5[@automation-id='access-denied-title']"), DEFAULT_CHECK_TIMEOUT)?.let {
            throw TBankClientIllegalStateException("Access denied, too many tries")
        }

        LOG.debug("Phone has been submitted successfully")
    }

    private fun ChromeDriver.passSecretCode(codeRetriever: () -> String) {
        LOG.debug("Try to submit secret code")

        val codeInput = findBy(By.xpath("//input[@name='otp']"))
            ?: throw TBankClientIllegalStateException("This is not secret code page [url=$currentUrl]")

        val secretCode = codeRetriever()
        LOG.debug("Passed code: $secretCode")
        if (secretCode.isBlank()) {
            throw TBankClientIIllegalArgumentException("Secret code must contains only 4 numbers")
        }
        codeInput.clear()
        codeInput.sendKeys(secretCode)

        findBy(By.xpath("//p[@automation-id='server-error']"), DEFAULT_CHECK_TIMEOUT)?.let {
            throw TBankClientIIllegalArgumentException("Secret code is incorrect")
        }

        LOG.debug("Secret code has been submitted successfully")
    }

    private fun ChromeDriver.passPassword(passwordRetriever: () -> String) {
        LOG.debug("Try to pass password")

        val passwordForm = findBy(By.xpath("//input[@name='password']")) ?: run {
            LOG.debug("Skip password form")
            return
        }

        val password = passwordRetriever()
        if (password.isBlank()) {
            throw TBankClientIIllegalArgumentException("Incorrect password")
        }

        passwordForm.clear()
        passwordForm.sendKeys(password)

        val submitButton = findBy(By.xpath("//button[@automation-id='button-submit']"))
            ?: throw TBankClientIllegalStateException("Can't find password submit button")
        submitButton.click()

        findBy(By.xpath("//p[@automation-id='server-error']"), DEFAULT_CHECK_TIMEOUT)?.let {
            throw TBankClientIIllegalArgumentException("Password is incorrect")
        }

        LOG.debug("Password has been passed successfully")
    }

    private fun ChromeDriver.skipFastCode() {
        LOG.debug("Try to skip fast code")

        findBy(By.xpath("//h1[@id='form-title']"))?.text ?: run {
            LOG.debug("Current url isn't fast code page [url=$currentUrl]")
            return
        }

        val skipButton = findBy(By.xpath("//button[@automation-id='cancel-button']"))
            ?: throw TBankClientIllegalStateException("Can't find skip button in fast code page")
        skipButton.click()

        LOG.debug("Fast code has been skipped successfully")
    }

    private fun ChromeDriver.checkLoginSuccess(): Boolean {
        for (i in 1..4) {
            val success = currentUrl?.contains("/mybank") == true
            if (success) {
                return true
            }
            Thread.sleep(100)
        }

        return false
    }
}
