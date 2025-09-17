package ru.holeyko.tbankapi.exceptions

open class TBankClientException(message: String?) : RuntimeException(message) {
    constructor() : this(null)
}
