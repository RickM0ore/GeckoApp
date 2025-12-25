package re.rickmoo.gecko.infra

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.core.content.ContextCompat
import androidx.startup.AppInitializer
import org.json.JSONObject
import org.mozilla.gecko.util.ThreadUtils.runOnUiThread
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSession.PermissionDelegate
import org.mozilla.geckoview.GeckoSession.PermissionDelegate.MediaCallback
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.WebExtension
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.full.memberFunctions


class GeckoConfigurer(
    private val context: Context,
    private val activityBridge: ActivityBridge,
    geckoView: GeckoView,
    private val session: GeckoSession,
    private val configure: GeckoConfigurer.(session: GeckoSession) -> Unit,
) {
    private val geckoRuntime =
        AppInitializer.getInstance(context).initializeComponent(GeckoRuntimeInitializer::class.java)

    init {
        session.open(geckoRuntime)
        geckoView.setSession(session)
        session.settings.allowJavascript = true
    }

    private val dependency by lazy { ConcurrentHashMap<String, Any>() }

    fun addExtensionDependency(name: String, dependency: Any) {
        this.dependency[name] = dependency
    }

    fun addExtensionDependency(dependency: Any) {
        this.dependency[dependency::class.simpleName!!.replaceFirstChar { it.lowercase() }] = dependency
    }

    fun load(uri: String) {
        configure(session)
        session.loadUri(uri)
    }


    private lateinit var permissionCallbackInternal: (Boolean) -> Unit
    private lateinit var multiplePermissionCallbackInternal: (Map<String, Boolean>) -> Unit
    private lateinit var contentCallbackInternal: (Uri?) -> Unit
    private lateinit var multipleContentCallbackInternal: (List<Uri>) -> Unit
    private lateinit var activityResultCallbackInternal: (ActivityResult) -> Unit

    fun permissionCallback(granted: Boolean) {
        permissionCallbackInternal(granted)
    }

    fun multiplePermissionCallback(granted: Map<String, Boolean>) {
        multiplePermissionCallbackInternal(granted)
    }

    fun contentCallback(content: Uri?) {
        contentCallbackInternal(content)
    }

    fun multipleContentCallback(content: List<Uri>) {
        multipleContentCallbackInternal(content)
    }

    fun activityResultCallback(result: ActivityResult) {
        activityResultCallbackInternal(result)
    }


    fun addMediaPermissionRequest() {
        session.setPermissionDelegate(
            object : PermissionDelegate {
                override fun onMediaPermissionRequest(
                    session: GeckoSession,
                    uri: String,
                    video: Array<out PermissionDelegate.MediaSource?>?,
                    audio: Array<out PermissionDelegate.MediaSource?>?,
                    callback: MediaCallback
                ) {
                    val audioPermission = audio?.isNotEmpty() == true && ContextCompat.checkSelfPermission(
                        context, Manifest.permission.RECORD_AUDIO
                    ) != PERMISSION_GRANTED
                    val videoPermission = video?.isNotEmpty() == true
                        && ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PERMISSION_GRANTED

                    if (audioPermission && !videoPermission) {
                        permissionCallbackInternal = {
                            callback.grant(video?.firstOrNull(), if (it) audio.firstOrNull() else null)
                        }
                        activityBridge.requestPermission(Manifest.permission.RECORD_AUDIO)
                    } else if (!audioPermission && videoPermission) {
                        permissionCallbackInternal = {
                            callback.grant(if (it) video.firstOrNull() else null, audio?.firstOrNull())
                        }
                        activityBridge.requestPermission(Manifest.permission.CAMERA)
                    } else if (audioPermission) {
                        multiplePermissionCallbackInternal = {
                            callback.grant(
                                if (it[Manifest.permission.CAMERA] == true) video?.firstOrNull() else null,
                                if (it[Manifest.permission.RECORD_AUDIO] == true) audio.firstOrNull() else null,
                            )
                        }
                        activityBridge.requestPermission(
                            arrayOf(
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.CAMERA
                            )
                        )
                    } else {
                        callback.grant(video?.firstOrNull(), audio?.firstOrNull())
                    }
                }

                override fun onAndroidPermissionsRequest(
                    session: GeckoSession,
                    permissions: Array<out String?>?,
                    callback: PermissionDelegate.Callback
                ) {
                    val needGrant = permissions?.filter {
                        if (it == null) false
                        else if (ContextCompat.checkSelfPermission(context, it) != PERMISSION_GRANTED) {
                            true
                        } else
                            false
                    }?.filterNotNull()
                    if (needGrant.isNullOrEmpty()) callback.grant()
                    else {
                        activityBridge.requestPermission(needGrant.toTypedArray())
                        multiplePermissionCallbackInternal = {
                            callback.grant()
                        }
                    }
                }
            })

    }

    fun addPromptDelegate() {
        session.promptDelegate = object : GeckoSession.PromptDelegate {
            override fun onFilePrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.FilePrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse> {
                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
                when (prompt.type) {
                    GeckoSession.PromptDelegate.FilePrompt.Type.SINGLE -> {
                        contentCallbackInternal = {
                            if (it != null)
                                result.complete(prompt.confirm(context, it))
                            else
                                result.complete(prompt.dismiss())
                        }
                        activityBridge.requestContent(prompt.mimeTypes?.filterNotNull()?.toTypedArray() ?: emptyArray())
                    }

                    GeckoSession.PromptDelegate.FilePrompt.Type.MULTIPLE -> {
                        multipleContentCallbackInternal = {
                            if (it.isNotEmpty())
                                result.complete(prompt.confirm(context, it.toTypedArray()))
                            else
                                result.complete(prompt.dismiss())
                        }
                        activityBridge.requestMultipleContent(
                            prompt.mimeTypes?.filterNotNull()?.toTypedArray() ?: emptyArray()
                        )
                    }

                    GeckoSession.PromptDelegate.FilePrompt.Type.FOLDER -> {
                        activityResultCallbackInternal = {
                            if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                                val uri: Uri? = it.data?.data
                                if (uri != null) {
                                    result.complete(prompt.confirm(context, uri))
                                } else {
                                    result.complete(prompt.dismiss())
                                }
                            }
                        }
                    }
                }
                return result
            }
        }
    }

    fun addContentDelegate() {
        session.contentDelegate = object : GeckoSession.ContentDelegate {

        }
    }

    fun addProgressDelegate(onComplete: (success: Boolean) -> Unit) {
        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStop(session: GeckoSession, success: Boolean) {
                onComplete(success)
            }
        }
    }

    fun registerWebExtension() {
        geckoRuntime.webExtensionController.ensureBuiltIn(
            "resource://android/assets/messaging/", "me@rickmoo.re"
        ).accept({ extension ->
            dependency.forEach { (name, _) ->
                runOnUiThread {
                    extension?.setMessageDelegate(object : WebExtension.MessageDelegate {
                        // stream message
                        override fun onConnect(port: WebExtension.Port) {
                            Log.i(null, "onConnect")
                            port.setDelegate(object : WebExtension.PortDelegate {
                                override fun onPortMessage(
                                    message: Any,
                                    port: WebExtension.Port
                                ) {
                                    Log.i("onPortMessage", message.toString())
                                    port.postMessage(JSONObject(message.toString()))
                                    port.disconnect()
                                }
                            })
                        }

                        // One time message
                        override fun onMessage(
                            nativeApp: String,
                            message: Any, // JSONObject | primitive type
                            sender: WebExtension.MessageSender
                        ): GeckoResult<in Any> {
                            val result = GeckoResult<Any>()
                            val app = dependency[nativeApp]
                            if (app == null) {
                                result.completeExceptionally(RuntimeException("Cannot dispatch native call"))
                                return result
                            }
                            when (message) {
                                is JSONObject -> {
                                    val action = message.getString("action")
                                    val data = message.get("data")
                                    val function = app::class.memberFunctions.find { it.name == action }
                                    if (function == null) {
                                        result.completeExceptionally(RuntimeException("native function not found: $action"))
                                        return result
                                    }
                                    try {
                                        result.complete(function.callNative(app, data))
                                    } catch (e: Exception) {
                                        Log.e("GeckoExtension", "call native failed", e)
                                        result.completeExceptionally(RuntimeException("Cannot dispatch native call"))
                                    }
                                }

                                else -> {
                                    throw RuntimeException("Cannot dispatch native call")
                                }
                            }
                            return result
                        }
                    }, name)
                }
            }
        }, {
            Log.e("geckoView", "install webExtension failed", it)
        })
    }
}