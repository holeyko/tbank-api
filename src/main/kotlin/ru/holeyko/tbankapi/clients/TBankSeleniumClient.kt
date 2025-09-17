package ru.holeyko.tbankapi.clients

import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.slf4j.LoggerFactory
import ru.holeyko.tbankapi.clients.TBankClient.Companion.INTERNAL_TRANSFER_TEMPLATE
import ru.holeyko.tbankapi.clients.TBankClient.Companion.PK_URL
import ru.holeyko.tbankapi.exceptions.TBankClientIIllegalArgumentException
import ru.holeyko.tbankapi.exceptions.TBankClientIllegalStateException
import ru.holeyko.tbankapi.extentions.DEFAULT_CHECK_TIMEOUT
import ru.holeyko.tbankapi.extentions.findBy
import ru.holeyko.tbankapi.extentions.goToIfNeed
import ru.holeyko.tbankapi.mappers.DebitCardMapper
import ru.holeyko.tbankapi.mappers.SavingMapper
import ru.holeyko.tbankapi.model.DebitCard
import ru.holeyko.tbankapi.model.Saving
import java.math.BigDecimal

class TBankSeleniumClient(
    private val driver: ChromeDriver,
) : TBankClient, AutoCloseable {

    override fun getSavings(): List<Saving> {
        driver.goToIfNeed(PK_URL)
        val widgetContainer = findProductsWidget()
        val savingDivs = widgetContainer.findElements(By.xpath("//div[contains(@data-qa-type, 'widget-savings')]"))

        return savingDivs.map(SavingMapper::mapFromDivOnPKUrl)
    }

    override fun getDebitCards(): List<DebitCard> {
        driver.goToIfNeed(PK_URL)
        val widgetContainer = findProductsWidget()
        val debitCardDivs = widgetContainer.findElements(By.xpath("//div[contains(@data-qa-type, 'widget-debit')]"))

        return debitCardDivs.map(DebitCardMapper::mapFromDivOnPKUrl)
    }

    override fun transferMoney(fromId: Long, toId: Long, amount: BigDecimal) {
        LOG.debug("Try to transfer money from $fromId to $toId with $amount RUB")

        if (fromId == toId) {
            throw TBankClientIIllegalArgumentException("fromId and toId are same [id=$fromId]")
        }

        driver.goToIfNeed(createTransferUrl(fromId, toId, amount))
        val submitButton = driver.findBy(By.xpath("//button[@data-qa-type='submit-button']"))
            ?: throw TBankClientIllegalStateException("Can't find submit button for transfer money")
        submitButton.click()

        driver.findBy(By.xpath("//div[contains(@data-qa-type, 'notification-pay-popup']"), DEFAULT_CHECK_TIMEOUT)
            ?.let { throw TBankClientIIllegalArgumentException("Insufficient funds for the transfer") }
    }

    override fun close() {
        driver.quit()
    }

    private fun findProductsWidget(): WebElement = driver.findBy(By.xpath("//ul[@data-qa-type='visible-items']"))
        ?: throw TBankClientIllegalStateException("Can't find saving widgets")

    private fun createTransferUrl(fromId: Long, toId: Long, amount: BigDecimal) = INTERNAL_TRANSFER_TEMPLATE
        .replace("{fromId}", fromId.toString())
        .replace("{toId}", toId.toString())
        .replace("{amount}", amount.toString())

    companion object {
        private val LOG = LoggerFactory.getLogger(TBankSeleniumClient::class.java)
    }
}
