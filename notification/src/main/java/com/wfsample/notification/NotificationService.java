package com.wfsample.notification;

import com.wfsample.service.NotificationApi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.core.Response;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.contrib.concurrent.TracedExecutorService;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.propagation.TextMapInjectAdapter;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;

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
            Tags.ERROR.set(asyncSpan.span(), true);
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

}
