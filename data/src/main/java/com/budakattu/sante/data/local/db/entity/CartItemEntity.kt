package com.budakattu.sante.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.budakattu.sante.domain.model.CartItem
import com.budakattu.sante.domain.model.ProductAvailability

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val itemId: String,
    val userId: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val pricePerUnit: Float,
    val unit: String,
    val imageUrl: String?,
    val availability: String,
    val expectedDispatchDate: String?,
    val familyName: String,
    val village: String,
)

fun CartItemEntity.toDomain() = CartItem(
    itemId = itemId,
    productId = productId,
    productName = productName,
    quantity = quantity,
    pricePerUnit = pricePerUnit,
    unit = unit,
    imageUrl = imageUrl,
    availability = ProductAvailability.valueOf(availability),
    expectedDispatchDate = expectedDispatchDate,
    familyName = familyName,
    village = village,
)

fun CartItem.toEntity(userId: String) = CartItemEntity(
    itemId = itemId,
    userId = userId,
    productId = productId,
    productName = productName,
    quantity = quantity,
    pricePerUnit = pricePerUnit,
    unit = unit,
    imageUrl = imageUrl,
    availability = availability.name,
    expectedDispatchDate = expectedDispatchDate,
    familyName = familyName,
    village = village,
)
