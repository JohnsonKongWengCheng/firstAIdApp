package com.example.firstaid.model.admin

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdminRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun checkAdminStatus(userId: String?, docId: String?): Boolean {
        return try {
            if (userId != null) {
                // Primary: check Admin collection
                val adminQuery = db.collection("Admin")
                    .whereEqualTo("userId", userId)
                    .limit(1)
                    .get()
                    .await()

                if (!adminQuery.isEmpty) {
                    return true
                }
            }

            // Fallback to User flags
            if (docId != null) {
                val userDoc = db.collection("User").document(docId).get().await()
                val role = userDoc.getString("role")
                val flag = userDoc.getBoolean("isAdmin") ?: false
                return role == "admin" || flag
            }

            false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun checkTopicsWithoutBadges(): Boolean {
        return try {
            val firstAidResult = db.collection("First_Aid").get().await()
            val badgeResult = db.collection("Badge").get().await()

            val topicsWithBadges = badgeResult.documents.mapNotNull { badgeDoc ->
                badgeDoc.getString("firstAidId")
            }.toSet()

            firstAidResult.documents.any { doc ->
                val firstAidId = doc.getString("firstAidId") ?: doc.id
                !topicsWithBadges.contains(firstAidId)
            }
        } catch (e: Exception) {
            true // Fallback: allow navigation
        }
    }

    suspend fun checkTopicsWithoutExams(): Boolean {
        return try {
            val firstAidResult = db.collection("First_Aid").get().await()
            val examResult = db.collection("Exam").get().await()

            val topicsWithExams = examResult.documents.mapNotNull { examDoc ->
                examDoc.getString("firstAidId")
            }.toSet()

            firstAidResult.documents.any { doc ->
                val firstAidId = doc.getString("firstAidId") ?: doc.id
                !topicsWithExams.contains(firstAidId)
            }
        } catch (e: Exception) {
            true // Fallback: allow navigation
        }
    }

    suspend fun checkTopicsWithExams(): Boolean {
        return try {
            val firstAidResult = db.collection("First_Aid").get().await()
            val examResult = db.collection("Exam").get().await()

            val topicsWithExams = examResult.documents.mapNotNull { examDoc ->
                examDoc.getString("firstAidId")
            }.toSet()

            firstAidResult.documents.any { doc ->
                val firstAidId = doc.getString("firstAidId") ?: doc.id
                topicsWithExams.contains(firstAidId)
            }
        } catch (e: Exception) {
            true // Fallback: allow navigation
        }
    }

    suspend fun checkTopicsWithoutModules(): Boolean {
        return try {
            val firstAidResult = db.collection("First_Aid").get().await()
            val moduleResult = db.collection("Learning").get().await()

            val topicsWithModules = moduleResult.documents.mapNotNull { moduleDoc ->
                moduleDoc.getString("firstAidId")
            }.toSet()

            firstAidResult.documents.any { doc ->
                val firstAidId = doc.getString("firstAidId") ?: doc.id
                !topicsWithModules.contains(firstAidId)
            }
        } catch (e: Exception) {
            true // Fallback: allow navigation
        }
    }

    suspend fun checkTopicsWithModules(): Boolean {
        return try {
            val firstAidResult = db.collection("First_Aid").get().await()
            val moduleResult = db.collection("Learning").get().await()

            val topicsWithModules = moduleResult.documents.mapNotNull { moduleDoc ->
                moduleDoc.getString("firstAidId")
            }.toSet()

            firstAidResult.documents.any { doc ->
                val firstAidId = doc.getString("firstAidId") ?: doc.id
                topicsWithModules.contains(firstAidId)
            }
        } catch (e: Exception) {
            true // Fallback: allow navigation
        }
    }
}