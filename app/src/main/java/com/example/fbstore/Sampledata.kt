package com.example.fbstore

data class Image(
    val urI: String = "",
    val userId: String = "",
    val userName: String = ""
)

data class User(
    val id: String = "",
    val userName: String = ""
)

data class Missions(
    var MissionList: List<String> = listOf()
)

data class Team(
    val members: List<User> = emptyList()
)