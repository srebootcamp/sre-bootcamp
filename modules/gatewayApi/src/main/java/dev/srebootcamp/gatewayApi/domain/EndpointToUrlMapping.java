package dev.srebootcamp.gatewayApi.domain;

import dev.srebootcamp.gatewayApi.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@PropertySource("classpath:gateway.properties")
public class EndpointToUrlMapping implements IEndpointToUrlMapping {


    Map<String, String> endpointToUrlMapping;

    EndpointToUrlMapping(@Value("${endpoint.approval.ochestration.url}") String orchestration,
                         @Value("${endpoint.login.url}") String loginUrl) {
        if (orchestration == null) throw new RuntimeException("endpoint.approval.ochestration.url is not defined");
        if (loginUrl == null) throw new RuntimeException("endpoint.login.url is not defined");
        endpointToUrlMapping = Map.of(
                "payment", orchestration,
                "login", loginUrl);
    }


    @Override
    public String getBaseUrlFor(String urlString) {
        String key = StringUtils.firstSegment(urlString);
        String url = endpointToUrlMapping.get(key);
        if (url == null)
            throw new NoGatewayException("No mapping found for " + urlString + " key is [" + key + "]. Legal values are " + endpointToUrlMapping.keySet());
        return url;
    }
}
