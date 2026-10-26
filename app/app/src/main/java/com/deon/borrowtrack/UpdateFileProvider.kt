package com.deon.borrowtrack

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileNotFoundException

/**
 * Minimal read-only provider that serves files from the app cache "update/"
 * directory (used only for installing downloaded update APKs).
 * Framework-only — no androidx dependency.
 */
class UpdateFileProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val name = uri.lastPathSegment ?: throw FileNotFoundException("No file")
        val dir = File(context?.cacheDir, "update")
        val file = File(dir, name)
        if (!file.canonicalPath.startsWith(dir.canonicalPath)) {
            throw FileNotFoundException("Forbidden")
        }
        if (!file.exists()) throw FileNotFoundException(name)
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun getType(uri: Uri): String? = "application/vnd.android.package-archive"
    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0
    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0
}
