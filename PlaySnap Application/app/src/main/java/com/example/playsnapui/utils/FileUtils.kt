package com.example.playsnapui.utils

import android.content.Context
import android.net.Uri
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

fun getImageMultipart(context: Context, imageUri: Uri): MultipartBody.Part? {
    val file = File(getRealPathFromURI(context, imageUri)) // Convert Uri to File
    val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
    return MultipartBody.Part.createFormData("file", file.name, requestFile)
}

// Helper function to get file path from URI
fun getRealPathFromURI(context: Context, uri: Uri): String {
    val projection = arrayOf(android.provider.MediaStore.Images.Media.DATA)
    val cursor = context.contentResolver.query(uri, projection, null, null, null)
    cursor?.moveToFirst()
    val columnIndex = cursor?.getColumnIndex(projection[0])
    val path = cursor?.getString(columnIndex ?: 0) ?: ""
    cursor?.close()
    return path
}
