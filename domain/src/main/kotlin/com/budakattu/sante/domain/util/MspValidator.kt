package com.budakattu.sante.domain.util

import javax.inject.Inject

class MspValidator @Inject constructor() {
    fun isBelowMsp(pricePerUnit: Float, mspPerUnit: Float): Boolean = pricePerUnit < mspPerUnit
}
