package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.lang.IllegalArgumentException


object UserHolder {
    private val map = mutableMapOf<String, User>()

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder(){
        map.clear()
    }

    fun registerUser(
        fullName:String,
        email:String,
        password:String
    ):User {
        val user = User.makeUser(fullName, email = email, password = password)
        return when {
            (map.containsKey(user.login))
            -> throw IllegalArgumentException("A user with this email already exists")
            else -> user.also { map[user.login] = user }
        }
    }

    fun registerUserByPhone(fullName: String, rawPhone: String):User{
        val user = User.makeUser(fullName, phone = rawPhone)
        if (user.login.length != 12 && user.login[0].toString()=="+"){
            throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")
        }
        return when {
            (!map.containsKey(user.login))
                 -> user.also { map[user.login] = user}
            else -> throw IllegalArgumentException("A user with this phone already exists")

        }

    }

    fun loginUser (login:String, password: String) : String? {
        var newLogin:String? = null
        if (login.contains("@")) {
            newLogin = login.trim()
        }else if (login[0].toString() == "+"){
            newLogin = login.replace("[^+\\d]".toRegex(),"")
        }
        return map[newLogin]?.run {
            if (checkPassword(password)) userInfo
            else null
        }
    }




    fun requestAccessCode(login: String) : Unit {
        var newLogin:String? = null
        if (login.contains("@")) {
            newLogin = login.trim()
        }else if (login[0].toString() == "+"){
            newLogin = login.replace("[^+\\d]".toRegex(),"")
        }
         map[newLogin]?.run {
             val code = generateAccessCode()
             map[newLogin].apply {
                 accessCode = code
                 passwordHash = encrypt(code)
             }
         }

    }
    fun importUsers(list: List<String>): List<User> =
        list.map {
            val (fullName, email, access, phone) = it.split(";")
            val cuser = User.makeUserImport(fullName, email, access, phone)
                ?.also { user -> map[user.login] = user }
            cuser
        }.filterNotNull()

}
