package ru.holeyko.tbankapi.extentions

import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

val DEFAULT_TIMEOUT = Duration.ofSeconds(3)
val DEFAULT_CHECK_TIMEOUT = Duration.ofSeconds(1)

fun ChromeDriver.findBy(by: By, timeout: Duration = DEFAULT_TIMEOUT): WebElement? {
    return runCatching {
        WebDriverWait(this, timeout).until(
            ExpectedConditions.presenceOfElementLocated(by)
        )
    }.getOrNull()
}

fun ChromeDriver.findAllBy(by: By, timeout: Duration = DEFAULT_TIMEOUT): List<WebElement> {
    return runCatching {
        WebDriverWait(this, timeout).until(
            ExpectedConditions.presenceOfAllElementsLocatedBy(by)
        )
    }.getOrDefault(listOf())
}

fun ChromeDriver.goToIfNeed(url: String) {
    if (currentUrl?.endsWith(url) == true || currentUrl?.endsWith("$url/") == true) {
        return
    }

    get(url)
}
