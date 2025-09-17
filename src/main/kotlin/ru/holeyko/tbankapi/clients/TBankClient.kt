package ru.holeyko.tbankapi.clients

import ru.holeyko.tbankapi.model.DebitCard
import ru.holeyko.tbankapi.model.Saving
import java.math.BigDecimal

interface TBankClient : AutoCloseable {

    fun getSavings(): List<Saving>

    fun getDebitCards(): List<DebitCard>

    fun transferMoney(fromId: Long, toId: Long, amount: BigDecimal)

    companion object {
        const val PK_URL = "https://www.tbank.ru/mybank"
        const val INTERNAL_TRANSFER_TEMPLATE = "https://www.tbank.ru/mybank/payments/transfer-between-accounts/" +
            "?predefined={\"account\":\"{fromId}\", \"accountTo\": \"{toId}\", \"moneyAmount\":\"{amount}\"}" +
            "&requiredParams=[\"accountId\"]&internal_source=quick_transfers"
    }
}
