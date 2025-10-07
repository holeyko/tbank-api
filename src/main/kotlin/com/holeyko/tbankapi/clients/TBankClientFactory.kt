package com.holeyko.tbankapi.clients

import com.holeyko.tbankapi.exceptions.TBankClientElementNotFoundException
import com.holeyko.tbankapi.exceptions.TBankClientIIllegalArgumentException
import com.holeyko.tbankapi.exceptions.TBankClientIllegalStateException
import com.holeyko.tbankapi.extentions.DEFAULT_CHECK_TIMEOUT
import com.holeyko.tbankapi.extentions.findBy
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.slf4j.LoggerFactory

object TBankClientFactory {
    const val AUTH_URL = "https://www.tbank.ru/login"

    private val LOG = LoggerFactory.getLogger(TBankClientFactory::class.java)

    fun defaultOptions(withGui: Boolean = false): ChromeOptions {
        val options = ChromeOptions().addArguments(
            "--window-size=1280,720",
            "--enable-webgl",
            "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36"
        )

        if (!withGui) {
            options.addArguments("--headless=new")
        }

        return options
    }

    fun openConnection(
        phone: String,
        codeRetriever: () -> String,
        password: String,
        fastCode: String,
        options: ChromeOptions = defaultOptions()
    ): TBankClient {
        val driver = ChromeDriver(options)

        try {
            driver.setup(phone, codeRetriever, password, fastCode)
        } catch (e: Exception) {
            LOG.debug("Failed open connection due exception.", e)
            driver.close()
            throw e
        }

        return TBankSeleniumClient(driver, fastCode)
    }

    private fun ChromeDriver.setup(
        phone: String,
        codeRetriever: () -> String,
        password: String,
        fastCode: String,
    ) {
        LOG.debug("Go to auth url: $AUTH_URL")
        get(AUTH_URL)

        passPhone(phone)
        passSecretCode(codeRetriever)
        passPassword(password)
        passFastCode(fastCode)

        // if (checkLoginSuccess()) {
        // TODO: fix it
        if (true) {
            return
        } else {
            throw TBankClientIllegalStateException("Failed login attempt")
        }
    }

    private fun ChromeDriver.passPhone(phone: String) {
        LOG.debug("Try to submit phone")

        findBy(By.xpath("//input[@automation-id='phone-input']"))
            ?.sendKeys(phone)
            ?: throw TBankClientIllegalStateException("This is not phone page [url=$currentUrl]")

        findBy(By.xpath("//button[@automation-id='button-submit']"))
            ?.click()
            ?: throw TBankClientElementNotFoundException("Can't find password submit button")

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

        val secretCode = codeRetriever()
        LOG.debug("Passed code: $secretCode")
        if (secretCode.isBlank()) {
            throw TBankClientIIllegalArgumentException("Secret code must contains only 4 numbers")
        }

        findBy(By.xpath("//input[@name='otp']"))
            ?.sendKeys(secretCode)
            ?: throw TBankClientIllegalStateException("This is not secret code page [url=$currentUrl]")

        findBy(By.xpath("//p[@automation-id='server-error']"), DEFAULT_CHECK_TIMEOUT)?.let {
            throw TBankClientIIllegalArgumentException("Secret code is incorrect")
        }

        LOG.debug("Secret code has been submitted successfully")
    }

    private fun ChromeDriver.passPassword(password: String) {
        LOG.debug("Try to pass password")

        if (password.isBlank()) {
            throw TBankClientIIllegalArgumentException("Incorrect password")
        }

        findBy(By.xpath("//input[@name='password']"))
            ?.sendKeys(password)
            ?: run {
                LOG.debug("Skip password form")
                return
            }

        findBy(By.xpath("//button[@automation-id='button-submit']"))
            ?.click()
            ?: throw TBankClientIllegalStateException("Can't find password submit button")

        findBy(By.xpath("//p[@automation-id='server-error']"), DEFAULT_CHECK_TIMEOUT)?.let {
            throw TBankClientIIllegalArgumentException("Password is incorrect")
        }

        LOG.debug("Password has been passed successfully")
    }

    private fun ChromeDriver.passFastCode(fastCode: String) {
        LOG.debug("Try to pass fast code")

        if (fastCode.length != 4) {
            throw TBankClientIIllegalArgumentException("Fast code must has 4 symbols")
        }

        findBy(By.xpath("//h1[@id='form-title']"))?.text
            ?.takeIf { it == "Придумайте код" }
            ?: run {
                LOG.debug("Current url isn't fast code page [url=$currentUrl]")
                return
            }

        fastCode.forEachIndexed { i, sym ->
            findBy(By.xpath("//input[@id='pinCode$i']"))
                ?.sendKeys(sym.toString())
                ?: throw TBankClientIllegalStateException("Can't find ${i + 1} field for fast code")
        }

        findBy(By.xpath("//button[@automation-id='button-submit']"))
            ?.click()
            ?: throw TBankClientIllegalStateException("Can't find submit button in fast code page")

        LOG.debug("Fast code has been passed successfully")
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
