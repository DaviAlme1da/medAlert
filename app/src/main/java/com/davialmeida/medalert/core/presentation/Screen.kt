package com.davialmeida.medalert.core.presentation

const val PROFILE_ID_ARG = "profileId"

enum class Screen(val route: String) {
    MAIN("main"),
    TODAY("today"),
    MEDICATIONS("medications"),
    HISTORY("history"),
    SETTINGS("settings"),
    PROFILES("profiles"),
    CREATE_PROFILE("create_profile"),
    EDIT_PROFILE("edit_profile/{$PROFILE_ID_ARG}")
}

fun editProfileRoute(profileId: Long): String {
    return "edit_profile/$profileId"
}