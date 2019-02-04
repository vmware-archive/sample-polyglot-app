package com.wfsample.notification;

import com.wfsample.service.NotificationApi;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Implementation of Notification Service.
 *
 * @author Hao Song (songhao@vmware.com).
 */
@Path("/notify")
@Service
public class NotificationService implements NotificationApi {
  AtomicInteger notify = new AtomicInteger(0);

  public Response notify(String trackNum) {
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    if (notify.incrementAndGet() % 10 == 0) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Unable to send " +
          "notification").build();
    }
    return Response.ok("Track Num: " + trackNum + " notified!").build();
  }

}