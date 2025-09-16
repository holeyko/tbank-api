package ru.holeyko.tbankapi.mappers

import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import ru.holeyko.tbankapi.exceptions.TBankClientException
import ru.holeyko.tbankapi.extentions.parseNumber
import ru.holeyko.tbankapi.model.DebitCard

object DebitCardMapper {
    private val DEBIT_CARD_LINK_PATTERN = """mybank/accounts/debit/(\d+)/?.*"""

    fun mapFromDivOnPKUrl(debitCardWidget: WebElement): DebitCard {
        val debitCardLink = (debitCardWidget.findElement(By.xpath(".//a[contains(@data-qa-type, 'link')]"))
            ?: throw TBankClientException("Can't find link on debit card in debit card widget"))
        val debitCardId = parseSavingIdFromLink(debitCardLink.getAttribute("href") ?: "")

        val subtitleDiv = debitCardWidget.findElement(By.xpath(".//div[@data-qa-type='subtitle']"))
            ?: throw TBankClientException("Can't find subtitle in debit card widget")
        val name = subtitleDiv.text

        val titleDiv = debitCardWidget.findElement(By.xpath(".//span[@data-qa-type='title']"))
            ?: throw TBankClientException("Can't find title in debit card widget")
        val titleElements = titleDiv.findElements(By.xpath(".//span"))
        if (titleElements.size != 1) {
            throw TBankClientException("Illegal format title in debit card widget")
        }
        val balance = titleElements[0].text.parseNumber()

        return DebitCard(debitCardId, name, balance)
    }

    private fun parseSavingIdFromLink(link: String): Long {
        return DEBIT_CARD_LINK_PATTERN.toRegex()
            .find(link)
            ?.groupValues
            ?.getOrNull(1)
            ?.toLong()
            ?: throw TBankClientException("Can't parse debit card id from link")
    }
}
