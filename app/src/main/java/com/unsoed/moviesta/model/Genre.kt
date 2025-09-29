package com.unsoed.moviesta.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

@Parcelize
data class Genre(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
) : Parcelable

data class GenreResponse(
    @SerializedName("genres") val genres: List<Genre>
)