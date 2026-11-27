package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AppPricingConfig
import com.example.data.model.CompanyDriverInvite
import com.example.data.model.CompanyPrivateDocs
import com.example.data.model.CompanyProfile
import com.example.data.model.DriverPrivateDocs
import com.example.data.model.DriverProfile
import com.example.data.model.DriverReview
import com.example.data.model.Trip
import com.example.data.model.TripMessage
import com.example.data.model.UserProfile
import com.example.data.model.WalletTransaction
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToInt

class AnqeznyRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("يجب تسجيل الدخول أولاً قبل الوصول لقاعدة البيانات")

    fun isCurrentUserBootstrappedAdmin(): Boolean {
        val user = auth.currentUser ?: return false
        return user.isEmailVerified && user.email.equals("ahmedaassdd344@gmail.com", ignoreCase = true)
    }

    suspend fun checkIsAdmin(uid: String): Boolean {
        if (isCurrentUserBootstrappedAdmin()) return true
        return try {
            val snap = db.collection("admins").document(uid).get().await()
            snap.exists()
        } catch (_: Exception) {
            false
        }
    }

    // --- OBSERVE STREAMS ---

    fun observeUserProfile(uid: String): Flow<UserProfile?> =
        db.collection("users").document(uid)
            .snapshots()
            .map { it.toObject(UserProfile::class.java, ESTIMATE) }

    fun observeDriver(uid: String): Flow<DriverProfile?> =
        db.collection("drivers").document(uid)
            .snapshots()
            .map { it.toObject(DriverProfile::class.java, ESTIMATE) }

    fun observeDriverPrivateDocs(uid: String): Flow<DriverPrivateDocs?> =
        db.collection("drivers").document(uid).collection("private").document("documents")
            .snapshots()
            .map { it.toObject(DriverPrivateDocs::class.java, ESTIMATE) }

    fun observeApprovedDrivers(): Flow<List<DriverProfile>> =
        db.collection("drivers")
            .whereEqualTo("status", "approved")
            .snapshots()
            .map { snap -> snap.toObjects(DriverProfile::class.java, ESTIMATE) }

    fun observeAllDrivers(): Flow<List<DriverProfile>> =
        db.collection("drivers")
            .snapshots()
            .map { snap -> snap.toObjects(DriverProfile::class.java, ESTIMATE) }

    fun observeCompanyDrivers(companyId: String): Flow<List<DriverProfile>> =
        db.collection("drivers")
            .whereEqualTo("companyId", companyId)
            .snapshots()
            .map { snap -> snap.toObjects(DriverProfile::class.java, ESTIMATE) }

    fun observeCompany(uid: String): Flow<CompanyProfile?> =
        db.collection("companies").document(uid)
            .snapshots()
            .map { it.toObject(CompanyProfile::class.java, ESTIMATE) }

    fun observeCompanyPrivateDocs(uid: String): Flow<CompanyPrivateDocs?> =
        db.collection("companies").document(uid).collection("private").document("documents")
            .snapshots()
            .map { it.toObject(CompanyPrivateDocs::class.java, ESTIMATE) }

    fun observeAllCompanies(): Flow<List<CompanyProfile>> =
        db.collection("companies")
            .snapshots()
            .map { snap -> snap.toObjects(CompanyProfile::class.java, ESTIMATE) }

    fun observeCompanyInvites(companyId: String): Flow<List<CompanyDriverInvite>> =
        db.collection("company_drivers")
            .whereEqualTo("companyId", companyId)
            .snapshots()
            .map { snap -> snap.toObjects(CompanyDriverInvite::class.java, ESTIMATE) }

    fun observeCustomerTrips(uid: String): Flow<List<Trip>> =
        db.collection("trips")
            .whereEqualTo("customerId", uid)
            .snapshots()
            .map { snap -> snap.toObjects(Trip::class.java, ESTIMATE) }

    fun observePendingTrips(): Flow<List<Trip>> =
        db.collection("trips")
            .whereEqualTo("status", "pending")
            .snapshots()
            .map { snap -> snap.toObjects(Trip::class.java, ESTIMATE) }

    fun observeDriverTrips(driverId: String): Flow<List<Trip>> =
        db.collection("trips")
            .whereEqualTo("driverId", driverId)
            .snapshots()
            .map { snap -> snap.toObjects(Trip::class.java, ESTIMATE) }

    fun observeCompanyTrips(companyId: String): Flow<List<Trip>> =
        db.collection("trips")
            .whereEqualTo("companyId", companyId)
            .snapshots()
            .map { snap -> snap.toObjects(Trip::class.java, ESTIMATE) }

    fun observeAllTrips(): Flow<List<Trip>> =
        db.collection("trips")
            .snapshots()
            .map { snap -> snap.toObjects(Trip::class.java, ESTIMATE) }

    fun observeTripMessages(tripId: String, currentUid: String): Flow<List<TripMessage>> =
        db.collection("trips").document(tripId).collection("messages")
            .whereArrayContains("participantUids", currentUid)
            .snapshots()
            .map { snap -> snap.toObjects(TripMessage::class.java, ESTIMATE) }

    fun observeDriverReviews(driverId: String): Flow<List<DriverReview>> =
        db.collection("reviews")
            .whereEqualTo("driverId", driverId)
            .snapshots()
            .map { snap -> snap.toObjects(DriverReview::class.java, ESTIMATE) }

    fun observeDriverWalletTransactions(driverId: String): Flow<List<WalletTransaction>> =
        db.collection("wallet_transactions")
            .whereEqualTo("driverId", driverId)
            .snapshots()
            .map { snap -> snap.toObjects(WalletTransaction::class.java, ESTIMATE) }

    fun observeAllWalletTransactions(): Flow<List<WalletTransaction>> =
        db.collection("wallet_transactions")
            .snapshots()
            .map { snap -> snap.toObjects(WalletTransaction::class.java, ESTIMATE) }

    fun observePricingConfig(): Flow<AppPricingConfig> =
        db.collection("app_config").document("pricing")
            .snapshots()
            .map { snap -> snap.toObject(AppPricingConfig::class.java, ESTIMATE) ?: AppPricingConfig() }

    // --- MUTATIONS (Crash-Safe Result<T>) ---

    suspend fun saveUserProfile(
        displayName: String,
        role: String,
        phone: String,
        acceptedTerms: Boolean
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("غير مسجل"))
        val path = "users/$uid"
        return try {
            require(acceptedTerms) { "يجب الموافقة على الشروط والأحكام" }
            require(displayName.trim().length in 2..80) { "الاسم يجب أن يكون بين 2 و 80 حرف" }
            val docRef = db.document(path)
            val existing = docRef.get().await()
            if (existing.exists()) {
                val updates = mapOf(
                    "displayName" to displayName.trim(),
                    "role" to role,
                    "phone" to phone.trim().take(20),
                    "acceptedTerms" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            } else {
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to displayName.trim(),
                    "role" to role,
                    "phone" to phone.trim().take(20),
                    "email" to auth.currentUser?.email,
                    "acceptedTerms" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null } as Map<String, Any>
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun persistActiveRole(
        role: String,
        fallbackName: String,
        fallbackPhone: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("غير مسجل"))
        val path = "users/$uid"
        return try {
            val docRef = db.document(path)
            val existing = docRef.get().await()
            if (existing.exists()) {
                val updates = mutableMapOf<String, Any>(
                    "role" to role,
                    "acceptedTerms" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (fallbackName.trim().length >= 2) {
                    updates["displayName"] = fallbackName.trim().take(80)
                }
                if (fallbackPhone.trim().isNotBlank()) {
                    updates["phone"] = fallbackPhone.trim().take(20)
                }
                docRef.update(updates).await()
            } else {
                val safeName = fallbackName.trim().take(80).ifBlank {
                    auth.currentUser?.displayName?.take(80)?.ifBlank { "مستخدم أنقذني" } ?: "مستخدم أنقذني"
                }
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "role" to role,
                    "phone" to fallbackPhone.trim().take(20),
                    "email" to auth.currentUser?.email,
                    "acceptedTerms" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null } as Map<String, Any>
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun registerDriver(
        name: String,
        phone: String,
        nationalId: String,
        licenseNumber: String,
        licenseExpiry: String,
        winchType: String,
        plateNumber: String,
        area: String,
        companyId: String?,
        companyName: String?,
        docIdThumb: String?,
        docDriverLicenseThumb: String?,
        docVehicleLicenseThumb: String?,
        docTruckPhotoThumb: String?
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("غير مسجل"))
        val driverPath = "drivers/$uid"
        val privatePath = "drivers/$uid/private/documents"
        return try {
            require(name.trim().length >= 2) { "الاسم مطلوب" }
            require(phone.trim().length >= 6) { "رقم الموبايل مطلوب" }
            require(nationalId.trim().length >= 6) { "الرقم القومي مطلوب" }
            require(licenseNumber.trim().length >= 3) { "رقم الرخصة مطلوب" }
            require(winchType.trim().length >= 2) { "نوع الونش مطلوب" }
            require(plateNumber.trim().length >= 2) { "رقم اللوحة مطلوب" }

            val existingSnap = db.document(driverPath).get().await()
            if (existingSnap.exists()) {
                val updates = mutableMapOf<String, Any>(
                    "name" to name.trim().take(80),
                    "phone" to phone.trim().take(20),
                    "winchType" to winchType.trim().take(80),
                    "plateNumber" to plateNumber.trim().take(30),
                    "area" to area.trim().take(60),
                    "docsSubmitted" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (!companyId.isNullOrBlank()) updates["companyId"] = companyId.trim()
                if (!companyName.isNullOrBlank()) updates["companyName"] = companyName.trim()
                if (existingSnap.getString("status") == "rejected") {
                    updates["status"] = "pending"
                }
                db.document(driverPath).update(updates).await()
            } else {
                val publicPayload = mapOf(
                    "userId" to uid,
                    "name" to name.trim().take(80),
                    "phone" to phone.trim().take(20),
                    "winchType" to winchType.trim().take(80),
                    "plateNumber" to plateNumber.trim().take(30),
                    "area" to area.trim().take(60),
                    "status" to "pending",
                    "online" to false,
                    "companyId" to companyId?.trim()?.takeIf { it.isNotEmpty() },
                    "companyName" to companyName?.trim()?.takeIf { it.isNotEmpty() },
                    "walletBalance" to 0.0,
                    "withdrawableBalance" to 0.0,
                    "totalTrips" to 0L,
                    "ratingSum" to 0L,
                    "ratingCount" to 0L,
                    "ratingAvg" to 0.0,
                    "docsSubmitted" to true,
                    "acceptedTerms" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null } as Map<String, Any>
                db.document(driverPath).set(publicPayload).await()
            }

            val privatePayload = mapOf(
                "userId" to uid,
                "phone" to phone.trim().take(20),
                "nationalId" to nationalId.trim().take(20),
                "licenseNumber" to licenseNumber.trim().take(30),
                "licenseExpiry" to licenseExpiry.trim().take(20),
                "docIdCardThumb" to docIdThumb?.takeIf { it.isNotEmpty() },
                "docDriverLicenseThumb" to docDriverLicenseThumb?.takeIf { it.isNotEmpty() },
                "docVehicleLicenseThumb" to docVehicleLicenseThumb?.takeIf { it.isNotEmpty() },
                "docTruckPhotoThumb" to docTruckPhotoThumb?.takeIf { it.isNotEmpty() },
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>
            db.document(privatePath).set(privatePayload).await()

            saveUserProfile(name.trim(), "driver", phone.trim(), acceptedTerms = true)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, driverPath)
            Result.failure(e)
        }
    }

       suspend fun toggleDriverOnline(online: Boolean): Result<Unit> {
        val uid = requireUserId()
        return toggleDriverOnlineForId(uid, online)
    }

    suspend fun toggleDriverOnlineForId(driverId: String, online: Boolean): Result<Unit> {
        val path = "drivers/$driverId"
        return try {
            db.document(path).update(
                mapOf(
                    "online" to online,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }


    suspend fun registerCompany(
        companyName: String,
        managerName: String,
        phone: String,
        commercialReg: String,
        taxCard: String,
        address: String,
        area: String,
        docRegThumb: String?,
        docTaxThumb: String?,
        docIdThumb: String?,
        docOfficeThumb: String?
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("غير مسجل"))
        val companyPath = "companies/$uid"
        val privatePath = "companies/$uid/private/documents"
        return try {
            require(companyName.trim().length >= 2) { "اسم الشركة مطلوب" }
            require(managerName.trim().length >= 2) { "اسم المسؤول مطلوب" }
            require(commercialReg.trim().length >= 3) { "السجل التجاري مطلوب" }
            require(taxCard.trim().length >= 3) { "البطاقة الضريبية مطلوبة" }

            val existing = db.document(companyPath).get().await()
            if (existing.exists()) {
                val updates = mutableMapOf<String, Any>(
                    "companyName" to companyName.trim().take(100),
                    "managerName" to managerName.trim().take(80),
                    "commercialReg" to commercialReg.trim().take(40),
                    "taxCard" to taxCard.trim().take(40),
                    "address" to address.trim().take(150),
                    "area" to area.trim().take(60),
                    "docsSubmitted" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (existing.getString("status") == "rejected") {
                    updates["status"] = "pending"
                }
                db.document(companyPath).update(updates).await()
            } else {
                val payload = mapOf(
                    "userId" to uid,
                    "companyName" to companyName.trim().take(100),
                    "managerName" to managerName.trim().take(80),
                    "commercialReg" to commercialReg.trim().take(40),
                    "taxCard" to taxCard.trim().take(40),
                    "address" to address.trim().take(150),
                    "area" to area.trim().take(60),
                    "status" to "pending",
                    "acceptedTerms" to true,
                    "docsSubmitted" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                db.document(companyPath).set(payload).await()
            }

            val privPayload = mapOf(
                "userId" to uid,
                "phone" to phone.trim().take(20),
                "docRegThumb" to docRegThumb?.takeIf { it.isNotEmpty() },
                "docTaxThumb" to docTaxThumb?.takeIf { it.isNotEmpty() },
                "docIdThumb" to docIdThumb?.takeIf { it.isNotEmpty() },
                "docOfficeThumb" to docOfficeThumb?.takeIf { it.isNotEmpty() },
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>
            db.document(privatePath).set(privPayload).await()

            saveUserProfile(managerName.trim(), "company", phone.trim(), acceptedTerms = true)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, companyPath)
            Result.failure(e)
        }
    }

    suspend fun addCompanyDriverInvite(
        companyName: String,
        driverName: String,
        driverPhone: String,
        plateNumber: String,
        winchType: String,
        area: String
    ): Result<Unit> {
        val uid = requireUserId()
        val ref = db.collection("company_drivers").document()
        return try {
            val payload = mapOf(
                "companyId" to uid,
                "companyName" to companyName.trim().take(100),
                "driverName" to driverName.trim().take(80),
                "driverPhone" to driverPhone.trim().take(20),
                "plateNumber" to plateNumber.trim().take(30),
                "winchType" to winchType.trim().take(80),
                "area" to area.trim().take(60),
                "status" to "invited",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            ref.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun createTrip(
        customerName: String,
        customerPhone: String,
        pickupArea: String,
        pickupLat: Double,
        pickupLng: Double,
        dropoffArea: String,
        dropoffLat: Double,
        dropoffLng: Double,
        distanceKm: Double,
        vehicleType: String,
        serviceType: String,
        scheduledAt: String?,
        paymentMethod: String,
        gatewayLive: Boolean,
        fareEgp: Double,
        commissionEgp: Double,
        driverNetEgp: Double,
        assignedDriver: DriverProfile? = null
    ): Result<String> {
        val uid = requireUserId()
        val ref = db.collection("trips").document()
        return try {
            val pin = (1000..9999).random().toString()
            val status = if (assignedDriver != null) "accepted" else "pending"
            val payload = mutableMapOf<String, Any?>(
                "customerId" to uid,
                "customerName" to customerName.trim().take(80),
                "customerPhone" to customerPhone.trim().take(20),
                "pickupArea" to pickupArea.trim().take(100),
                "pickupLat" to pickupLat,
                "pickupLng" to pickupLng,
                "dropoffArea" to dropoffArea.trim().take(100),
                "dropoffLat" to dropoffLat,
                "dropoffLng" to dropoffLng,
                "distanceKm" to distanceKm,
                "vehicleType" to vehicleType,
                "serviceType" to serviceType,
                "scheduledAt" to scheduledAt?.trim()?.takeIf { it.isNotEmpty() },
                "paymentMethod" to paymentMethod,
                "gatewayLive" to gatewayLive,
                "fareEgp" to fareEgp,
                "commissionEgp" to commissionEgp,
                "driverNetEgp" to driverNetEgp,
                "pin" to pin,
                "status" to status,
                "photosCount" to 0L,
                "pinVerified" to false,
                "signaturePointsCount" to 0L,
                "signedByCustomer" to false,
                "cancelFeeEgp" to 0.0,
                "rated" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (assignedDriver != null) {
                payload["driverId"] = assignedDriver.userId
                payload["driverName"] = assignedDriver.name
                payload["driverPlate"] = assignedDriver.plateNumber
                if (!assignedDriver.companyId.isNullOrBlank()) {
                    payload["companyId"] = assignedDriver.companyId
                }
            }
            val cleanPayload = payload.filterValues { it != null } as Map<String, Any>
            ref.set(cleanPayload).await()
            Result.success(ref.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun acceptTrip(tripId: String, driver: DriverProfile): Result<Unit> {
        val uid = requireUserId()
        val path = "trips/$tripId"
        return try {
            val payload = mutableMapOf<String, Any>(
                "driverId" to uid,
                "driverName" to driver.name,
                "driverPlate" to driver.plateNumber,
                "status" to "accepted",
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!driver.companyId.isNullOrBlank()) {
                payload["companyId"] = driver.companyId
            }
            db.document(path).update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun markArrived(tripId: String): Result<Unit> {
        val path = "trips/$tripId"
        return try {
            requireUserId()
            db.document(path).update(
                mapOf(
                    "status" to "arrived",
                    "waitingStartedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun saveTripPhotosAndConfirmLoaded(
        tripId: String,
        photos: List<String>,
        pinVerified: Boolean
    ): Result<Unit> {
        val path = "trips/$tripId"
        return try {
            requireUserId()
            val updates = mutableMapOf<String, Any>(
                "photosCount" to photos.size.coerceAtMost(4).toLong(),
                "pinVerified" to pinVerified,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (photos.isNotEmpty()) updates["photo1Thumb"] = photos[0]
            if (photos.size >= 2) updates["photo2Thumb"] = photos[1]
            if (photos.size >= 3) updates["photo3Thumb"] = photos[2]
            if (photos.size >= 4) updates["photo4Thumb"] = photos[3]
            if (pinVerified) {
                updates["status"] = "loaded"
            }
            db.document(path).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

        suspend fun requestWalletTransaction(
        driver: DriverProfile,
        type: String,
        amountEgp: Double,
        method: String,
        accountOrRef: String
    ): Result<Unit> {
        val uid = requireUserId()
        val realDriverId = driver.userId.ifBlank { uid }
        val ref = db.collection("wallet_transactions").document()
        return try {
            require(amountEgp > 0) { "المبلغ غير صحيح" }
            require(accountOrRef.trim().length >= 3) { "رقم المرجع أو الحساب مطلوب" }
            if (type == "withdrawal") {
                require(amountEgp <= driver.withdrawableBalance) { "المبلغ المطلوب أكبر من الأرباح المتاحة للسحب" }
            }
            ref.set(
                mapOf(
                    "driverId" to realDriverId,
                    "driverName" to driver.name,
                    "type" to type,
                    "amountEgp" to amountEgp,
                    "method" to method,
                    "accountOrRef" to accountOrRef.trim().take(100),
                    "status" to "pending",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun cancelTrip(trip: Trip, cancelFeeEgp: Double, reason: String): Result<Unit> {
        val path = "trips/${trip.id}"
        return try {
            requireUserId()
            val fee = if (trip.status == "accepted" || trip.status == "arrived") cancelFeeEgp else 0.0
            db.document(path).update(
                mapOf(
                    "status" to "cancelled",
                    "cancelFeeEgp" to fee,
                    "cancelReason" to reason.trim().take(200),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun submitDriverRatingAndReview(
        trip: Trip,
        stars: Int,
        comment: String,
        tags: List<String>
    ): Result<Unit> {
        val uid = requireUserId()
        val tripPath = "trips/${trip.id}"
        return try {
            require(stars in 1..5) { "التقييم يجب أن يكون بين 1 و 5 نجوم" }
            val driverId = trip.driverId ?: return Result.failure(IllegalStateException("لا يوجد سائق للرحلة"))
            val cleanComment = comment.trim().take(300)

            db.document(tripPath).update(
                mapOf(
                    "rating" to stars.toLong(),
                    "reviewComment" to cleanComment,
                    "rated" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val reviewRef = db.collection("reviews").document("rev_${trip.id}")
            reviewRef.set(
                mapOf(
                    "tripId" to trip.id,
                    "driverId" to driverId,
                    "customerId" to uid,
                    "customerName" to trip.customerName.ifBlank { "عميل أنقذني" },
                    "stars" to stars.toLong(),
                    "comment" to cleanComment,
                    "tags" to tags.take(5),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val driverSnap = db.collection("drivers").document(driverId).get().await()
            val curSum = driverSnap.getLong("ratingSum") ?: 0L
            val curCount = driverSnap.getLong("ratingCount") ?: 0L
            val newSum = curSum + stars.toLong()
            val newCount = curCount + 1L
            val newAvg = ((newSum.toDouble() / newCount.toDouble()) * 10.0).roundToInt() / 10.0

            db.collection("drivers").document(driverId).update(
                mapOf(
                    "ratingSum" to newSum,
                    "ratingCount" to newCount,
                    "ratingAvg" to newAvg,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, tripPath)
            Result.failure(e)
        }
    }

    suspend fun sendTripMessage(
        trip: Trip,
        senderName: String,
        senderRole: String,
        text: String
    ): Result<Unit> {
        val uid = requireUserId()
        val ref = db.collection("trips").document(trip.id).collection("messages").document()
        return try {
            val participants = listOfNotNull(trip.customerId, trip.driverId).distinct()
            ref.set(
                mapOf(
                    "tripId" to trip.id,
                    "senderId" to uid,
                    "senderName" to senderName.trim().take(80),
                    "senderRole" to senderRole,
                    "text" to text.trim().take(300),
                    "participantUids" to participants,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun requestWalletTransaction(
        driver: DriverProfile,
        type: String,
        amountEgp: Double,
        method: String,
        accountOrRef: String
    ): Result<Unit> {
        val uid = requireUserId()
        val ref = db.collection("wallet_transactions").document()
        return try {
            require(amountEgp > 0) { "المبلغ غير صحيح" }
            require(accountOrRef.trim().length >= 3) { "رقم المرجع أو الحساب مطلوب" }
            if (type == "withdrawal") {
                require(amountEgp <= driver.withdrawableBalance) { "المبلغ المطلوب أكبر من الأرباح المتاحة للسحب" }
            }
            ref.set(
                mapOf(
                    "driverId" to uid,
                    "driverName" to driver.name,
                    "type" to type,
                    "amountEgp" to amountEgp,
                    "method" to method,
                    "accountOrRef" to accountOrRef.trim().take(100),
                    "status" to "pending",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    // --- ADMIN ACTIONS ---

    suspend fun adminUpdateDriverStatus(
        driverId: String,
        status: String,
        rejectionReason: String? = null
    ): Result<Unit> {
        val path = "drivers/$driverId"
        return try {
            requireUserId()
            val updates = mutableMapOf<String, Any>(
                "status" to status,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (status != "approved") updates["online"] = false
            if (!rejectionReason.isNullOrBlank()) updates["rejectionReason"] = rejectionReason.trim().take(250)
            db.document(path).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun adminUpdateCompanyStatus(
        companyId: String,
        status: String,
        rejectionReason: String? = null
    ): Result<Unit> {
        val path = "companies/$companyId"
        return try {
            requireUserId()
            val updates = mutableMapOf<String, Any>(
                "status" to status,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!rejectionReason.isNullOrBlank()) updates["rejectionReason"] = rejectionReason.trim().take(250)
            db.document(path).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

        suspend fun adminProcessWalletTransaction(
        tx: WalletTransaction,
        approve: Boolean,
        adminNote: String? = null
    ): Result<Unit> {
        val txPath = "wallet_transactions/${tx.id}"
        return try {
            requireUserId()
            val newStatus = if (approve) "approved" else "rejected"
            val txUpdates = mutableMapOf<String, Any>(
                "status" to newStatus,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!adminNote.isNullOrBlank()) txUpdates["adminNote"] = adminNote.trim().take(200)
            db.document(txPath).update(txUpdates).await()

            if (approve) {
                val allDrivers = db.collection("drivers").get().await()
                val matchingDocs = allDrivers.documents.filter { doc ->
                    doc.id == tx.driverId ||
                        (tx.driverName.isNotBlank() && doc.getString("name") == tx.driverName)
                }
                for (doc in matchingDocs) {
                    if (tx.type == "topup") {
                        val curWallet = doc.getDouble("walletBalance") ?: 0.0
                        doc.reference.update(
                            mapOf(
                                "walletBalance" to curWallet + tx.amountEgp,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        ).await()
                    } else if (tx.type == "withdrawal") {
                        val curWithdrawable = doc.getDouble("withdrawableBalance") ?: 0.0
                        doc.reference.update(
                            mapOf(
                                "withdrawableBalance" to (curWithdrawable - tx.amountEgp).coerceAtLeast(0.0),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        ).await()
                    }
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, txPath)
            Result.failure(e)
        }
    }
               suspend fun adminUpdatePricingConfig(config: AppPricingConfig): Result<Unit> {
        val path = "app_config/pricing"
        return try {
            requireUserId()
            db.document(path).set(
                mapOf(
                    "dieselPriceEgp" to config.dieselPriceEgp,
                    "baseFareEgp" to config.baseFareEgp,
                    "perKmEgp" to config.perKmEgp,
                    "commissionPercent" to config.commissionPercent,
                    "minWalletEgp" to config.minWalletEgp,
                    "cancelFeeEgp" to config.cancelFeeEgp,
                    "minWithdrawEgp" to config.minWithdrawEgp,
                    "gatewayLive" to config.gatewayLive,
                    "platformAccountInfo" to config.platformAccountInfo.trim().take(150),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }
