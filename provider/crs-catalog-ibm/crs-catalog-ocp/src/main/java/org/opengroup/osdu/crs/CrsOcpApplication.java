/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.crs;

import org.opengroup.osdu.core.common.logging.DefaultLogger;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CrsOcpApplication {

  @Bean
  public JaxRsDpsLog getJaxRsDpsLog() {
    return new JaxRsDpsLog(new DefaultLogger(), new DpsHeaders());
  }

  @Bean
  public DpsHeaders getDpsHeaders() {
    return new DpsHeaders();
  }

  public static void main(String[] args) {
    SpringApplication.run(CrsOcpApplication.class, args);
  }
}
