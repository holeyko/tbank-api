package ru.holeyko.tbankapi

import org.openqa.selenium.chrome.ChromeDriver

fun main(args: Array<String>) {
    val driver = ChromeDriver()
    
    try {
        println("Открываем сайт T-Bank...")
        driver.get("https://www.tbank.ru")
        
        // Thread.sleep(3000)
        
        println("Сайт успешно загружен: ${driver.title}")
    } catch (e: Exception) {
        println("Ошибка при загрузке сайта: ${e.message}")
    } finally {
        driver.quit()
    }
}
