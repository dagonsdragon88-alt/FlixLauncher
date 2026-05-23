package com.movtery.zalithlauncher.utils.skin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import com.movtery.zalithlauncher.feature.log.Logging
import com.movtery.zalithlauncher.utils.path.PathManager
import net.kdt.pojavlaunch.value.MinecraftAccount
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Manages skin and cape assets for player profiles.
 * Handles uploading, parsing, and storing custom skins and capes.
 */
object SkinCapeManager {
    private const val SKIN_WIDTH = 64
    private const val SKIN_HEIGHT = 64
    private const val CAPE_WIDTH = 64
    private const val CAPE_HEIGHT = 32

    /**
     * Upload and process a skin image from a URI.
     * Parses the 2D skin matrix and extracts the face layout.
     */
    fun uploadSkin(context: Context, account: MinecraftAccount, uri: Uri): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) {
                Logging.e("SkinCapeManager", "Failed to decode skin bitmap from URI")
                return false
            }

            // Validate skin dimensions (must be 64x64 or multiples)
            if (bitmap.width < SKIN_WIDTH || bitmap.height < SKIN_HEIGHT) {
                Logging.e("SkinCapeManager", "Invalid skin dimensions: ${bitmap.width}x${bitmap.height}")
                return false
            }

            // Scale to 64x64 if needed
            val scaledBitmap = if (bitmap.width != SKIN_WIDTH || bitmap.height != SKIN_HEIGHT) {
                Bitmap.createScaledBitmap(bitmap, SKIN_WIDTH, SKIN_HEIGHT, true)
            } else {
                bitmap
            }

            // Save skin to file
            val skinFile = getSkinFile(account)
            skinFile.parentFile?.mkdirs()
            FileOutputStream(skinFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            Logging.i("SkinCapeManager", "Skin uploaded successfully for account: ${account.username}")
            true
        } catch (e: Exception) {
            Logging.e("SkinCapeManager", "Failed to upload skin", e)
            false
        }
    }

    /**
     * Upload and process a cape image from a URI.
     */
    fun uploadCape(context: Context, account: MinecraftAccount, uri: Uri): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) {
                Logging.e("SkinCapeManager", "Failed to decode cape bitmap from URI")
                return false
            }

            // Validate cape dimensions (must be 64x32 or multiples)
            if (bitmap.width < CAPE_WIDTH || bitmap.height < CAPE_HEIGHT) {
                Logging.e("SkinCapeManager", "Invalid cape dimensions: ${bitmap.width}x${bitmap.height}")
                return false
            }

            // Scale to 64x32 if needed
            val scaledBitmap = if (bitmap.width != CAPE_WIDTH || bitmap.height != CAPE_HEIGHT) {
                Bitmap.createScaledBitmap(bitmap, CAPE_WIDTH, CAPE_HEIGHT, true)
            } else {
                bitmap
            }

            // Save cape to file
            val capeFile = getCapeFile(account)
            capeFile.parentFile?.mkdirs()
            FileOutputStream(capeFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            Logging.i("SkinCapeManager", "Cape uploaded successfully for account: ${account.username}")
            true
        } catch (e: Exception) {
            Logging.e("SkinCapeManager", "Failed to upload cape", e)
            false
        }
    }

    /**
     * Get the face drawable from the skin bitmap.
     * Extracts the 8x8 face region from the skin texture.
     */
    fun getFaceFromSkin(context: Context, account: MinecraftAccount, size: Int): Drawable? {
        return try {
            val skinFile = getSkinFile(account)
            if (!skinFile.exists()) return null

            val bitmap = BitmapFactory.decodeFile(skinFile.absolutePath) ?: return null
            val faceBitmap = extractFace(bitmap)
            val scaledFace = Bitmap.createScaledBitmap(faceBitmap, size, size, true)
            BitmapDrawable(context.resources, scaledFace)
        } catch (e: Exception) {
            Logging.e("SkinCapeManager", "Failed to get face from skin", e)
            null
        }
    }

    /**
     * Extract the face region from a skin bitmap.
     * Face is located at (8, 8) with size 8x8 in the 64x64 skin texture.
     */
    private fun extractFace(skin: Bitmap): Bitmap {
        val scaleFactor = skin.width / 64.0f
        val faceSize = (8 * scaleFactor).toInt()
        val faceX = (8 * scaleFactor).toInt()
        val faceY = (8 * scaleFactor).toInt()

        return Bitmap.createBitmap(skin, faceX, faceY, faceSize, faceSize)
    }

    /**
     * Get the cape drawable for display.
     */
    fun getCapeDrawable(context: Context, account: MinecraftAccount): Drawable? {
        return try {
            val capeFile = getCapeFile(account)
            if (!capeFile.exists()) return null

            val bitmap = BitmapFactory.decodeFile(capeFile.absolutePath) ?: return null
            BitmapDrawable(context.resources, bitmap)
        } catch (e: Exception) {
            Logging.e("SkinCapeManager", "Failed to get cape drawable", e)
            null
        }
    }

    /**
     * Check if account has a custom skin.
     */
    fun hasCustomSkin(account: MinecraftAccount): Boolean {
        return getSkinFile(account).exists()
    }

    /**
     * Check if account has a custom cape.
     */
    fun hasCustomCape(account: MinecraftAccount): Boolean {
        return getCapeFile(account).exists()
    }

    /**
     * Get the skin file path for an account.
     */
    fun getSkinFile(account: MinecraftAccount): File {
        return File(PathManager.DIR_USER_SKIN, "${account.uniqueUUID}.png")
    }

    /**
     * Get the cape file path for an account.
     */
    fun getCapeFile(account: MinecraftAccount): File {
        return File(PathManager.DIR_USER_CAPE, "${account.uniqueUUID}.png")
    }

    /**
     * Delete custom skin for an account.
     */
    fun deleteSkin(account: MinecraftAccount): Boolean {
        return try {
            val file = getSkinFile(account)
            if (file.exists()) file.delete() else true
        } catch (e: Exception) {
            Logging.e("SkinCapeManager", "Failed to delete skin", e)
            false
        }
    }

    /**
     * Delete custom cape for an account.
     */
    fun deleteCape(account: MinecraftAccount): Boolean {
        return try {
            val file = getCapeFile(account)
            if (file.exists()) file.delete() else true
        } catch (e: Exception) {
            Logging.e("SkinCapeManager", "Failed to delete cape", e)
            false
        }
    }
}
