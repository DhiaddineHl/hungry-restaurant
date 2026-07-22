package woyou.aidlservice.jiuiv5;

/**
 * Sunmi inner-printer async result callback.
 * Part of the SUNMI Inner Printer AIDL SDK.
 */
interface ICallback {
    /** Called when a queued command finishes (true = success). */
    void onRunResult(boolean isSuccess);

    /** Returns a string result for query-style commands. */
    void onReturnString(String result);

    /** Called when the command raised an exception. */
    void onRaiseException(int code, String msg);

    /** Fine-grained print result (code 0 = OK). Available on newer firmware. */
    void onPrintResult(int code, String msg);
}
