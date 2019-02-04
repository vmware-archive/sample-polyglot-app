package com.wfsample.notification;

import com.wavefront.config.WavefrontReportingConfig;
import com.wavefront.opentracing.WavefrontTracer;
import com.wavefront.opentracing.reporting.WavefrontSpanReporter;
import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.common.application.ApplicationTags;
import com.wavefront.sdk.jaxrs.reporter.WavefrontJaxrsReporter;
import com.wavefront.sdk.jaxrs.server.WavefrontJaxrsServerFilter;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.core.env.Environment;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import io.opentracing.Tracer;

import static com.wavefront.config.ReportingUtils.constructApplicationTags;
import static com.wavefront.config.ReportingUtils.constructWavefrontReportingConfig;
import static com.wavefront.config.ReportingUtils.constructWavefrontSender;

/**
 * Dynamic Feature of Wavefront JAX-RS Filter.
 *
 * @author Hao Song (songhao@vmware.com).
 */
@Provider
public class WavefrontJaxrsFeature implements DynamicFeature {
  // TODO: Move DynamicFeature into Wavefront-Jaxrs-SDK
  private final Environment env;

  WavefrontJaxrsFeature(Environment env) {
    this.env = env;
  }

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext featureContext) {
    ApplicationTags applicationTags =
        constructApplicationTags(env.getProperty("applicationTagsYamlFile"));
    WavefrontReportingConfig wfReportingConfig =
        constructWavefrontReportingConfig(env.getProperty("wfReportingConfigYamlFile"));
    String source = wfReportingConfig.getSource();
    WavefrontSender wavefrontSender = constructWavefrontSender(wfReportingConfig);
    WavefrontJaxrsReporter wfJaxrsReporter = new WavefrontJaxrsReporter.Builder
        (applicationTags).withSource(source).build(wavefrontSender);
    WavefrontJaxrsServerFilter.Builder wfJaxrsFilterBuilder = new WavefrontJaxrsServerFilter.Builder
        (wfJaxrsReporter, applicationTags);
    if (BooleanUtils.isTrue(wfReportingConfig.getReportTraces())) {
      WavefrontSpanReporter wfSpanReporter;
      wfSpanReporter = new WavefrontSpanReporter.Builder().withSource(source).build(wavefrontSender);
      Tracer tracer = new WavefrontTracer.Builder(wfSpanReporter, applicationTags).build();
      wfJaxrsFilterBuilder.withTracer(tracer);
    }
    wfJaxrsReporter.start();
    WavefrontJaxrsServerFilter wfJaxrsServerFilter = wfJaxrsFilterBuilder.build();
    featureContext.register(wfJaxrsServerFilter);
  }
}

