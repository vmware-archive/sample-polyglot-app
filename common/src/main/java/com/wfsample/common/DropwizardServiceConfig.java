package com.wfsample.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

/**
 * Dropwizard based service configurtion.
 *
 * @author Srujan Narkedamalli (snarkedamall@wavefront.com).
 */
public class DropwizardServiceConfig extends Configuration {

  /**
   * Port on which the styling service is running.
   */
  @JsonProperty
  private int stylingPort = 50051;

  /**
   * Port on which printing service is running.
   */
  @JsonProperty
  private int printingPort = 50052;

  /**
   * Port on which packaging service is running.
   */
  @JsonProperty
  private int packagingPort = 50053;

  /**
   * Port on which delivery service is running.
   */
  @JsonProperty
  private int deliveryPort = 50054;

  /**
   * Host on which the styling service is running.
   */
  @JsonProperty
  private String stylingHost = "stylingService";

  /**
   * Host on which the styling service is running.
   */
  @JsonProperty
  private String printingHost = "printingService";

  /**
   * Host on which the styling service is running.
   */
  @JsonProperty
  private String packagingHost = "packagingService";

  /**
   * Host on which the delivery service is running.
   */
  @JsonProperty
  private String deliveryHost = "deliveryService";

  /**
   * Path to the Application tags yaml file.
   */
  @JsonProperty
  private String applicationTagsYamlFile;

  /**
   * Path to the wavefront reporting config yaml file.
   */
  @JsonProperty
  private String wfReportingConfigYamlFile;

  public int getStylingPort() {
    return stylingPort;
  }

  public int getPrintingPort() {
    return printingPort;
  }

  public int getPackagingPort() {
    return packagingPort;
  }

  public int getDeliveryPort() {
    return deliveryPort;
  }

  public String getStylingHost() {
    return stylingHost;
  }

  public String getPrintingHost() {
    return printingHost;
  }

  public String getPackagingHost() {
    return packagingHost;
  }

  public String getDeliveryHost() {
    return deliveryHost;
  }

  public String getApplicationTagsYamlFile() {
    return applicationTagsYamlFile;
  }

  public String getWfReportingConfigYamlFile() {
    return wfReportingConfigYamlFile;
  }
}
