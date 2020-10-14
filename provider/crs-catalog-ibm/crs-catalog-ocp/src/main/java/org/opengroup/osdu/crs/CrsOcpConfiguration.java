package org.opengroup.osdu.crs;

import org.opengroup.osdu.crs.model.CatalogImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Logger;

@Configuration
public class CrsOcpConfiguration {
	
	private static Logger logger = Logger.getLogger(CrsOcpConfiguration.class.getName());

	private static final String DEFAULT_CATALOG_FILE = "crs_catalog_v2.json";  // later make it as config map and mount it as file on oc cluster.
	
    @Bean
    public CatalogImpl getCatalog(@Value("${osdu.crs.catalog.filename:null}") String location) throws Exception {
        
    	logger.info("osdu.crs.catalog.filename" + location);
        try (FileReader reader = new FileReader(location)) {
            return CatalogImpl.createCatalog(reader);
        } catch (NullPointerException e) {
        	logger.info("osdu.crs.catalog.filename");
		} catch (FileNotFoundException e) {
        	logger.info("File not found");
		}
    	
      	ClassPathResource cpr = new ClassPathResource(DEFAULT_CATALOG_FILE);
        InputStream inputStream = cpr.getInputStream();
        Reader reader = new InputStreamReader(inputStream);
        return CatalogImpl.createCatalog(reader);
        
    	
    }

}
