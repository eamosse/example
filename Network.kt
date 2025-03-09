package eu.chronolink.easytech.data.helpers

import eu.chronolink.easytech.data.result.DataResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.io.IOException


internal fun <T : Any, K : Any> Response<T>.parse(parser: (T) -> K): DataResult<K> {
    return if (isSuccessful) {
        body()?.let {
            DataResult.Success(parser(it))
        } ?: run {
            Timber.tag("Error in response").e(NoDataException(), "Aucune donnée")
            DataResult.Error(
                exception = NoDataException(),
                message = "Aucune donnée",
                code = 404
            )
        }
    } else {
        Timber.tag("Error in API Response").e(java.lang.Exception(), "%s-%s", message(), code())
        DataResult.Error(
            exception = Exception(),
            message = message(),
            code = code()
        )
    }
}

class NoDataException : Exception()
class NetworkException : Exception()
class UnAuthenticatedException : Exception()

internal suspend fun <T : Any> safeCall(execute: suspend () -> DataResult<T>): DataResult<T> {
    return try {
        withContext(Dispatchers.IO) {
            execute()
        }
    } catch (e: Exception) {
        Timber.tag("Error in API CALL").e(e)
        when (e) {
            is IOException -> {
                DataResult.Error(
                    exception = NetworkException(),
                    message = "Problème d'accès au réseau",
                    code = -1
                )
            }

            is HttpException -> {
                if (e.code() == 401) {
                    DataResult.Error(
                        exception = UnAuthenticatedException(),
                        message = "Problème d'authentification",
                        code = e.code()
                    )
                } else {
                    DataResult.Error(
                        exception = e,
                        message = e.message ?: "No message",
                        code = e.code()
                    )
                }
            }

            else -> {
                DataResult.Error(
                    exception = e,
                    message = e.message ?: "No message",
                    code = -1
                )
            }
        }
    }
}