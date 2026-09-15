package com.example.util

import kotlin.math.roundToLong

object NumberToWordsConverter {
    private val units = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    )

    private val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )

    fun convert(amount: Double): String {
        val totalPaise = (amount * 100).roundToLong()
        val rupees = totalPaise / 100
        val paise = totalPaise % 100

        val rupeeWords = convertRupees(rupees)
        val result = StringBuilder()

        if (rupeeWords.isEmpty()) {
            result.append("Rs. Zero")
        } else {
            result.append("Rs. ").append(rupeeWords)
        }

        if (paise > 0) {
            val paiseWords = convertRupees(paise)
            result.append(" and ").append(paiseWords).append(" Paise")
        }

        result.append(" only")
        return result.toString()
    }

    private fun convertRupees(number: Long): String {
        if (number == 0L) return ""
        if (number < 20) return units[number.toInt()]
        if (number < 100) {
            val rem = number % 10
            return tens[(number / 10).toInt()] + (if (rem > 0) " " + units[rem.toInt()] else "")
        }
        if (number < 1000) {
            val rem = number % 100
            return units[(number / 100).toInt()] + " Hundred" + (if (rem > 0) " " + convertRupees(rem) else "")
        }
        if (number < 100000) {
            val rem = number % 1000
            return convertRupees(number / 1000) + " Thousand" + (if (rem > 0) " " + convertRupees(rem) else "")
        }
        if (number < 10000000) {
            val rem = number % 100000
            return convertRupees(number / 100000) + " Lakh" + (if (rem > 0) " " + convertRupees(rem) else "")
        }
        val rem = number % 10000000
        return convertRupees(number / 10000000) + " Crore" + (if (rem > 0) " " + convertRupees(rem) else "")
    }
}
