package com.hritik.lifetrackertimeline.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val createdAt: Timestamp? = null,
    val lastLoginAt: Timestamp? = null,
    @get:PropertyName("isPremium")
    @set:PropertyName("isPremium")
    @JvmField
    var isPremium: Boolean = false,
    val purchaseToken: String? = null,
    val productId: String? = null,
    val purchaseTime: Timestamp? = null
)
