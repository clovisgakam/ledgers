package de.adorsys.ledgers.middleware.client.mappers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.general.AddressTO;
import de.adorsys.ledgers.middleware.api.domain.payment.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;

import static de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO.BULK;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMapperTO {
    private Map<String, List<String>> debtorPart;
    private Map<String, List<String>> creditorPart;
    private Map<String, List<String>> address;
    @JsonIgnore
    private ObjectMapper mapper;
    @JsonIgnore
    private XmlMapper xmlMapper = new XmlMapper();

    @JsonIgnore
    public PaymentTO toAbstractPayment(String payment, String paymentType, String paymentProduct) {
        JsonNode node = readTree(payment, paymentProduct);
        PaymentTO paymentTO = parseDebtorPart(paymentType, paymentProduct, node);
        List<PaymentTargetTO> parts = parseCreditorParts(node, paymentTO.getPaymentType());
        paymentTO.setTargets(parts);
        return paymentTO;
    }

    private JsonNode readTree(String payment, String product) {
        try {
            return product.contains("pain") || product.contains("xml")
                           ? xmlMapper.readTree(payment)
                           : mapper.readTree(payment);
        } catch (IOException e) {
            log.error("Read tree exception {}, {}", e.getCause(), e.getMessage());
            throw new IllegalArgumentException("Could not parse payment!");
        }
    }

    private List<PaymentTargetTO> parseCreditorParts(JsonNode node, PaymentTypeTO type) {
        List<PaymentTargetTO> targets = new ArrayList<>();
        if (type == BULK) {
            node.get("payments").elements()
                    .forEachRemaining(n -> targets.add(mapTarget(n)));
        } else {
            targets.add(mapTarget(node));
        }
        return targets;
    }

    private PaymentTO parseDebtorPart(String paymentType, String paymentProduct, JsonNode node) {
        PaymentTO paymentTO = new PaymentTO();
        paymentTO.setPaymentProduct(paymentProduct);
        paymentTO.setPaymentType(PaymentTypeTO.valueOf(paymentType));

        mapProperties(debtorPart, "batchBookingPreferred", node, paymentTO::setRequestedExecutionDate, LocalDate.class);
        mapProperties(debtorPart, "requestedExecutionTime", node, paymentTO::setRequestedExecutionTime, LocalTime.class);
        mapProperties(debtorPart, "requestedExecutionTime", node, paymentTO::setRequestedExecutionTime, LocalTime.class);
        mapProperties(debtorPart, "startDate", node, paymentTO::setStartDate, LocalDate.class);
        mapProperties(debtorPart, "endDate", node, paymentTO::setEndDate, LocalDate.class);
        mapProperties(debtorPart, "executionRule", node, paymentTO::setExecutionRule, String.class);
        mapProperties(debtorPart, "frequency", node, paymentTO::setFrequency, FrequencyCodeTO.class);
        mapProperties(debtorPart, "dayOfExecution", node, paymentTO::setDayOfExecution, Integer.class);
        mapProperties(debtorPart, "debtorAccount", node, paymentTO::setDebtorAccount, AccountReferenceTO.class);
        mapProperties(debtorPart, "debtorAgent", node, paymentTO::setDebtorAgent, String.class);
        mapProperties(debtorPart, "debtorName", node, paymentTO::setDebtorName, String.class);
        mapProperties(debtorPart, "transactionStatus", node, paymentTO::setTransactionStatus, TransactionStatusTO.class);
        return paymentTO;
    }

    private PaymentTargetTO mapTarget(JsonNode node) {
        PaymentTargetTO target = new PaymentTargetTO();
        mapProperties(creditorPart, "endToEndIdentification", node, target::setEndToEndIdentification, String.class);
        mapProperties(creditorPart, "instructedAmount", node, target::setInstructedAmount, AmountTO.class);
        mapProperties(creditorPart, "currencyOfTransfer", node, target::setCurrencyOfTransfer, Currency.class);
        mapProperties(creditorPart, "creditorAccount", node, target::setCreditorAccount, AccountReferenceTO.class);
        mapProperties(creditorPart, "creditorAgent", node, target::setCreditorAgent, String.class);
        mapProperties(creditorPart, "creditorName", node, target::setCreditorName, String.class);
        mapProperties(creditorPart, "purposeCode", node, target::setPurposeCode, PurposeCodeTO.class);
        mapProperties(creditorPart, "remittanceInformationUnstructured", node, target::setRemittanceInformationUnstructured, String.class);
        mapProperties(creditorPart, "remittanceInformationStructured", node, target::setRemittanceInformationStructured, RemittanceInformationStructuredTO.class);
        mapProperties(creditorPart, "chargeBearer", node, target::setChargeBearerTO, ChargeBearerTO.class);

        target.setCreditorAddress(mapAddress(node.get("creditorAddress")));
        return target;
    }

    private AddressTO mapAddress(JsonNode node) {
        AddressTO addressTO = new AddressTO();
        mapProperties(address, "street", node, addressTO::setStreet, String.class);
        mapProperties(address, "buildingNumber", node, addressTO::setBuildingNumber, String.class);
        mapProperties(address, "city", node, addressTO::setCity, String.class);
        mapProperties(address, "postalCode", node, addressTO::setPostalCode, String.class);
        mapProperties(address, "country", node, addressTO::setCountry, String.class);
        mapProperties(address, "line1", node, addressTO::setStreet, String.class);
        mapProperties(address, "line1", node, addressTO::setStreet, String.class);
        return addressTO;
    }

    private <T> void mapProperties(Map<String, List<String>> map, String property, JsonNode node, Consumer<T> consumer, Class<T> clazz) {
        Optional.ofNullable(map.get(property))
                .ifPresent(pr -> pr.forEach(p -> mapProperty(node, consumer, clazz, p)));
    }

    private <T> void mapProperty(JsonNode node, Consumer<T> consumer, Class<T> clazz, String p) {
        Optional.ofNullable(node.get(p))
                .map(n -> mapObject(n, clazz))
                .ifPresent(consumer);
    }

    private <T> T mapObject(JsonNode node, Class<T> clazz) {
        try {
            return mapper.readValue(node.toString(), clazz);
        } catch (IOException e) {
            log.error("Read tree exception {}, {}", e.getCause(), e.getMessage());
        }
        return null;
    }
}
