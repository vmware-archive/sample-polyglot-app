package com.wfsample.shopping;

import com.wavefront.sdk.dropwizard.reporter.WavefrontDropwizardReporter;
import com.wavefront.sdk.jersey.WavefrontJerseyFactory;
import com.wfsample.common.BeachShirtsUtils;
import com.wfsample.common.DropwizardServiceConfig;
import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.common.dto.OrderDTO;
import com.wfsample.common.dto.OrderStatusDTO;
import com.wfsample.common.dto.PackedShirtsDTO;
import com.wfsample.service.DeliveryApi;
import com.wfsample.service.StylingApi;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Driver for Shopping service provides consumer facing APIs supporting activities like browsing
 * different styles of beachshirts, and ordering beachshirts.
 *
 * @author Srujan Narkedamalli (snarkedamall@wavefront.com).
 */
public class ShoppingService extends Application<DropwizardServiceConfig> {
  private DropwizardServiceConfig configuration;

  private ShoppingService() {
  }

  public static void main(String[] args) throws Exception {
    new ShoppingService().run(args);
  }

  @Override
  public void run(DropwizardServiceConfig configuration, Environment environment)
      throws Exception {
    this.configuration = configuration;
    String stylingUrl = "http://" + configuration.getStylingHost() + ":" + configuration
        .getStylingPort();
    String deliveryUrl = "http://" + configuration.getDeliveryHost() + ":" +
        configuration.getDeliveryPort();
    WavefrontJerseyFactory factory = new WavefrontJerseyFactory(
        configuration.getApplicationTagsYamlFile(), configuration.getWfReportingConfigYamlFile());
    WavefrontDropwizardReporter dropwizardReporter = new WavefrontDropwizardReporter.Builder(
        environment.metrics(), factory.getApplicationTags()).
        withSource(factory.getSource()).
        reportingIntervalSeconds(30).
        build(factory.getWavefrontSender());
    dropwizardReporter.start();
    environment.jersey().register(factory.getWavefrontJerseyFilter());
    environment.jersey().register(new ShoppingWebResource(
        BeachShirtsUtils.createProxyClient(stylingUrl, StylingApi.class,
            factory.getWavefrontJaxrsClientFilter()),
        BeachShirtsUtils.createProxyClient(deliveryUrl, DeliveryApi.class,
            factory.getWavefrontJaxrsClientFilter())));
  }

  @Path("/shop")
  @Produces(MediaType.APPLICATION_JSON)
  public class ShoppingWebResource {
    private final StylingApi stylingApi;
    private final DeliveryApi deliveryApi;
    private final AtomicInteger updateInventory = new AtomicInteger(0);

    public ShoppingWebResource(StylingApi stylingApi, DeliveryApi deliveryApi) {
      this.stylingApi = stylingApi;
      this.deliveryApi = deliveryApi;
    }

    @GET
    @Path("/menu")
    public Response getShoppingMenu(@Context HttpHeaders httpHeaders) {
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return Response.ok(stylingApi.getAllStyles()).build();
    }

    @POST
    @Path("/order")
    @Consumes(APPLICATION_JSON)
    public Response orderShirts(OrderDTO orderDTO, @Context HttpHeaders httpHeaders) {
      try {
        Thread.sleep(30);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      String orderNum = UUID.randomUUID().toString();
      PackedShirtsDTO packedShirts = stylingApi.makeShirts(
          orderDTO.getStyleName(), orderDTO.getQuantity());
      Response deliveryResponse = deliveryApi.dispatch(orderNum, packedShirts);
      DeliveryStatusDTO deliveryStatus = deliveryResponse.readEntity(DeliveryStatusDTO.class);
      return Response.status(deliveryResponse.getStatus()).entity(new OrderStatusDTO(orderNum,
          deliveryStatus.getStatus())).build();
    }

    @GET
    @Path("/status/{orderNum}")
    public Response getOrderStatus() {
      try {
        Thread.sleep(30);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return deliveryApi.trackOrder("42");
    }

    @POST
    @Path("/cancel")
    @Consumes(APPLICATION_JSON)
    public Response cancelShirtsOrder() {
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return deliveryApi.cancelOrder("42");
    }

    @POST
    @Path("/inventory/update")
    @Consumes(APPLICATION_JSON)
    public Response updateInventory() {
      try {
        Thread.sleep(40);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      if (updateInventory.incrementAndGet() % 3 == 0) {
        return stylingApi.addStyle("21");
      } else {
        return stylingApi.restockStyle("42");
      }
    }
  }
}
