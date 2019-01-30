package com.wfsample.delivery;

import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.common.dto.PackedShirtsDTO;
import com.wfsample.service.DeliveryApi;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.core.Response;

/**
 * Controller for delivery service which is responsible for dispatching shirts returning tracking
 * number for a given order.
 *
 * @author Hao Song (songhao@vmware.com).
 */
public class DeliveryController implements DeliveryApi {
  AtomicInteger tracking = new AtomicInteger(0);
  AtomicInteger dispatch = new AtomicInteger(0);
  AtomicInteger cancel = new AtomicInteger(0);

  @Override
  public Response dispatch(String orderNum, PackedShirtsDTO packedShirts) {
    if (dispatch.incrementAndGet() % 20 == 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(
          new DeliveryStatusDTO(null, "no shirts to deliver")).build();
    }
    try {
      Thread.sleep(90);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    String trackingNum = UUID.randomUUID().toString();
    System.out.println("Tracking number of Order:" + orderNum + " is " + trackingNum);
    return Response.ok(new DeliveryStatusDTO(trackingNum, "shirts delivery dispatched")).build();
  }

  @Override
  public Response trackOrder(String orderNum) {
    if (tracking.incrementAndGet() % 8 == 0) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    try {
      Thread.sleep(30);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return Response.ok().build();
  }

  @Override
  public Response cancelOrder(String orderNum) {
    try {
      Thread.sleep(45);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    if (cancel.incrementAndGet() % 7 == 0) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    return Response.ok().build();
  }
}