package ru.holeyko.tbankapi

import org.openqa.selenium.chrome.ChromeOptions
import ru.holeyko.tbankapi.clients.TBankClient
import ru.holeyko.tbankapi.clients.TbankClientFactory
import kotlin.system.exitProcess

private var tbankClient: TBankClient? = null

fun main() {
    while (true) {
        println("Pass commands:")
        println(
            Command.entries
                .map { "- " + it.cmd }
                .joinToString("\n")
        )

        val input = readLine() ?: ""
        val command = Command.findByCmd(input)
        if (command == null) {
            println("unknown command: $input")
            println()
            continue
        }

        handleCommand(command)
    }
}

private fun handleCommand(command: Command) {
    runCatching {
        when (command) {
            Command.OPEN_CONNECT -> {
                tbankClient?.close()
                tbankClient = TbankClientFactory.openConnect(
                    ChromeOptions(),
                    // .addArguments("--headless=new"),
                    { "Your Phone" },
                    {
                        println("Enter code:")
                        readLine() ?: ""
                    },
                    { "Your password" }
                )
            }

            Command.EXIT -> {
                tbankClient?.close()
                exitProcess(0)
            }

            Command.SAVING_LIST -> {
                val savings = tbankClient?.getSavings()
                println("Savings:")
                savings?.forEach { println(it) }
                println()
            }

            Command.DEBITS -> {
                val debits = tbankClient?.getDebitCards()
                println("Debits:")
                debits?.forEach { println(it) }
                println()
            }

            Command.INTERNAL_TRANSFER -> {
                println("From:")
                val from = readLine()!!
                println("To:")
                val to = readLine()!!
                println("Money:")
                val money = readLine()?.toBigDecimal()!!

                tbankClient?.transferMoney(from, to, money)
            }

            else -> return@runCatching
        }
    }.onFailure {
        println("Exception: $it")
    }
}

enum class Command(
    val cmd: String
) {
    OPEN_CONNECT("open_cnt"),
    SAVING_LIST("savings"),
    DEBITS("debits"),
    INTERNAL_TRANSFER("transfer"),
    EXIT("exit");

    companion object {
        fun findByCmd(cmd: String) = Command
            .entries
            .find { it.cmd == cmd }
    }
}
