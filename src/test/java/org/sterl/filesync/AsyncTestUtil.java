package org.sterl.filesync;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class AsyncTestUtil {

    public static <T> void waitForEquals(T t, Callable<T> expression) throws Exception {
        waitForEquals(t, expression, 1, TimeUnit.SECONDS);
    }
    public static <T> T waitFor(Callable<T> expression, Predicate<T> predicate, long time, TimeUnit unit) throws Exception {
        final long start = System.currentTimeMillis();
        T result = null;
        while (unit.toMillis(time) > (System.currentTimeMillis() - start)) {
            result = expression.call();
            if (predicate.test(result)) {
                // okay
                break;
            } else {
                Thread.sleep(50);
            }
        }
        return result;
    }

    public static <T> void waitForEquals(T t, Callable<T> expression, long time, TimeUnit unit) throws Exception {
        T result = waitFor(expression, value -> t.equals(value), time, unit);
        assertEquals(t, result);
    }
}
