package re.rickmoo.gecko.infra

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileNotFoundException

class GlideContentProvider : ContentProvider() {
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        // 1. 从 uri 参数中解析出原始的网络 URL
        val originalUrl = uri.getQueryParameter("url")
            ?: throw FileNotFoundException("No URL provided")

        try {
            // ContentProvider 的 openFile 运行在 Binder 线程池中，
            // 通常可以执行耗时操作，但为了保险最好确认 Glide 配置允许在此线程运行
            val file: File = Glide.with(context!!)
                .asFile()
                .load(originalUrl)
                .submit() // 同步阻塞获取
                .get()

            // 3. 返回文件的只读描述符给 GeckoView
            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)

        } catch (e: Exception) {
            e.printStackTrace()
            // 下载失败，这里无法简单回退到原网络请求（因为已经重定向了）
            // 建议返回一个默认的错误图，或者抛出异常让网页显示破图
            throw FileNotFoundException("Glide failed to download: ${e.message}")
        }
    }

    // 其他方法只需默认实现或返回 null
    override fun onCreate(): Boolean = true
    override fun query(u: Uri, p: Array<String>?, s: String?, sA: Array<String>?, sort: String?): Cursor? = null
    override fun getType(uri: Uri): String {
        return uri.path?.let {
            when (it.split('.').last()) {
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "gif" -> "image/gif"
                "webp" -> "image/webp"
                else -> "image/jpeg"
            }
        } ?: "image/png"
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0

}