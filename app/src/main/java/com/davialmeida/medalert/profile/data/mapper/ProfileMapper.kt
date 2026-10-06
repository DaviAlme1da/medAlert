package com.davialmeida.medalert.profile.data.mapper

import com.davialmeida.medalert.profile.data.local.entity.ProfileEntity
import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.model.ProfileColor

fun ProfileEntity.toDomain(): Profile {
    return Profile(
        id = id,
        name = name,
        color = ProfileColor.valueOf(color)
    )
}

fun Profile.toEntity(): ProfileEntity {
    return ProfileEntity(
        id = id,
        name = name,
        color = color.name
    )
}