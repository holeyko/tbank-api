package com.holeyko.tbankapi.model

import java.math.BigDecimal

data class Saving(
    val id: Long,
    val name: String,
    val balance: BigDecimal,
    val limit: BigDecimal
)
