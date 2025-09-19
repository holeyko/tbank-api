package com.holeyko.tbankapi.extentions

import java.math.BigDecimal

fun String.parseNumber(): BigDecimal {
    val numberStr = filter { it.isDigit() || it == ',' || it == '.' }
        .replace(',', '.')
    return BigDecimal(numberStr)
}
