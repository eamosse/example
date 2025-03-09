package eu.chronolink.easytech.data.result

sealed class DataResult<out T : Any> {
    data object Idle : DataResult<Nothing>()
    data class Working(val message: String? = "") : DataResult<Nothing>()
    data class Success<out T : Any>(val data: T) : DataResult<T>()
    data class Error(
        val exception: Throwable,
        val code: Int,
        val message: String
    ) : DataResult<Nothing>()
}