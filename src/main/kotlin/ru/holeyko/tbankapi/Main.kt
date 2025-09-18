package ru.holeyko.tbankapi

import org.openqa.selenium.chrome.ChromeOptions
import ru.holeyko.tbankapi.clients.TBankClient
import ru.holeyko.tbankapi.clients.TBankClientFactory
import java.io.File
import kotlin.system.exitProcess

private var tbankClient: TBankClient? = null

fun main() {
    while (true) {
        println("Pass commands by number:")
        Command.entries.forEachIndexed { i, cmd ->
            println("${i + 1}) ${cmd.name}")
        }

        val command = readLine()?.trim()?.toIntOrNull()
            ?.let { Command.byId(it - 1) }
        if (command == null) {
            println("unknown command index")
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
                tbankClient = TBankClientFactory.openConnection(
                    File("creds/phone.txt").bufferedReader().readLine().trim(), // Own phone
                    {
                        println("Enter code:")
                        readLine() ?: ""
                    },
                    File("creds/password.txt").bufferedReader().readLine().trim(), // Own password
                    File("creds/fastcode.txt").bufferedReader().readLine().trim(),
                    ChromeOptions(),
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
                println("FromId ToId Amount")
                val input = readLine()?.split(" ")!!
                if (input.size != 3) {
                    println("Should 3 arguments")
                    return
                }
                val from = input[0].toLong()
                val to = input[1].toLong()
                val money = input[2].toBigDecimal()

                tbankClient?.transferMoney(from, to, money)
            }
        }
    }.onFailure {
        println("Exception: $it")
    }
}

enum class Command {
    OPEN_CONNECT,
    SAVING_LIST,
    DEBITS,
    INTERNAL_TRANSFER,
    EXIT;

    companion object {
        fun byId(index: Int) = Command.entries.getOrNull(index)
    }
}
