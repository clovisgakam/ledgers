package de.adorsys.ledgers.middleware.client.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PaymentMapperConfiguration {
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String DEFAULT_PAYMENT_HOLDER = "classpath:payment_mapping.yml";
    private static final String FILE_PREFIX = "file:";
    private static final ResourceLoader resourceLoader = new DefaultResourceLoader();
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper = new XmlMapper();

    @Value("${payment_mapping.path:}")
    private String paymentMapping;

    @Bean
    public PaymentMapperTO paymentMapperTO() throws IOException {
        try {
            Resource resource = resourceLoader.getResource(resolveYmlToRead());
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            PaymentMapperTO mapperTO = yamlMapper.readValue(resource.getInputStream(), PaymentMapperTO.class);
            mapperTO.setMapper(objectMapper);
            mapperTO.setXmlMapper(xmlMapper);
            return mapperTO;
        } catch (IOException e) {
            log.error("Could not process payment mapper configuration!");
            throw e;
        }
    }


    private String resolveYmlToRead() {
        if (StringUtils.isBlank(paymentMapping)) {
            return DEFAULT_PAYMENT_HOLDER;
        } else {
            if (paymentMapping.startsWith(CLASSPATH_PREFIX) || paymentMapping.startsWith(FILE_PREFIX)) {
                return paymentMapping;
            }
            return CLASSPATH_PREFIX + paymentMapping;
        }
    }
}
