package com.budakattu.sante.domain.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MspValidatorTest {
    private val validator = MspValidator()

    @Test
    fun returnsTrueWhenPriceIsBelowMsp() {
        assertTrue(validator.isBelowMsp(pricePerUnit = 100f, mspPerUnit = 120f))
    }

    @Test
    fun returnsFalseWhenPriceMeetsMsp() {
        assertFalse(validator.isBelowMsp(pricePerUnit = 120f, mspPerUnit = 120f))
    }
}
