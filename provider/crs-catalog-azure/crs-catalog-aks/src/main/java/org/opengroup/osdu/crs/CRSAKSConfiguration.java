package org.opengroup.osdu.crs;

import org.opengroup.osdu.crs.model.CatalogImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileReader;

import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Parameter;

import static org.opengroup.osdu.crs.configuration.SwaggerConfiguration.PASS_AS_HEADER;

@Configuration
public class CRSAKSConfiguration {

    @Bean
    public CatalogImpl getCatalog(@Value("${osdu.crs.catalog.filename}") String location) throws Exception {
        try (FileReader reader = new FileReader(location)) {
            return CatalogImpl.createCatalog(reader);
        }
    }

}
