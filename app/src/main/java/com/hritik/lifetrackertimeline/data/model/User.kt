package com.hritik.lifetrackertimeline.data.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val createdAt: Timestamp? = null,
    val lastLoginAt: Timestamp? = null,
    val isPremium: Boolean = false
)