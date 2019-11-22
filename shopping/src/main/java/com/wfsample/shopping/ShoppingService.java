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
import com.wfsample.service.InventoryApi;
import com.wfsample.service.PaymentsApi;
import com.wfsample.service.ShoppingApi;
import com.wfsample.service.StylingApi;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;

import javax.ws.rs.core.Response;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

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
  public void run(DropwizardServiceConfig configuration, Environment environment) {
    this.configuration = configuration;
    String inventoryUrl = "http://" + configuration.getInventoryHost() + ":" +
        configuration.getInventoryPort();
    String paymentsUrl = "http://" + configuration.getPaymentsHost() + ":" +
        configuration.getPaymentsPort();
    String stylingUrl = "http://" + configuration.getStylingHost() + ":" +
        configuration.getStylingPort();
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
        BeachShirtsUtils.createProxyClient(inventoryUrl, InventoryApi.class,
            factory.getWavefrontJaxrsClientFilter()),
        BeachShirtsUtils.createProxyClient(paymentsUrl, PaymentsApi.class,
            factory.getWavefrontJaxrsClientFilter()),
        BeachShirtsUtils.createProxyClient(stylingUrl, StylingApi.class,
            factory.getWavefrontJaxrsClientFilter()),
        BeachShirtsUtils.createProxyClient(deliveryUrl, DeliveryApi.class,
            factory.getWavefrontJaxrsClientFilter()), factory.getTracer()));
  }

  public class ShoppingWebResource implements ShoppingApi {
    private final InventoryApi inventoryApi;
    private final PaymentsApi paymentsApi;
    private final StylingApi stylingApi;
    private final DeliveryApi deliveryApi;
    private final Tracer tracer;
    private final AtomicInteger updateInventory = new AtomicInteger(0);
    private final AtomicInteger removeFromMenu = new AtomicInteger(0);

    public ShoppingWebResource(InventoryApi inventoryApi, PaymentsApi paymentsApi,
                               StylingApi stylingApi, DeliveryApi deliveryApi, Tracer tracer) {
      this.inventoryApi = inventoryApi;
      this.stylingApi = stylingApi;
      this.deliveryApi = deliveryApi;
      this.paymentsApi = paymentsApi;
      this.tracer = tracer;
    }

    @Override
    public Response getShoppingMenu() {
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return Response.ok(stylingApi.getAllStyles()).build();
    }

    @Override
    public Response orderShirts(OrderDTO orderDTO) {
      try {
        Thread.sleep(30);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      String orderNum = UUID.randomUUID().toString();
      Response inventoryResponse = inventoryApi.available(orderDTO.getStyleName());
      if (inventoryResponse.getStatus() >= 400) {
        return Response.status(inventoryResponse.getStatus()).entity("Items out of stock, " +
            "please try again later").build();
      }
      Response paymentResponse = paymentsApi.pay(orderNum, orderDTO.getPayment());
      if (paymentResponse.getStatus() >= 400) {
        return Response.status(paymentResponse.getStatus()).entity("Payment not successful, " +
            "please try again later").build();
      }
      PackedShirtsDTO packedShirts = stylingApi.makeShirts(
          orderDTO.getStyleName(), orderDTO.getQuantity());
      Response deliveryResponse = deliveryApi.dispatch(orderNum, packedShirts);
      DeliveryStatusDTO deliveryStatus = deliveryResponse.readEntity(DeliveryStatusDTO.class);
      return Response.status(deliveryResponse.getStatus()).entity(new OrderStatusDTO(orderNum,
          deliveryStatus.getStatus())).build();
    }

    @Override
    public Response getOrderStatus() {
      try {
        Thread.sleep(30);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return deliveryApi.trackOrder("42");
    }

    @Override
    public Response cancelShirtsOrder() {
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return deliveryApi.cancelOrder("42");
    }

    @Override
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

    @Override
    public Response removeFromMenu(String id) {
      try {
        Thread.sleep(40);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      if (removeFromMenu.incrementAndGet() % 11 == 0) {
        Span span = tracer.activeSpan();
        if (span != null) {
          Tags.ERROR.set(span, true);
        }
      }
      return Response.ok().build();
    }
  }
}
