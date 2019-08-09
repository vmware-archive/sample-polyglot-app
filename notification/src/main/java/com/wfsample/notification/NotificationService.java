package com.wfsample.notification;

import com.google.common.collect.ImmutableMap;
import com.wfsample.service.NotificationApi;
import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.contrib.concurrent.TracedExecutorService;
import io.opentracing.log.Fields;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementation of Notification Service.
 *
 * @author Hao Song (songhao@vmware.com).
 */
@Service
public class NotificationService implements NotificationApi {
  private final Random rand = new Random();
  private final Tracer tracer;
  private final ExecutorService notificationExecutor;

  @Autowired
  public NotificationService() {
    this.tracer = GlobalTracer.get();
    notificationExecutor = new TracedExecutorService(
        Executors.newFixedThreadPool(2 * Runtime.getRuntime().availableProcessors()), tracer);
  }

  public Response notify(String trackNum) {
    notificationExecutor.submit(new InternalNotifyService());
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return Response.accepted().build();
  }

  class InternalNotifyService implements Runnable {

    @Override
    public void run() {
      try (Scope asyncSpan = tracer.buildSpan("asyncNotify").
          startActive(true)) {
        try {
          Thread.sleep(200);
          if (rand.nextInt(100) == 50) {
            throw new NullPointerException();
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (Exception e) {
          Tags.ERROR.set(asyncSpan.span(), true);
          asyncSpan.span().log(ImmutableMap.of(
              Fields.EVENT, "error",
              Fields.ERROR_KIND, e.getClass().getName(),
              Fields.STACK, ExceptionUtils.getStackTrace(e)
          ));
        }
      }
    }
  }

}
