package com.example.ui.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NumberFormatterTest {

  @Test
  fun parseDoubleStrict_parsesValidNumbersAndRejectsInvalid() {
    assertEquals(42.0, NumberFormatter.parseDoubleStrict("42")!!, 0.0)
    assertEquals(-3.5, NumberFormatter.parseDoubleStrict(" -3.5 ")!!, 0.0)
    assertNull(NumberFormatter.parseDoubleStrict(""))
    assertNull(NumberFormatter.parseDoubleStrict("   "))
    assertNull(NumberFormatter.parseDoubleStrict("abc"))
    assertNull(NumberFormatter.parseDoubleStrict("NaN"))
    assertNull(NumberFormatter.parseDoubleStrict("Infinity"))
  }

  @Test
  fun formatDisplay_stripsTrailingZerosAndFormatsThousands() {
    assertEquals("42", NumberFormatter.formatDisplay(42.0))
    assertEquals("1,250.75", NumberFormatter.formatDisplay(1250.75))
    assertEquals("0", NumberFormatter.formatDisplay(-0.0))
  }
}
