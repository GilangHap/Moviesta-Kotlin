package com.unsoed.moviesta.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize // Import yang diperlukan

// Ubah dari data class biasa menjadi Parcelable

@Parcelize
data class Cast(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,         // Nama asli aktor
    @SerializedName("character") val character: String, // Nama karakter yang diperankan
    @SerializedName("profile_path") val profilePath: String? // Foto profil aktor
) : Parcelable

// 2. Model untuk Respons Credits
data class CreditsResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("cast") val cast: List<Cast>
)

// Model untuk Detail Film (dengan genre)
@Parcelize
data class FilmDetail(
    val id: Int,
    val title: String,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("runtime") val runtime: Int?,
    val genres: List<Genre>
) : Parcelable {
    // Computed properties
    val rating: Double get() = voteAverage
    val sinopsis: String? get() = overview
}

@Parcelize // Anotasi wajib
data class Film(
    val id: Int,
    val title: String,
    @SerializedName("overview") val sinopsis: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("vote_average") val voteAverage: Double
) : Parcelable {
    // Computed property untuk rating (agar tidak break existing code)
    val rating: Double get() = voteAverage
}

// Model untuk respons utama dari API (daftar film)
data class FilmResponse(
    @SerializedName("results") val films: List<Film>
)