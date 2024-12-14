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

data class PostModel(
    val postId: String,
    val user: String,
    var imageUrl: String,
    val mission: String,
    val comments: List<String>,
)

data class TeamData(
    val teamName: String,
    val members: List<String>,
    val password: String = "", //isEmpty()ならpublic
)