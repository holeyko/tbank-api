# TBank API Client

Клиент-библиотека для автоматизации работы с Т-банком через Selenium WebDriver на Kotlin.

## Возможности

- ✅ Вход в ЛК
- ✅ Получение информации о дебитовых счетакх
- ✅ Получение информации о накопительных счетах
- ✅ Возможность перевода между счетами

## Основные сущности

### [TBankClientFactory](src/main/kotlin/ru/holeyko/tbankapi/clients/TBankClientFactory.kt)

Позволяет войти в аккаунт и настроить клиента с помощью метода `openConnection`.

### [TBankClient](src/main/kotlin/ru/holeyko/tbankapi/clients/TBankClient.kt)

- Позволяет получить список дебетовых карт.
- Позволяет получить список накопительных счетов.
- Позволяет переводить деньги между своими счетами.
