package example

import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger


class ExamplesTest {
    @Test
    fun async_without_blocking_test() {
        val hitCount = AtomicInteger()

        Observable.interval(1, TimeUnit.SECONDS)
            .take(5)
            .subscribe { hitCount.incrementAndGet() }
        assertTrue(
            "actual count = " + hitCount.get(),
            hitCount.get() == 5
        )
    }

    @Test
    fun async_with_blocking_test() {
        val hitCount = AtomicInteger()

        Observable.interval(1, TimeUnit.SECONDS)
            .take(5)
            .blockingSubscribe { hitCount.incrementAndGet() }
        assertTrue(
            "actual count = " + hitCount.get(),
            hitCount.get() == 5
        )
    }

    @Test
    fun async_blocking_operator() {
        val allLengthFour = Observable.just(
            "Alpha", "Beta", "Gamma",
            "Delta", "Zeta"
        ).filter { s: String -> s.length == 4 }
            .toList()
            .blockingGet()
        assertTrue(allLengthFour == listOf("Beta", "Zeta"))
    }
}
