# TBank API Client

Клиент-библиотека для автоматизации работы с Т-банком через Selenium WebDriver на Kotlin.

## Возможности

- ✅ Авторизация в системе Т-банк
- ✅ Получение информации о банковских счетах
- ✅ Получение истории операций с фильтрацией
- ✅ Управление сессиями WebDriver
- ✅ Гибкая конфигурация (headless/обычный режим, таймауты, размер окна)
- ✅ Обход базовых механизмов детекции автоматизации
- ✅ Подробное логирование операций
- ✅ Автоматическое управление ресурсами

## Требования

- Java 17+
- Chrome browser
- Gradle 8.4+

## Установка

1. Склонируйте репозиторий:
```bash
git clone https://github.com/holeyko/tbank-api.git
cd tbank-api
```

2. Соберите проект:
```bash
./gradlew build
```

## Быстрый старт

### Базовое использование

```kotlin
import ru.holeyko.tbankapi.TBankClient
import ru.holeyko.tbankapi.TBankResult

fun main() {
    // Создание клиента с настройками по умолчанию
    TBankClient().use { client ->
        // Авторизация
        val loginResult = client.login("your_username", "your_password")
        
        when (loginResult) {
            is TBankResult.Success -> {
                println("Авторизация успешна: ${loginResult.data}")
                
                // Получение информации о счетах
                val accountsResult = client.getAccounts()
                accountsResult.getOrNull()?.forEach { account ->
                    println("${account.accountName}: ${account.balance} ${account.currency}")
                }
                
                // Получение операций
                val transactionsResult = client.getTransactions(limit = 10)
                transactionsResult.getOrNull()?.forEach { transaction ->
                    println("${transaction.date}: ${transaction.amount} - ${transaction.description}")
                }
            }
            is TBankResult.Error -> {
                println("Ошибка авторизации: ${loginResult.message}")
            }
        }
    }
}
```

### Использование с кастомными настройками

```kotlin
import ru.holeyko.tbankapi.TBankClientBuilder

fun main() {
    val client = TBankClientBuilder()
        .headless(false) // Показывать браузер
        .windowSize(1920, 1080) // Размер окна
        .implicitWait(15) // Время ожидания элементов
        .pageLoadTimeout(45) // Время загрузки страницы
        .build()
    
    client.use { 
        // Ваш код здесь
    }
}
```

### Фильтрация операций

```kotlin
import ru.holeyko.tbankapi.model.TransactionFilter
import ru.holeyko.tbankapi.model.TransactionType
import java.math.BigDecimal
import java.time.LocalDateTime

val filter = TransactionFilter(
    fromDate = LocalDateTime.now().minusDays(30), // За последний месяц
    toDate = LocalDateTime.now(),
    minAmount = BigDecimal("1000.00"), // Суммы от 1000 рублей
    operationType = TransactionType.DEBIT // Только списания
)

val result = client.getTransactions(filter = filter, limit = 100)
```

## API Reference

### TBankClient

Основной класс для работы с Т-банком.

#### Методы

- `login(username: String, password: String): TBankResult<String>` - Авторизация
- `getAccounts(): TBankResult<List<AccountInfo>>` - Получение списка счетов
- `getTransactions(filter: TransactionFilter?, limit: Int): TBankResult<List<Transaction>>` - Получение операций
- `isLoggedIn(): Boolean` - Проверка статуса авторизации
- `getCurrentUrl(): String?` - Текущий URL страницы
- `getPageTitle(): String?` - Заголовок текущей страницы
- `getSessionInfo(): Map<String, Any>` - Информация о сессии
- `close()` - Закрытие сессии и браузера

### TBankClientBuilder

Builder для создания клиента с кастомными настройками.

#### Методы

- `headless(headless: Boolean)` - Режим браузера (true = скрытый, false = видимый)
- `implicitWait(seconds: Long)` - Время неявного ожидания элементов
- `pageLoadTimeout(seconds: Long)` - Таймаут загрузки страницы
- `scriptTimeout(seconds: Long)` - Таймаут выполнения скриптов
- `windowSize(width: Int, height: Int)` - Размер окна браузера
- `userAgent(userAgent: String)` - User-Agent браузера
- `build(): TBankClient` - Создание клиента

### TBankResult<T>

Sealed class для результатов операций.

```kotlin
sealed class TBankResult<out T> {
    data class Success<T>(val data: T) : TBankResult<T>()
    data class Error(val message: String) : TBankResult<Nothing>()
    
    val isSuccess: Boolean
    val isError: Boolean
    fun getOrNull(): T?
    fun getOrThrow(): T
}
```

### Модели данных

#### AccountInfo
```kotlin
data class AccountInfo(
    val accountNumber: String,
    val accountName: String,
    val balance: BigDecimal,
    val currency: String,
    val accountType: String,
    val isActive: Boolean
)
```

#### Transaction
```kotlin
data class Transaction(
    val id: String,
    val date: LocalDateTime,
    val description: String,
    val amount: BigDecimal,
    val currency: String,
    val category: String?,
    val counterparty: String?,
    val operationType: TransactionType
)
```

#### TransactionFilter
```kotlin
data class TransactionFilter(
    val fromDate: LocalDateTime? = null,
    val toDate: LocalDateTime? = null,
    val accountNumber: String? = null,
    val minAmount: BigDecimal? = null,
    val maxAmount: BigDecimal? = null,
    val operationType: TransactionType? = null
)
```

## Конфигурация

### Настройки по умолчанию

```kotlin
data class TBankClientConfig(
    val implicitWait: Long = 10, // секунд
    val pageLoadTimeout: Long = 30, // секунд
    val scriptTimeout: Long = 30, // секунд
    val headless: Boolean = true, // скрытый режим
    val windowWidth: Int = 1920,
    val windowHeight: Int = 1080,
    val userAgent: String = "Mozilla/5.0 ..." // Chrome на macOS
)
```

### Логирование

Проект использует библиотеку `kotlin-logging` с backend `logback`. Для настройки логирования создайте файл `logback.xml` в `src/main/resources/`:

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <logger name="ru.holeyko.tbankapi" level="DEBUG" />
    <logger name="org.selenium" level="WARN" />
    
    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

## Примеры использования

Полные примеры использования находятся в файле `src/main/kotlin/ru/holeyko/tbankapi/examples/ExampleUsage.kt`.

Для запуска примеров:

```bash
./gradlew run
```

## Безопасность

⚠️ **Важно**: 
- Никогда не храните логины и пароли в коде
- Используйте переменные окружения или конфигурационные файлы
- Будьте осторожны с логированием чувствительных данных
- Этот инструмент предназначен для автоматизации личных финансов, использование для других целей может нарушать условия использования банка

```kotlin
// Пример безопасного использования
val username = System.getenv("TBANK_USERNAME") ?: error("TBANK_USERNAME not set")
val password = System.getenv("TBANK_PASSWORD") ?: error("TBANK_PASSWORD not set")

TBankClient().use { client ->
    client.login(username, password)
    // ...
}
```

## Тестирование

```bash
# Запуск тестов
./gradlew test

# Запуск с отчетом покрытия
./gradlew test jacocoTestReport
```

## Ограничения

- Поддержка только Chrome WebDriver
- Работа с веб-интерфейсом Т-банка (может сломаться при обновлениях сайта)
- Требует стабильного интернет-соединения
- Селекторы элементов могут потребовать обновления при изменении сайта

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Disclaimer

Данный проект создан исключительно для образовательных и исследовательских целей. Автор не несет ответственности за использование данного инструмента. Пользователи должны соблюдать условия использования Т-банка и применимое законодательство.
