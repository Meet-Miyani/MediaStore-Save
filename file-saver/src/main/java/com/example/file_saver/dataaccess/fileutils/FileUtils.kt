package com.example.file_saver.dataaccess.fileutils

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * FileUtils
 * Created by lishilin on 2020/07/14
 */
object FileUtils {
    /**
     * Get Authority (7.0Uri adaptation)
     *
     * @param context context
     * @return String Authority
     */
    fun getAuthority(context: Context): String {
        return context.packageName + ".FileProvider"
    }

    /**
     * Get FileUri
     *
     * @param context context
     * @param file file
     * @return Uri
     */
    fun getFileUri(context: Context, file: File?): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context, getAuthority(context),
                file!!
            )
        } else {
            Uri.fromFile(file)
        }
    }

    /**
     * Get ContentUri
     *
     * @param context context
     * @param file file (specific files can only be queried)
     * @return Uri
     */
    fun getContentUri(context: Context, file: File?): Uri? {
        if (file == null || !file.exists()) {
            return null
        }
        var uri: Uri? = null
        val path = file.absolutePath
        val mimeType = getMimeType(file.name)
        val contentResolver = context.contentResolver
        val contentUri = getContentUri(mimeType)
        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection = MediaStore.MediaColumns.DATA + "=?"
        val selectionArgs = arrayOf(path)
        val cursor = contentResolver.query(contentUri, projection, selection, selectionArgs, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val idCol = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
                if (idCol >= 0) {
                    uri = ContentUris.withAppendedId(contentUri, cursor.getLong(idCol))
                }
            }
            cursor.close()
        }
        return uri
    }

    /**
     * Get duplicate file Uri
     *
     * @param context context
     * @param file file
     * @return Uri
     */
    fun getDuplicateFileUri(context: Context, file: File?): Uri? {
        if (file == null || !file.exists()) {
            return null
        }
        var uri: Uri? = null
        val name = file.name
        val size = file.length()
        val mimeType = getMimeType(name)
        val contentResolver = context.contentResolver
        val contentUri = getContentUri(mimeType)
        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection =
            MediaStore.MediaColumns.DISPLAY_NAME + "=?" + " AND " + MediaStore.MediaColumns.SIZE + "=?"
        val selectionArgs = arrayOf(name, size.toString())
        val cursor = contentResolver.query(contentUri, projection, selection, selectionArgs, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val idCol = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
                if (idCol >= 0) {
                    uri = ContentUris.withAppendedId(contentUri, cursor.getLong(idCol))
                }
            }
            cursor.close()
        }
        return uri
    }

    /**
     * Get Path
     *
     *
     * Notice:
     * It is only used as Debug, and it is not recommended for actual development. After all,
     * it is meaningless to obtain the path. It is better to use Uri to operate
     *
     * @param context context
     * @param uri uri (specific Uri can only be queried, such as the Uri of the system media library)
     * @return String Path
     */
    fun getPath(context: Context, uri: Uri?): String? {
        if (uri == null) {
            return null
        }
        var path: String? = null
        val contentResolver = context.contentResolver
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val pathCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                if (pathCol >= 0) {
                    path = cursor.getString(pathCol)
                }
            }
            cursor.close()
        }
        return path
    }

    /**
     * Get Name
     *
     * @param context context
     * @param uri uri
     * @return String Name
     */
    fun getName(context: Context, uri: Uri?): String? {
        if (uri == null) {
            return null
        }
        val documentFile: DocumentFile? = DocumentFile.fromSingleUri(context, uri)
        return documentFile?.name

// String name = null;
// ContentResolver contentResolver = context.getContentResolver();
// String[] projection = new String[]{MediaStore.MediaColumns.DISPLAY_NAME};
// Cursor cursor = contentResolver. query(uri, projection, null, null, null);
// if (cursor != null) {
// if (cursor.moveToFirst()) {
// int nameCol = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
// if (nameCol >= 0) {
// name = cursor.getString(nameCol);
// }
// }
// cursor.close();
// }
// return name;
    }

    /**
     * get file extension
     *
     * @param name name
     * @return String Extension
     */
    fun getExtension(name: String): String {
        val index = name.lastIndexOf(".")
        return if (index > 0) {
            name.substring(index + 1)
        } else ""
    }

    /**
     * Get file MimeType
     *
     * @param name name
     * @return String MimeType
     */
    fun getMimeType(name: String): String? {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtension(name))
    }

    /**
     * Get file MimeType
     *
     * @param context context
     * @param uri uri
     * @return String MimeType
     */
    fun getMimeType(context: Context, uri: Uri?): String? {
        return if (uri == null) {
            null
        } else context.contentResolver.getType(uri)

// DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
// if (documentFile != null) {
// return documentFile.getType();
// }
// return null;
    }

    /**
     * Get Uri (according to mimeType)
     *
     * @param mimeType mimeType
     * @return Uri
     */
    fun getContentUri(mimeType: String?): Uri {
        val contentUri: Uri
        contentUri = if (mimeType!!.startsWith("image")) {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        } else if (mimeType.startsWith("video")) {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else if (mimeType.startsWith("audio")) {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Files.getContentUri("external")
        }
        return contentUri
    }

    /**
     * Get the folder name (according to mimeType)
     *
     * @param mimeType mimeType
     * @return String dirName
     */
    fun getDirName(mimeType: String?): String {
        return if (mimeType!!.startsWith("image")) {
            Environment.DIRECTORY_PICTURES
        } else if (mimeType.startsWith("video")) {
            Environment.DIRECTORY_PICTURES
        } else if (mimeType.startsWith("audio")) {
            Environment.DIRECTORY_MUSIC
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Environment.DIRECTORY_DOCUMENTS
            } else {
                Environment.DIRECTORY_DOWNLOADS
            }
        }
    }

    /**
     * Copy files to external
     *
     *
     * Access media files in shared storage: https://developer.android.com/training/data-storage/shared/media#add-item
     *
     * @param context context
     * @param dirName directory name (eg: "WeChat" in "/Pictures/WeChat")
     * @param file file
     * @return Uri
     */
    fun copyFileToExternal(context: Context, dirName: String, file: File?): Uri? {
        if (file == null || !file.exists()) {
            return null
        }

        // Get whether there are duplicate files to avoid repeated copying
        var uri = getDuplicateFileUri(context, file)
        if (uri != null) {
            return uri
        }
        val name = file.name
        val mimeType = getMimeType(name)
        val contentResolver = context.contentResolver
        val contentUri = getContentUri(mimeType)

        // insert parameters
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, name) // file name
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType) // mimeType
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var dirPath = getDirName(mimeType)
            if (!TextUtils.isEmpty(dirName)) {
                dirPath += File.separatorChar.toString() + dirName
            }
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, dirPath) // relative path
            values.put(
                MediaStore.MediaColumns.IS_PENDING,
                1
            ) // The processing status of the file (to prevent it from being queried by other apps during the writing process,
        // remember to modify it after the writing is complete)
        }

        // Get the inserted Uri
        uri = contentResolver.insert(contentUri, values)
        if (uri == null) {
            return null
        }

        // copy the file
        var copySuccess = false
        try {
            contentResolver.openOutputStream(uri).use { outputStream ->
                FileInputStream(file).use { inputStream ->
                    copySuccess = copy(
                        inputStream,
                        outputStream!!
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // delete if copy fails
        if (!copySuccess) {
            delete(context, uri)
            return null
        }

        // Update the processing status of the file
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(
                MediaStore.MediaColumns.IS_PENDING,
                0
            ) // The processing status of the file (to prevent it from being queried by other apps during the writing process,
            // remember to modify it after the writing is complete)
            contentResolver.update(uri, values, null, null)
        }
        return uri
    }

    /**
     * copy
     * [android.os.FileUtils.copy]
     *
     * @param inputStream inputStream
     * @param outputStream outputStream
     * @return boolean
     */
    fun copy(inputStream: InputStream, outputStream: OutputStream): Boolean {
        try {
            val buffer = ByteArray(8192)
            var byteRead: Int
            while (inputStream.read(buffer).also { byteRead = it } > 0) {
                outputStream.write(buffer, 0, byteRead)
            }
            outputStream.flush()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * delete
     *
     *
     * Notice:
     * Only applicable to the Uri returned by the system file selector, other Uri will report an error
     *
     *
     * Access documents and other files from shared storage: https://developer.android.com/training/data-storage/shared/documents-files#delete
     *
     * @param context context
     * @param uri uri
     * @return boolean
     */
    fun deleteSystem(context: Context, uri: Uri?): Boolean {
        var delete = false
        val contentResolver = context.contentResolver
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                delete = DocumentsContract.deleteDocument(contentResolver, uri!!)
            }else{
                uri?.let { delete = it.toFile().delete() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return delete
    }

    /**
     * delete
     *
     *
     * Notice:
     * Only applicable to media files created by the app itself;
     * Other types of files such as documents cannot be deleted, and files of other apps cannot be deleted successfully.
     * Only the Uri data in the media library can be deleted, and the actual files are not deleted.
     * To operate the data of other apps, the user needs to grant permission, catch RecoverableSecurityException exception,
     * and then request permission, see the official documentation for details.
     *
     *
     * Access media files in shared storage: https://developer.android.com/training/data-storage/shared/media#remove-item
     *
     * @param context context
     * @param uri uri
     * @return boolean
     */
    fun delete(context: Context, uri: Uri?): Boolean {
        var delete = false
        val contentResolver = context.contentResolver
        try {
            delete = contentResolver.delete(uri!!, null, null) > 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return delete
    }
}