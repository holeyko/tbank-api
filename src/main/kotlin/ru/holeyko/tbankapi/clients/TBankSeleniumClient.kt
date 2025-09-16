package ru.holeyko.tbankapi.clients

import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.slf4j.LoggerFactory
import ru.holeyko.tbankapi.clients.TBankClient.Companion.INTERNAL_TRANSFER_URL
import ru.holeyko.tbankapi.clients.TBankClient.Companion.PK_URL
import ru.holeyko.tbankapi.exceptions.TBankClientException
import ru.holeyko.tbankapi.extentions.findAllBy
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
        val widgetContainer = driver.findBy(By.xpath("//ul[@data-qa-type='visible-items']"))
            ?: throw TBankClientException("Can't find saving widgets")
        val savingDivs = widgetContainer.findElements(By.xpath("//div[contains(@data-qa-type, 'widget-savings')]"))

        return savingDivs.map { SavingMapper.mapFromDivOnPKUrl(it) }
    }

    override fun getDebitCards(): List<DebitCard> {
        driver.goToIfNeed(PK_URL)
        val widgetContainer = driver.findBy(By.xpath("//ul[@data-qa-type='visible-items']"))
            ?: throw TBankClientException("Can't find saving widgets")
        val debitCardDivs = widgetContainer.findElements(By.xpath("//div[contains(@data-qa-type, 'widget-debit')]"))

        return debitCardDivs.map { DebitCardMapper.mapFromDivOnPKUrl(it) }
    }

    override fun transferMoney(from: String, to: String, amount: BigDecimal) {
        if (from == to) {
            LOG.debug("Same names, no transfer")
            return
        }

        driver.goToIfNeed(INTERNAL_TRANSFER_URL)
        driver.findBy(By.xpath("//div[@data-qa-type='accountFrom']"))
            ?.let { setAccountInTransferDiv(it, from) }
        driver.findBy(By.xpath("//div[@data-qa-type='accountTo']"))
            ?.let { setAccountInTransferDiv(it, to) }

        val moneyInput = driver.findBy(By.xpath("//div[@data-qa-type='amount-from']"))
            ?.findElement(By.xpath(".//input[@data-qa-type='amount-from.input']"))
            ?: throw TBankClientException("Can't find input for money")
        moneyInput.sendKeys(amount.toString())

        val submitButton = driver.findBy(By.xpath("//button[@data-qa-type='submit-button']"))
            ?: throw TBankClientException("Can't find submit button")
        submitButton.click()
    }

    override fun close() {
        driver.quit()
    }

    private fun setAccountInTransferDiv(transferDiv: WebElement, name: String) {
        transferDiv.findElement(By.xpath(".//div[contains(@data-qa-type, 'selectAccount')]")).click()
        val neededOptionDiv = driver.findAllBy(By.xpath("//div[contains(@data-qa-type, 'dropdown.item') and @role='option']"))
            .find {
                val labelSpan = it.findElement(By.xpath(".//span[contains(@data-qa-type, 'dropdown.item.label')]"))
                val optionName = labelSpan.text
                optionName == name
            }
            ?: throw TBankClientException("Can't find option with name $name")

        neededOptionDiv.click()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(TBankSeleniumClient::class.java)
    }
}
