package com.hungry.restaurant.pos.data.network

import com.hungry.restaurant.pos.data.network.dto.AvailabilityRequestDto
import com.hungry.restaurant.pos.data.network.dto.CancelOrderRequestDto
import com.hungry.restaurant.pos.data.network.dto.OrderDto
import com.hungry.restaurant.pos.data.network.dto.PageDto
import com.hungry.restaurant.pos.data.network.dto.PosSettingsDto
import com.hungry.restaurant.pos.data.network.dto.ProductDto
import com.hungry.restaurant.pos.data.network.dto.RestaurantDto
import com.hungry.restaurant.pos.data.network.dto.StaffDto
import com.hungry.restaurant.pos.data.network.dto.StatsDto
import com.hungry.restaurant.pos.data.network.dto.UpdatePosSettingsRequestDto
import com.hungry.restaurant.pos.data.network.dto.UpsertStaffRequestDto
import com.hungry.restaurant.pos.data.network.dto.VerifyPinRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * The POS app's entire backend surface - every call lands under `/restaurants/me/...`,
 * which the server resolves to the CALLER's own restaurant from the bearer token
 * (see [AuthInterceptor]), never a client-supplied id.
 */
interface RestaurantPosApi {

    @GET("restaurants/me")
    suspend fun getMyRestaurant(): RestaurantDto

    @PATCH("restaurants/me/accepting-orders")
    suspend fun setAcceptingOrders(@Query("value") value: Boolean): RestaurantDto

    @GET("restaurants/me/orders")
    suspend fun listOrders(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
    ): PageDto<OrderDto>

    @GET("restaurants/me/orders/{id}")
    suspend fun getOrder(@Path("id") id: String): OrderDto

    @POST("restaurants/me/orders/{id}/confirm")
    suspend fun confirmOrder(@Path("id") id: String): OrderDto

    @POST("restaurants/me/orders/{id}/reject")
    suspend fun rejectOrder(@Path("id") id: String): OrderDto

    @POST("restaurants/me/orders/{id}/prepare")
    suspend fun prepareOrder(@Path("id") id: String): OrderDto

    @POST("restaurants/me/orders/{id}/ready")
    suspend fun readyOrder(@Path("id") id: String): OrderDto

    @POST("restaurants/me/orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") id: String, @Body body: CancelOrderRequestDto): OrderDto

    @GET("restaurants/me/menu")
    suspend fun listMenu(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 200,
    ): PageDto<ProductDto>

    @PATCH("restaurants/me/menu/{id}/availability")
    suspend fun setAvailability(@Path("id") id: String, @Body body: AvailabilityRequestDto): ProductDto

    @GET("restaurants/me/staff")
    suspend fun listStaff(): List<StaffDto>

    @POST("restaurants/me/staff")
    suspend fun createStaff(@Body body: UpsertStaffRequestDto): StaffDto

    @PUT("restaurants/me/staff/{id}")
    suspend fun updateStaff(@Path("id") id: String, @Body body: UpsertStaffRequestDto): StaffDto

    @DELETE("restaurants/me/staff/{id}")
    suspend fun deactivateStaff(@Path("id") id: String)

    /** [Response] rather than a plain return type: a wrong PIN is an ordinary 401, not an error to throw on. */
    @POST("restaurants/me/staff/{id}/verify-pin")
    suspend fun verifyPin(@Path("id") id: String, @Body body: VerifyPinRequestDto): Response<StaffDto>

    @GET("restaurants/me/pos-settings")
    suspend fun getPosSettings(): PosSettingsDto

    @PUT("restaurants/me/pos-settings")
    suspend fun updatePosSettings(@Body body: UpdatePosSettingsRequestDto): PosSettingsDto

    @GET("restaurants/me/stats")
    suspend fun getStats(@Query("period") period: String): StatsDto
}
