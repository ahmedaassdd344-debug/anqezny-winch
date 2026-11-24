package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class GizaArea(
    val id: String,
    val nameAr: String,
    val lat: Double,
    val lng: Double
)

val GIZA_AREAS = listOf(
    GizaArea("dokki", "الدقي", 30.0394, 31.2111),
    GizaArea("mohandessin", "المهندسين", 30.0561, 31.2001),
    GizaArea("agouza", "العجوزة", 30.0522, 31.2133),
    GizaArea("imbaba", "إمبابة", 30.0762, 31.2089),
    GizaArea("warraq", "الوراق", 30.0975, 31.2125),
    GizaArea("talbiya", "الطالبية", 29.9985, 31.1840),
    GizaArea("haram", "الهرم", 29.9922, 31.1489),
    GizaArea("faisal", "فيصل", 30.0086, 31.1742),
    GizaArea("hadayek_ahram", "حدائق الأهرام", 29.9723, 31.0972),
    GizaArea("sheikh_zayed", "الشيخ زايد", 30.0218, 30.9854),
    GizaArea("october_6", "6 أكتوبر", 29.9720, 30.9430),
    GizaArea("hadayek_october", "حدائق أكتوبر", 29.9277, 30.9647),
    GizaArea("abu_rawash", "أبو رواش", 30.0445, 31.0642),
    GizaArea("ausim_kerdasa", "أوسيم / كرداسة", 30.1235, 31.1258)
)

enum class VehicleType(val labelAr: String, val multiplier: Double, val surchargeEgp: Double) {
    SEDAN("ملاكي عادية", 1.0, 0.0),
    SUV("دفع رباعي SUV", 1.15, 60.0),
    LUXURY_VIP("سيارة فاخرة VIP", 1.30, 140.0),
    SPORTS_LOW("سيارة رياضية / منخفضة جداً", 1.40, 200.0);

    companion object {
        fun fromLabel(raw: String): VehicleType =
            entries.firstOrNull { it.labelAr == raw || it.name == raw } ?: SEDAN
    }
}

enum class ServiceType(val labelAr: String, val multiplier: Double, val extraEgp: Double) {
    INSTANT("إنقاذ فوري (سعر مقفول)", 1.0, 0.0),
    ENCLOSED_VIP("نقل مغلق VIP (حماية كاملة)", 1.35, 250.0),
    SCHEDULED("حجز مسبق (تحديد موعد)", 1.05, 50.0),
    B2B_FLEET("عقود التوكيلات والأساطيل (B2B)", 0.95, 0.0);

    companion object {
        fun fromLabel(raw: String): ServiceType =
            entries.firstOrNull { it.labelAr == raw || it.name == raw } ?: INSTANT
    }
}

enum class PaymentMethod(val labelAr: String, val electronic: Boolean) {
    CASH("كاش", false),
    VISA("فيزا / بطاقة بنكية", true),
    INSTAPAY("إنستا باي", true),
    BANK_TRANSFER("تحويل بنكي", true);

    companion object {
        fun fromLabel(raw: String): PaymentMethod =
            entries.firstOrNull { it.labelAr == raw || it.name == raw } ?: CASH
    }
}

data class PricingBreakdown(
    val distanceKm: Double,
    val baseFareEgp: Double,
    val distanceCostEgp: Double,
    val fuelFactorEgp: Double,
    val vehicleAndServiceExtraEgp: Double,
    val vatEgp: Double,
    val totalFareEgp: Double,
    val commissionEgp: Double,
    val driverNetEgp: Double
)

fun calculateDistanceKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(dLng / 2) * sin(dLng / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    val straight = earthRadiusKm * c
    val roadAdjusted = (straight * 1.28).coerceAtLeast(2.0)
    return (roadAdjusted * 10.0).roundToInt() / 10.0
}

fun calculateTripPricing(
    distanceKm: Double,
    vehicleType: VehicleType,
    serviceType: ServiceType,
    config: AppPricingConfig
): PricingBreakdown {
    val dieselFactor = (config.dieselPriceEgp / 20.50).coerceIn(0.6, 3.0)
    val effectivePerKm = config.perKmEgp * dieselFactor
    val distanceCost = distanceKm * effectivePerKm
    val fuelDelta = (effectivePerKm - config.perKmEgp) * distanceKm
    val rawSubtotal = (config.baseFareEgp + distanceCost) * vehicleType.multiplier * serviceType.multiplier
    val extras = vehicleType.surchargeEgp + serviceType.extraEgp
    val preTax = rawSubtotal + extras
    val vat = (preTax * 0.14).roundToInt().toDouble()
    val total = ((preTax + vat) / 5.0).roundToInt() * 5.0
    val commission = ((total * (config.commissionPercent / 100.0))).roundToInt().toDouble()
    val driverNet = (total - commission).coerceAtLeast(0.0)
    return PricingBreakdown(
        distanceKm = distanceKm,
        baseFareEgp = config.baseFareEgp,
        distanceCostEgp = (distanceKm * config.perKmEgp).roundToInt().toDouble(),
        fuelFactorEgp = fuelDelta.roundToInt().toDouble(),
        vehicleAndServiceExtraEgp = (rawSubtotal - (config.baseFareEgp + distanceCost) + extras).roundToInt().toDouble(),
        vatEgp = vat,
        totalFareEgp = total,
        commissionEgp = commission,
        driverNetEgp = driverNet
    )
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val role: String = "customer",
    val phone: String = "",
    val email: String = "",
    val acceptedTerms: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DriverProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val winchType: String = "",
    val plateNumber: String = "",
    val area: String = "",
    val status: String = "pending",
    val online: Boolean = false,
    val companyId: String? = null,
    val companyName: String? = null,
    val walletBalance: Double = 0.0,
    val withdrawableBalance: Double = 0.0,
    val totalTrips: Long = 0L,
    val ratingSum: Long = 0L,
    val ratingCount: Long = 0L,
    val ratingAvg: Double = 0.0,
    val docsSubmitted: Boolean = false,
    val acceptedTerms: Boolean = true,
    val rejectionReason: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DriverPrivateDocs(
    @DocumentId val id: String = "",
    val userId: String = "",
    val phone: String = "",
    val nationalId: String = "",
    val licenseNumber: String = "",
    val licenseExpiry: String = "",
    val docIdCardThumb: String? = null,
    val docDriverLicenseThumb: String? = null,
    val docVehicleLicenseThumb: String? = null,
    val docTruckPhotoThumb: String? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CompanyProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val companyName: String = "",
    val managerName: String = "",
    val commercialReg: String = "",
    val taxCard: String = "",
    val address: String = "",
    val area: String = "",
    val status: String = "pending",
    val acceptedTerms: Boolean = true,
    val docsSubmitted: Boolean = false,
    val rejectionReason: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CompanyPrivateDocs(
    @DocumentId val id: String = "",
    val userId: String = "",
    val phone: String = "",
    val docRegThumb: String? = null,
    val docTaxThumb: String? = null,
    val docIdThumb: String? = null,
    val docOfficeThumb: String? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CompanyDriverInvite(
    @DocumentId val id: String = "",
    val companyId: String = "",
    val companyName: String = "",
    val driverName: String = "",
    val driverPhone: String = "",
    val plateNumber: String = "",
    val winchType: String = "",
    val area: String = "",
    val linkedDriverId: String? = null,
    val status: String = "invited",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Trip(
    @DocumentId val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val driverId: String? = null,
    val driverName: String? = null,
    val driverPlate: String? = null,
    val companyId: String? = null,
    val pickupArea: String = "",
    val pickupLat: Double = 30.0444,
    val pickupLng: Double = 31.2100,
    val dropoffArea: String = "",
    val dropoffLat: Double = 30.0131,
    val dropoffLng: Double = 31.1800,
    val distanceKm: Double = 0.0,
    val vehicleType: String = "",
    val serviceType: String = "",
    val scheduledAt: String? = null,
    val paymentMethod: String = "كاش",
    val gatewayLive: Boolean = false,
    val fareEgp: Double = 0.0,
    val commissionEgp: Double = 0.0,
    val driverNetEgp: Double = 0.0,
    val pin: String = "1234",
    val status: String = "pending",
    val waitingStartedAt: Timestamp? = null,
    val photosCount: Long = 0L,
    val photo1Thumb: String? = null,
    val photo2Thumb: String? = null,
    val photo3Thumb: String? = null,
    val photo4Thumb: String? = null,
    val pinVerified: Boolean = false,
    val signaturePointsCount: Long = 0L,
    val signedByCustomer: Boolean = false,
    val cancelFeeEgp: Double = 0.0,
    val cancelReason: String? = null,
    val rating: Long? = null,
    val reviewComment: String? = null,
    val rated: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class TripMessage(
    @DocumentId val id: String = "",
    val tripId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderRole: String = "customer",
    val text: String = "",
    val participantUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DriverReview(
    @DocumentId val id: String = "",
    val tripId: String = "",
    val driverId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val stars: Long = 5L,
    val comment: String = "",
    val tags: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WalletTransaction(
    @DocumentId val id: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val type: String = "topup",
    val amountEgp: Double = 0.0,
    val method: String = "vodafone_cash",
    val accountOrRef: String = "",
    val status: String = "pending",
    val adminNote: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AppPricingConfig(
    @DocumentId val id: String = "",
    val dieselPriceEgp: Double = 20.50,
    val baseFareEgp: Double = 185.0,
    val perKmEgp: Double = 17.5,
    val commissionPercent: Double = 15.0,
    val minWalletEgp: Double = 200.0,
    val cancelFeeEgp: Double = 40.0,
    val minWithdrawEgp: Double = 100.0,
    val gatewayLive: Boolean = false,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
