package com.sanskar.eventsnap.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response model for Nager.Date public holidays API
 * API: https://date.nager.at/api/v3/PublicHolidays/{year}/{countryCode}
 */
data class HolidayResponse(
    @SerializedName("date")
    val date: String,
    @SerializedName("localName")
    val localName: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("countryCode")
    val countryCode: String,
    @SerializedName("fixed")
    val fixed: Boolean,
    @SerializedName("global")
    val global: Boolean,
    @SerializedName("counties")
    val counties: List<String>?,
    @SerializedName("launchYear")
    val launchYear: Int?,
    @SerializedName("types")
    val types: List<String>
)

fun HolidayResponse.toEvent(): Event {
    return Event(
        id = "${countryCode}_${date}_${name.replace(" ", "_")}",
        title = name,
        date = date,
        description = "Holiday in $countryCode - $localName",
        notes = "Type: ${types.joinToString(", ")}",
        source = EventSource.API
    )
}

