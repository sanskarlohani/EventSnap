package com.sanskar.eventsnap.data.remote

import com.sanskar.eventsnap.data.model.HolidayResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit interface for Nager.Date Public Holidays API
 * Documentation: https://date.nager.at/swagger/index.html
 */
interface HolidayApiService {

    @GET("PublicHolidays/{year}/{countryCode}")
    suspend fun getPublicHolidays(
        @Path("year") year: Int,
        @Path("countryCode") countryCode: String = "US"
    ): List<HolidayResponse>
}
