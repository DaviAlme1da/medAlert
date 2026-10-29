package com.davialmeida.medalert.core.presentation

const val PROFILE_ID_ARG = "profileId"
const val MEDICATION_ID_ARG = "seriesId"

enum class Screen(val route: String) {
    ONBOARDING("onboarding"),
    MAIN("main"),
    TODAY("today"),
    MEDICATIONS("medications"),
    HISTORY("history"),
    SETTINGS("settings"),
    PROFILES("profiles"),
    CREATE_PROFILE("create_profile"),
    EDIT_PROFILE("edit_profile/{$PROFILE_ID_ARG}"),
    CREATE_MEDICATION("create_medication"),
    EDIT_MEDICATION("edit_medication/{$MEDICATION_ID_ARG}"),
    MEDICATION_DETAIL("medication/{$MEDICATION_ID_ARG}")
}

fun editProfileRoute(profileId: Long): String {
    return "edit_profile/$profileId"
}

fun editMedicationRoute(seriesId: Long): String {
    return "edit_medication/$seriesId"
}

fun medicationDetailRoute(seriesId: Long): String {
    return "medication/$seriesId"
}
