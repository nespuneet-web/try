package com.example.ui.calculator

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object NumberFormatter {
  fun formatNumber(value: Double): String {
    if (!value.isFinite()) return value.toString()
    val bd = BigDecimal.valueOf(value).stripTrailingZeros()
    val plain = bd.toPlainString()
    return if (plain == "-0") "0" else plain
  }

  fun formatDisplay(value: Double): String {
    if (!value.isFinite()) return value.toString()
    val symbols = DecimalFormatSymbols(Locale.US)
    val formatter = DecimalFormat("#,##0.##########", symbols)
    val formatted = formatter.format(value)
    return if (formatted == "-0") "0" else formatted
  }

  fun parseDoubleStrict(raw: String): Double? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null
    val parsed = trimmed.toDoubleOrNull() ?: return null
    return if (parsed.isFinite()) parsed else null
  }
}
