package at.htl.catalogue;

import io.quarkus.logging.Log;
import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Timed
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class TimingInterceptor {

    @AroundInvoke
    Object measure(InvocationContext ctx) throws Exception {
        long start = System.nanoTime();
        try {
            return ctx.proceed();
        } finally {
            Log.infof("%s took %d us", ctx.getMethod().getName(), (System.nanoTime() - start) / 1000);
        }
    }
}
