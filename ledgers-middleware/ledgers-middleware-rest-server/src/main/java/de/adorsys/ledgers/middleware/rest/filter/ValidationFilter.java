package de.adorsys.ledgers.middleware.rest.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.middleware.api.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.IBANValidator;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_XML;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationFilter extends OncePerRequestFilter {
    private static final String IBAN = "iban";
    private static final String CURRENCY = "currency";

    private final CurrencyService currencyService;
    private final ObjectMapper mapper;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        MultiReadHttpServletRequest servletRequest = new MultiReadHttpServletRequest(request);
        boolean isXmlContent = StringUtils.equals(APPLICATION_XML.toString(), servletRequest.getHeader(CONTENT_TYPE));
        if (isXmlContent) {
            // TODO implement iban validation for xml payment
            chain.doFilter(servletRequest, response);
            return;
        }
        try {
            Optional<String> invalidIban = validate(readValuesByField(servletRequest, IBAN), v -> IBANValidator.getInstance().isValid(v));
            if (invalidIban.isPresent()) {
                buildError(response, invalidIban.get());
                return;
            }
            Optional<String> invalidCurrency = validate(readValuesByField(servletRequest, CURRENCY),
                                                        v -> currencyService.getSupportedCurrencies().stream().anyMatch(c -> StringUtils.equals(c.getCurrencyCode(), v)));
            if (invalidCurrency.isPresent()) {
                buildError(response, invalidCurrency.get());
                return;
            }
        } catch (IOException e) {
            String msg = String.format("Could not parse request body, msg: %s", e.getMessage());
            log.error(msg);
            response.sendError(400, msg);
            return;
        }
        chain.doFilter(servletRequest, response);
    }

    private Collection<String> readValuesByField(MultiReadHttpServletRequest servletRequest, String fieldName) throws IOException {
        JsonNode jsonNode = mapper.readTree(servletRequest.getInputStream());
        return jsonNode != null
                       ? jsonNode.findValuesAsText(fieldName)
                       : CollectionUtils.emptyCollection();
    }

    private Optional<String> validate(Collection<String> values, Predicate<String> predicate) {
        return values.stream().filter(predicate.negate())
                       .findFirst();
    }

    private void buildError(HttpServletResponse response, String value) throws IOException {
        log.error("Invalid value: {}", value);
        response.sendError(400, String.format("Invalid value: %s", value));
    }
}
