package utils

sealed class Either<out A, out B> {
    class Left<A>(val value: A) : Either<A, Nothing>()
    class Right<B>(val value: B) : Either<Nothing, B>()
}


inline fun <L, R, T> Either<L, R>.fold(left: (L) -> T, right: (R) -> T): T {
    return when (this) {
        is Either.Left -> left(value)
        is Either.Right -> right(value)
    }
}
