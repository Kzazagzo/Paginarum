package put.paginarum.util

sealed class DataStatus<out T> {
    data object Loading : DataStatus<Nothing>()

    data class Success<T>(val data: T) : DataStatus<T>()

    data class Error(val msg: String?) : DataStatus<Nothing>()
}
