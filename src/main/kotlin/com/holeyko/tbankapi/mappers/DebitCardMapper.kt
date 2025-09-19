package com.holeyko.tbankapi.mappers

import com.holeyko.tbankapi.exceptions.TBankClientIllegalStateException
import com.holeyko.tbankapi.extentions.parseNumber
import com.holeyko.tbankapi.model.DebitCard
import org.openqa.selenium.By
import org.openqa.selenium.WebElement

object DebitCardMapper {
    private val DEBIT_CARD_LINK_PATTERN = """mybank/accounts/debit/(\d+)/?.*"""

    fun mapFromDivOnPKUrl(debitCardWidget: WebElement): DebitCard {
        val debitCardLink = debitCardWidget.findElement(By.xpath(".//a[contains(@data-qa-type, 'link')]"))
        val debitCardId = parseSavingIdFromLink(debitCardLink.getAttribute("href") ?: "")

        val subtitleDiv = debitCardWidget.findElement(By.xpath(".//div[@data-qa-type='subtitle']"))
        val name = subtitleDiv.text

        val titleDiv = debitCardWidget.findElement(By.xpath(".//span[@data-qa-type='title']"))
        val titleElements = titleDiv.findElements(By.xpath(".//span"))
        if (titleElements.size != 1) {
            throw TBankClientIllegalStateException("Illegal format title in debit card widget")
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
            ?: throw TBankClientIllegalStateException("Can't parse debit card id from link [link=$link]")
    }
}
