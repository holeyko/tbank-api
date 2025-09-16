package ru.holeyko.tbankapi.model

import java.math.BigDecimal

data class DebitCard(
    val id: Long,
    val name: String,
    val balance: BigDecimal
)
