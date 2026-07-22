package com.hungry.restaurant.pos.printer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.hungry.restaurant.pos.data.model.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import woyou.aidlservice.jiuiv5.IWoyouService

/**
 * Binds to the SUNMI inner-printer AIDL service and exposes a small, coroutine
 * friendly surface for printing receipts.
 *
 * On non-Sunmi hardware (or an emulator) the service simply won't bind and
 * [status] settles on [Status.UNAVAILABLE]; callers get a clear failure instead
 * of a crash, so the rest of the app runs fine for development.
 */
class SunmiPrinter(private val appContext: Context) {

    enum class Status { IDLE, CONNECTING, CONNECTED, UNAVAILABLE }

    private val _status = MutableStateFlow(Status.IDLE)
    val status: StateFlow<Status> = _status.asStateFlow()

    @Volatile
    private var service: IWoyouService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = IWoyouService.Stub.asInterface(binder)
            _status.value = Status.CONNECTED
            Log.i(TAG, "Sunmi printer service connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            _status.value = Status.IDLE
            Log.w(TAG, "Sunmi printer service disconnected")
        }
    }

    /** Bind to the printer service. Safe to call multiple times. */
    fun connect() {
        if (_status.value == Status.CONNECTED || _status.value == Status.CONNECTING) return
        _status.value = Status.CONNECTING
        val intent = Intent().apply {
            setPackage(SERVICE_PACKAGE)
            action = SERVICE_ACTION
        }
        val bound = try {
            appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        } catch (t: Throwable) {
            Log.e(TAG, "bindService threw", t)
            false
        }
        if (!bound) {
            _status.value = Status.UNAVAILABLE
            Log.w(TAG, "Sunmi printer service not available on this device")
        }
    }

    fun disconnect() {
        if (service != null || _status.value == Status.CONNECTING) {
            runCatching { appContext.unbindService(connection) }
        }
        service = null
        _status.value = Status.IDLE
    }

    val isConnected: Boolean get() = service != null

    /**
     * Print a formatted kitchen/customer receipt for [order].
     * Runs off the main thread; returns a [Result] describing success/failure.
     */
    suspend fun printReceipt(order: Order): Result<Unit> = withContext(Dispatchers.IO) {
        val svc = service
            ?: return@withContext Result.failure(
                IllegalStateException("Printer not connected. Tap the printer icon to reconnect."),
            )
        runCatching {
            svc.enterPrinterBuffer(true)
            ReceiptFormatter(svc).print(order)
            svc.lineWrap(3, null)
            runCatching { svc.cutPaper(null) } // devices without a cutter simply ignore this
            svc.exitPrinterBuffer(true)
        }.onFailure { Log.e(TAG, "printReceipt failed", it) }
    }

    /** Feed a quick self-test to confirm the head is alive. */
    suspend fun printTest(): Result<Unit> = withContext(Dispatchers.IO) {
        val svc = service ?: return@withContext Result.failure(IllegalStateException("Printer not connected."))
        runCatching { svc.printerSelfChecking(null) }
    }

    companion object {
        private const val TAG = "SunmiPrinter"
        private const val SERVICE_PACKAGE = "woyou.aidlservice.jiuiv5"
        private const val SERVICE_ACTION = "woyou.aidlservice.jiuiv5.IWoyouService"
    }
}
