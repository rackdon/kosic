package rackdon.kosic.utils

import arrow.core.Either
import arrow.core.Left
import arrow.fx.IO
import arrow.fx.IO.Companion.cancellable
import arrow.fx.reactor.MonoK
import arrow.fx.reactor.value
import reactor.core.publisher.Mono

fun <A> IO<A>.toMonoK(): MonoK<A> =
    MonoK(Mono.create<A> { sink ->
        val dispose = unsafeRunAsyncCancellable { result ->
            result.fold(sink::error, sink::success)
        }
        sink.onCancel { dispose.invoke() }
    })

fun <A> MonoK<A>.toIO(): IO<A> =
    cancellable { cb ->
        val dispose = value().subscribe({ a -> cb(Either.Right(a)) }, { e -> cb(Left(e)) })
        IO { dispose.dispose() }
    }
