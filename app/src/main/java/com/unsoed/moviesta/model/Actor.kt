package com.unsoed.moviesta.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Actor(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("profile_path")
    val profilePath: String?,
    
    @SerializedName("known_for_department")
    val knownForDepartment: String?,
    
    @SerializedName("popularity")
    val popularity: Double,
    
    @SerializedName("gender")
    val gender: Int,
    
    @SerializedName("known_for")
    val knownFor: List<Film>?
) : Parcelable

@Parcelize
data class ActorResponse(
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("results")
    val actors: List<Actor>,
    
    @SerializedName("total_pages")
    val totalPages: Int,
    
    @SerializedName("total_results")
    val totalResults: Int
) : Parcelable

@Parcelize
data class ActorDetail(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("profile_path")
    val profilePath: String?,
    
    @SerializedName("biography")
    val biography: String?,
    
    @SerializedName("birthday")
    val birthday: String?,
    
    @SerializedName("place_of_birth")
    val placeOfBirth: String?,
    
    @SerializedName("known_for_department")
    val knownForDepartment: String?,
    
    @SerializedName("popularity")
    val popularity: Double,
    
    @SerializedName("gender")
    val gender: Int
) : Parcelable

@Parcelize
data class ActorCreditsResponse(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("cast")
    val films: List<Film>
) : Parcelable