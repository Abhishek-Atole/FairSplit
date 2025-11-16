package com.fairsplit.domain.util

/**
 * Sealed class for handling operation results
 * Used throughout the app for error handling
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Exception? = null) : Result<Nothing>()
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    @Suppress("UNCHECKED_CAST")
    fun <R> getOrDefault(default: R): R = when (this) {
        is Success -> data as R
        is Error -> default
    }
    
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception ?: Exception(message)
    }
    
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (String) -> Unit): Result<T> {
        if (this is Error) action(message)
        return this
    }
}
