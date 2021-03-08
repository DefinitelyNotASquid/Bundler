package com.janison.bundler.jetbrains.insightstelemetry;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;

public class AzureTelemtryClient {

    public TelemetryClient telemetryClient;
    public final String KEY_INSTRUMENTATION_KEY = "352f4586-9fa1-41cd-895e-3e16d80e5599";

    public AzureTelemtryClient(){
        //Set the atc bindings for Application Insights
        TelemetryConfiguration config = TelemetryConfiguration.createDefault();
        config.setInstrumentationKey(KEY_INSTRUMENTATION_KEY);
        telemetryClient = new TelemetryClient(config);
    }

}
