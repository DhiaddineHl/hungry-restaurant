package com.hungry.restaurant.pos.printer

import android.content.Context
import android.util.Log
import com.hungry.restaurant.pos.data.model.Order
import com.sunmi.peripheral.printer.InnerPrinterCallback
import com.sunmi.peripheral.printer.InnerPrinterManager
import com.sunmi.peripheral.printer.SunmiPrinterService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Binds to the SUNMI built-in printer through the official `printerlibrary`
 * client (`InnerPrinterManager` / `SunmiPrinterService`) and exposes a small,
 * coroutine friendly surface for printing receipts.
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
    private var service: SunmiPrinterService? = null

    private val callback = object : InnerPrinterCallback() {
        override fun onConnected(service: SunmiPrinterService) {
            this@SunmiPrinter.service = service
            _status.value = Status.CONNECTED
            Log.i(TAG, "Sunmi printer service connected")
        }

        override fun onDisconnected() {
            service = null
            _status.value = Status.IDLE
            Log.w(TAG, "Sunmi printer service disconnected")
        }
    }

    /** Bind to the printer service. Safe to call multiple times. */
    fun connect() {
        if (_status.value == Status.CONNECTED || _status.value == Status.CONNECTING) return
        _status.value = Status.CONNECTING
        val bound = try {
            InnerPrinterManager.getInstance().bindService(appContext, callback)
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
            runCatching { InnerPrinterManager.getInstance().unBindService(appContext, callback) }
        }
        service = null
        _status.value = Status.IDLE
    }

    val isConnected: Boolean get() = service != null

    /**
     * Print a formatted kitchen/customer receipt for [order]. [readyInMinutes],
     * when given, prints as a "Ready in: X min" footer line - the backend has
     * no per-order prep-time field, so this is whatever the accepting staff
     * member chose on the incoming-order screen (or the restaurant's own
     * default), not something read back from the order itself.
     * Runs off the main thread; returns a [Result] describing success/failure.
     */
    suspend fun printReceipt(order: Order, readyInMinutes: Int? = null): Result<Unit> = withContext(Dispatchers.IO) {
        val svc = service
            ?: return@withContext Result.failure(
                IllegalStateException("Printer not connected. Tap the printer icon to reconnect."),
            )
        runCatching {
            svc.enterPrinterBuffer(true)
            ReceiptFormatter(svc).print(order, readyInMinutes)
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
    }
}
