package com.holeyko.tbankapi.mappers

import com.holeyko.tbankapi.exceptions.TBankClientException
import com.holeyko.tbankapi.exceptions.TBankClientIllegalStateException
import com.holeyko.tbankapi.extentions.parseNumber
import com.holeyko.tbankapi.model.Saving
import org.openqa.selenium.By
import org.openqa.selenium.WebElement

object SavingMapper {
    private val SAVING_LINK_PATTERN = """mybank/accounts/saving/(\d+)/?.*"""

    fun mapFromDivOnPKUrl(savingDiv: WebElement): Saving {
        val savingLink = savingDiv.findElement(By.xpath(".//a[contains(@data-qa-type, 'link')]"))
        val savingId = parseSavingIdFromLink(savingLink.getAttribute("href") ?: "")

        val subtitleDiv = savingDiv.findElement(By.xpath(".//div[@data-qa-type='subtitle']"))
        val name = subtitleDiv.text

        val titleDiv = savingDiv.findElement(By.xpath(".//span[@data-qa-type='title']"))
        val titleElements = titleDiv.findElements(By.xpath(".//span"))
        if (titleElements.size != 3) {
            throw TBankClientIllegalStateException("Illegal format title in saving widget")
        }

        val balance = titleElements[0].text.parseNumber()
        val limitSpan = titleElements[2].text.parseNumber()

        return Saving(savingId, name, balance, limitSpan)
    }

    private fun parseSavingIdFromLink(link: String): Long {
        return SAVING_LINK_PATTERN.toRegex()
            .find(link)
            ?.groupValues
            ?.getOrNull(1)
            ?.toLong()
            ?: throw TBankClientException("Can't parse saving id from link [link=$link]")
    }
}
