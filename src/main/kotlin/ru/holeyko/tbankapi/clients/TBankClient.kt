package ru.holeyko.tbankapi.clients

import ru.holeyko.tbankapi.model.DebitCard
import ru.holeyko.tbankapi.model.Saving
import java.math.BigDecimal

interface TBankClient : AutoCloseable {

    fun getSavings(): List<Saving>

    fun getDebitCards(): List<DebitCard>

    fun transferMoney(from: String, to: String, amount: BigDecimal)

    companion object {
        const val PK_URL = "https://www.tbank.ru/mybank"
        const val INTERNAL_TRANSFER_URL = "https://www.tbank.ru/mybank/payments/transfer-between-accounts/?" +
            "internal_source=homePayments_transferList_category"
    }
}
