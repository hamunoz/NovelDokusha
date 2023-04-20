package my.noveldokusha.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.provider.OpenableColumns
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import my.noveldokusha.R
import my.noveldokusha.createEpubBook
import my.noveldokusha.importEpubToRepository
import my.noveldokusha.repository.Repository
import my.noveldokusha.utils.Extra_Uri
import my.noveldokusha.utils.NotificationsCenter
import my.noveldokusha.utils.asSequence
import my.noveldokusha.utils.isServiceRunning
import my.noveldokusha.utils.removeProgressBar
import my.noveldokusha.utils.text
import my.noveldokusha.utils.title
import my.noveldokusha.utils.tryAsResponse
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class EpubImportService : Service() {
    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var notificationsCenter: NotificationsCenter

    private class IntentData : Intent {
        var uri by Extra_Uri()

        constructor(intent: Intent) : super(intent)
        constructor(ctx: Context, uri: Uri) : super(ctx, EpubImportService::class.java) {
            this.uri = uri
        }
    }

    companion object {
        fun start(ctx: Context, uri: Uri) {
            if (!isRunning(ctx))
                ContextCompat.startForegroundService(ctx, IntentData(ctx, uri))
        }

        private fun isRunning(context: Context): Boolean =
            context.isServiceRunning(EpubImportService::class.java)
    }

    private val channel_id = "Import EPUB"
    private val channel_id_error = "epub import error"
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private var job: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationBuilder = notificationsCenter.showNotification(channel_id)
        startForeground(channel_id.hashCode(), notificationBuilder.build())
    }

    override fun onDestroy() {
        job?.cancel()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY
        val intentData = IntentData(intent)
        job = CoroutineScope(Dispatchers.IO).launch {
            tryAsResponse {
                notificationsCenter.modifyNotification(notificationBuilder, channel_id) {
                    title = getString(R.string.import_epub)
                    foregroundServiceBehavior = NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
                    setProgress(100, 0, true)
                }
                val inputStream = contentResolver.openInputStream(intentData.uri)
                if (inputStream == null) {
                    notificationsCenter.showNotification(channel_id_error) {
                        text = getString(R.string.failed_get_file)
                        removeProgressBar()
                    }
                    return@tryAsResponse
                }

                val fileName = contentResolver.query(
                    intentData.uri,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null,
                    null,
                    null,
                    null
                ).asSequence().map { it.getString(0) }.last()

                val epub = inputStream.use {
                    createEpubBook(fileName = fileName, inputStream = it)
                }

                notificationsCenter.modifyNotification(notificationBuilder, channel_id) {
                    text = getString(R.string.importing_epub)
                }
                importEpubToRepository(repository = repository, epub = epub, addToLibrary = true)

                notificationsCenter.modifyNotification(notificationBuilder, channel_id) {
                    text = getString(R.string.epub_added_to_library)
                    removeProgressBar()
                }
            }.onError {
                Timber.e(it.exception)
                notificationsCenter.showNotification(channel_id_error) {
                    text = getString(R.string.failed_to_import_epub)
                    setSubText(it.message)
                    removeProgressBar()
                }
            }

            stopSelf(startId)
        }
        return START_NOT_STICKY
    }
}