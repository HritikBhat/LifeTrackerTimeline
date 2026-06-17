package com.hritik.lifetrackertimeline.data.repository

import com.hritik.lifetrackertimeline.data.local.dao.UserDao
import com.hritik.lifetrackertimeline.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    val user: Flow<UserEntity?> = userDao.getUser()

    suspend fun saveUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun clearUser() {
        userDao.clearUser()
    }
}