package dk.babyapp.data.backup

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import dk.babyapp.data.AppDatabase
import java.io.File
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONArray
import org.json.JSONObject
import dk.babyapp.data.preferences.DataStoreAppPreferencesRepository
import dk.babyapp.data.medicine.MedicinePlan
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class EncryptedBackupService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: AppDatabase,
) {
    suspend fun create(password: CharArray): ByteArray {
        require(password.size >= 8) { "Adgangskoden skal være mindst 8 tegn" }
        val root = JSONObject().put("format", 1).put("createdAt", System.currentTimeMillis())
        val tables = JSONObject()
        val db = database.openHelper.writableDatabase
        TABLES.forEach { table ->
            val rows = JSONArray()
            db.query("SELECT * FROM $table").use { cursor -> while (cursor.moveToNext()) rows.put(cursor.toJson()) }
            tables.put(table, rows)
        }
        root.put("tables", tables)
        val photos = JSONObject()
        File(context.filesDir, "profile_photos").listFiles()?.forEach { file -> photos.put(file.name, Base64.getEncoder().encodeToString(file.readBytes())) }
        root.put("photos", photos)
        root.put("medicines", Json.encodeToString(DataStoreAppPreferencesRepository(context).preferences.first().medicines))
        return encrypt(root.toString().toByteArray(), password)
    }

    suspend fun restore(bytes: ByteArray, password: CharArray) {
        val root = JSONObject(String(decrypt(bytes, password)))
        require(root.getInt("format") == 1) { "Backupformatet understøttes ikke" }
        val medicines = Json.decodeFromString<List<MedicinePlan>>(root.optString("medicines", "[]"))
        val tables = root.getJSONObject("tables")
        TABLES.forEach { require(tables.has(it)) { "Backupfilen mangler $it" } }
        val db = database.openHelper.writableDatabase
        db.beginTransaction()
        try {
            TABLES.asReversed().forEach { db.execSQL("DELETE FROM $it") }
            TABLES.forEach { table ->
                val rows = tables.getJSONArray(table)
                repeat(rows.length()) { index -> db.insert(table, SQLiteDatabase.CONFLICT_REPLACE, rows.getJSONObject(index).toContentValues()) }
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
        val photoDir = File(context.filesDir, "profile_photos").apply { mkdirs() }
        photoDir.listFiles()?.forEach(File::delete)
        val photos = root.optJSONObject("photos") ?: JSONObject()
        photos.keys().forEach { name -> File(photoDir, File(name).name).writeBytes(Base64.getDecoder().decode(photos.getString(name))) }
        DataStoreAppPreferencesRepository(context).updateMedicines(medicines)
        database.invalidationTracker.refreshAsync()
    }

    private companion object {
        val TABLES = listOf("child_profiles", "parent_profiles", "child_parent_links", "care_providers", "care_events")
        val MAGIC = byteArrayOf(0x42, 0x41, 0x42, 0x59, 0x42, 0x4B, 0x31)
    }
}

internal fun encrypt(plain: ByteArray, password: CharArray): ByteArray {
    val salt = ByteArray(16).also(SecureRandom()::nextBytes); val iv = ByteArray(12).also(SecureRandom()::nextBytes)
    val key = SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(PBEKeySpec(password, salt, 120_000, 256)).encoded, "AES")
    val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv)) }
    return ByteBuffer.allocate(7 + salt.size + iv.size + plain.size + 16).put(byteArrayOf(0x42, 0x41, 0x42, 0x59, 0x42, 0x4B, 0x31)).put(salt).put(iv).put(cipher.doFinal(plain)).array()
}

internal fun decrypt(bytes: ByteArray, password: CharArray): ByteArray {
    require(bytes.size > 51 && bytes.take(7).toByteArray().contentEquals(byteArrayOf(0x42, 0x41, 0x42, 0x59, 0x42, 0x4B, 0x31))) { "Ugyldig backupfil" }
    val buffer = ByteBuffer.wrap(bytes).apply { position(7) }; val salt = ByteArray(16).also(buffer::get); val iv = ByteArray(12).also(buffer::get); val encrypted = ByteArray(buffer.remaining()).also(buffer::get)
    val key = SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(PBEKeySpec(password, salt, 120_000, 256)).encoded, "AES")
    return Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv)) }.doFinal(encrypted)
}

private fun Cursor.toJson() = JSONObject().also { row ->
    columnNames.forEachIndexed { index, name -> row.put(name, when (getType(index)) { Cursor.FIELD_TYPE_NULL -> JSONObject.NULL; Cursor.FIELD_TYPE_INTEGER -> getLong(index); Cursor.FIELD_TYPE_FLOAT -> getDouble(index); Cursor.FIELD_TYPE_BLOB -> Base64.getEncoder().encodeToString(getBlob(index)); else -> getString(index) }) }
}

private fun JSONObject.toContentValues() = ContentValues().also { values ->
    keys().forEach { key -> when (val value = get(key)) { JSONObject.NULL -> values.putNull(key); is Long -> values.put(key, value); is Int -> values.put(key, value); is Double -> values.put(key, value); else -> values.put(key, value.toString()) } }
}
