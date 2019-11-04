package de.adorsys.ledgers.middleware.rest.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.middleware.api.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrencyValidationFilter extends GenericFilterBean {
    private final CurrencyService currencyService;
    private final ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        MultiReadHttpServletRequest servletRequest = new MultiReadHttpServletRequest((HttpServletRequest) request);
        if (servletRequest.getContentLength() > 0) {
            try {
                List<JsonNode> currencies = objectMapper.readTree(servletRequest.getInputStream())
                                                    .findValues("currency");
                for (JsonNode node : currencies) {
                    if (!currencyService.getSupportedCurrencies().toString().contains(node.asText())) {
                        log.error("Invalid currency: {}", node.asText());
                        ((HttpServletResponse) response).sendError(400, String.format("Invalid currency %s", node.asText()));
                        return;
                    }
                }
            } catch (IOException e) {
                String msg = String.format("Could not parse request body, msg: %s", e.getMessage());
                log.error(msg);
                ((HttpServletResponse) response).sendError(400, msg);
                return;
            }
        }
        chain.doFilter(servletRequest, response);
    }
}