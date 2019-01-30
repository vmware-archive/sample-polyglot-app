package com.wfsample.styling;

import com.wavefront.sdk.dropwizard.reporter.WavefrontDropwizardReporter;
import com.wavefront.sdk.grpc.WavefrontClientInterceptor;
import com.wavefront.sdk.grpc.reporter.WavefrontGrpcReporter;
import com.wavefront.sdk.jersey.WavefrontJerseyFactory;
import com.wfsample.beachshirts.Color;
import com.wfsample.beachshirts.PackagingGrpc;
import com.wfsample.beachshirts.PrintRequest;
import com.wfsample.beachshirts.PrintingGrpc;
import com.wfsample.beachshirts.Shirt;
import com.wfsample.beachshirts.ShirtStyle;
import com.wfsample.beachshirts.Void;
import com.wfsample.beachshirts.WrapRequest;
import com.wfsample.beachshirts.WrappingType;
import com.wfsample.common.DropwizardServiceConfig;
import com.wfsample.common.dto.PackedShirtsDTO;
import com.wfsample.common.dto.ShirtStyleDTO;
import com.wfsample.service.StylingApi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.Response;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * Driver for styling service which manages different styles of shirts and takes orders for a shirts
 * of a given style.
 *
 * @author Srujan Narkedamalli (snarkedamall@wavefront.com).
 */
public class StylingService extends Application<DropwizardServiceConfig> {
  private DropwizardServiceConfig configuration;

  private StylingService() {
  }

  public static void main(String[] args) throws Exception {
    new StylingService().run(args);
  }

  @Override
  public void run(DropwizardServiceConfig configuration, Environment environment)
      throws Exception {
    this.configuration = configuration;
    WavefrontJerseyFactory factory = new WavefrontJerseyFactory(
        configuration.getApplicationTagsYamlFile(), configuration.getWfReportingConfigYamlFile());
    WavefrontDropwizardReporter dropwizardReporter = new WavefrontDropwizardReporter.Builder(
        environment.metrics(), factory.getApplicationTags()).
        withSource(factory.getSource()).
        reportingIntervalSeconds(30).
        build(factory.getWavefrontSender());
    dropwizardReporter.start();
    WavefrontGrpcReporter grpcReporter = new WavefrontGrpcReporter.Builder(
        factory.getApplicationTags()).
        withSource(factory.getSource()).
        reportingIntervalSeconds(30).
        build(factory.getWavefrontSender());
    grpcReporter.start();
    WavefrontClientInterceptor interceptor =
        new WavefrontClientInterceptor.Builder(grpcReporter, factory.getApplicationTags()).
            withTracer(factory.getTracer()).recordStreamingStats().build();
    environment.jersey().register(factory.getWavefrontJerseyFilter());
    environment.jersey().register(new StylingWebResource(interceptor));
  }

  public class StylingWebResource implements StylingApi {
    private final PrintingGrpc.PrintingBlockingStub printing;
    private final PackagingGrpc.PackagingBlockingStub packaging;
    // sample set of static styles.
    private List<ShirtStyleDTO> shirtStyleDTOS = new ArrayList<>();

    public StylingWebResource(WavefrontClientInterceptor clientInterceptor) {
      ShirtStyleDTO dto = new ShirtStyleDTO();
      dto.setName("style1");
      dto.setImageUrl("style1Image");
      ShirtStyleDTO dto2 = new ShirtStyleDTO();
      dto2.setName("style2");
      dto2.setImageUrl("style2Image");
      shirtStyleDTOS.add(dto);
      shirtStyleDTOS.add(dto2);
      ManagedChannel printingChannel = ManagedChannelBuilder.forAddress(
          configuration.getPrintingHost(), configuration.getPrintingPort()).
          intercept(clientInterceptor).
          usePlaintext().build();
      ManagedChannel packagingChannel = ManagedChannelBuilder.forAddress(
          configuration.getPackagingHost(), configuration.getPackagingPort()).
          intercept(clientInterceptor).
          usePlaintext().build();
      printing = PrintingGrpc.newBlockingStub(printingChannel);
      packaging = PackagingGrpc.newBlockingStub(packagingChannel);

    }

    public List<ShirtStyleDTO> getAllStyles() {
      try {
        Thread.sleep(10);
        printing.getAvailableColors(Void.getDefaultInstance());
        Thread.sleep(10);
        packaging.getPackingTypes(Void.getDefaultInstance());
        Thread.sleep(10);
        return shirtStyleDTOS;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    public PackedShirtsDTO makeShirts(String id, int quantity) {
      try {
        Thread.sleep(20);
        Iterator<Shirt> shirts = printing.printShirts(PrintRequest.newBuilder().
            setStyleToPrint(ShirtStyle.newBuilder().setName(id).setImageUrl(id + "Image").build()).
            setQuantity(quantity).build());
        Thread.sleep(20);
        if (quantity < 30) {
          packaging.wrapShirts(WrapRequest.newBuilder().addAllShirts(() ->
              shirts).build());
        } else {
          packaging.giftWrap(WrapRequest.newBuilder().addAllShirts(() ->
              shirts).build());
        }
        Thread.sleep(20);
        return new PackedShirtsDTO(new ArrayList<>());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public Response addStyle(String id) {
      try {
        Thread.sleep(10);
        printing.addPrintColor(Color.newBuilder().setColor("rgb").build());
        Thread.sleep(10);
        return Response.ok().build();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public Response restockStyle(String id) {
      try {
        Thread.sleep(10);
        printing.restockColor(Color.newBuilder().setColor("rgb").build());
        Thread.sleep(10);
        packaging.restockMaterial(WrappingType.newBuilder().setWrappingType("wrap").build());
        return Response.ok().build();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
