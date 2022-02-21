package com.trigonated.ctwtopheadlines.misc

/**
 * Generic class that represents an operation's status and result. Useful when combined with
 * [kotlinx.coroutines.flow.Flow].
 *
 * This class contains several helpful static functions to create results.
 * @param T The type of data of this operation's result.
 */
data class Result<T>(
    /** The status of the operation. */
    val status: Status,
    /** The data from the result. Typically non-null on successful operations and null otherwise. */
    val data: T?,
    /** The error that occurred. Typically only non-null on failed operations. */
    val error: Error?
) {

    companion object {
        /** A result from a successful operation. Usually has [data]. */
        fun <T> success(data: T?): Result<T> {
            return Result(Status.SUCCESS, data, null)
        }

        /** A result from a failed operation with an optional [error]. */
        fun <T> failure(error: Error?): Result<T> {
            return Result(Status.ERROR, null, error)
        }

        /** A result from an operation that is still loading. */
        fun <T> loading(data: T? = null): Result<T> {
            return Result(Status.LOADING, data, null)
        }

        /** A result from an operation that is refreshing. */
        fun <T> refreshing(data: T? = null): Result<T> {
            return Result(Status.REFRESHING, data, null)
        }

        /** A result from an operation that has [data] but is loading more data (e.g. pagination). */
        fun <T> loadingExtraData(data: T? = null): Result<T> {
            return Result(Status.LOADING_EXTRA_DATA, data, null)
        }

        /** A result from an operation that is waiting for an user action (e.g. authentication). */
        fun <T> awaitingUserAction(data: T? = null): Result<T> {
            return Result(Status.AWAITING_USER_ACTION, data, null)
        }
    }

    override fun toString(): String {
        return "Result(status=$status, data=$data, error=$error)"
    }

    enum class Status {
        SUCCESS,
        ERROR,
        LOADING,
        REFRESHING,
        LOADING_EXTRA_DATA,
        AWAITING_USER_ACTION;

        /** Whether the result is on a state that is at least [SUCCESS]. */
        fun hasData(): Boolean =
            ((this == SUCCESS) || (this == REFRESHING) || (this == LOADING_EXTRA_DATA))
    }
}