package org.yttr.glyph

/**
 * Either type, Left or Right
 */
sealed class Either<out L, out R> {
    /**
     * Left of an Either
     */
    data class Left<out L>(
        /**
         * Value of the Left
         */
        val l: L
    ) : Either<L, Nothing>()

    /**
     * Right of an Either
     */
    data class Right<out R>(
        /**
         * Value of the Right
         */
        val r: R
    ) : Either<Nothing, R>()
}

/**
 * Wrap value in Left
 */
fun <T> T.left(): Either.Left<T> = Either.Left(this)

/**
 * Wrap value in Right
 */
fun <T> T.right(): Either.Right<T> = Either.Right(this)
