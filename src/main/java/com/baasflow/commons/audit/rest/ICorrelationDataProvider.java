package com.baasflow.commons.audit.rest;

import java.util.Collections;
import java.util.Map;

public interface ICorrelationDataProvider {

  default Map<String, String> correlationParams() {
    return Collections.emptyMap();
  }
}
