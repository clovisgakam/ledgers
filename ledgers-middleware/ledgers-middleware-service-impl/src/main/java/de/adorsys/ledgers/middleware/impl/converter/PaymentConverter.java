/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.api.domain.payment.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("PMD")
@Mapper(componentModel = "spring")
public interface PaymentConverter {
    PaymentResultTO toPaymentResultTO(PaymentResultBO bo);

    PaymentResultBO toPaymentResultBO(PaymentResultTO to);

    PaymentTypeBO toPaymentTypeBO(PaymentTypeTO paymentType);

    PaymentTypeTO toPaymentTypeTO(PaymentTypeBO paymentType);

    default Object toPaymentTO(PaymentBO payment) {
        if (payment.getPaymentType() == PaymentTypeBO.SINGLE) {
            return toSinglePaymentTO(payment, payment.getTargets().get(0));
        } else if (payment.getPaymentType() == PaymentTypeBO.PERIODIC) {
            return toPeriodicPaymentTO(payment, payment.getTargets().get(0));
        } else {
            return toBulkPaymentTO(payment, payment.getTargets().get(0));
        }
    }


    @Mapping(source = "payment.paymentId", target = "paymentId")
    @Mapping(source = "payment.transactionStatus", target = "paymentStatus")
    @Mapping(target = "paymentProduct", expression = "java(toPaymentProductTO(payment.getPaymentProduct()))")
    SinglePaymentTO toSinglePaymentTO(PaymentBO payment, PaymentTargetBO paymentTarget);


    @Mapping(source = "payment.paymentId", target = "paymentId")
    @Mapping(source = "payment.transactionStatus", target = "paymentStatus")
    @Mapping(target = "paymentProduct", expression = "java(toPaymentProductTO(payment.getPaymentProduct()))")
    PeriodicPaymentTO toPeriodicPaymentTO(PaymentBO payment, PaymentTargetBO paymentTarget);


    @Mapping(source = "payment.paymentId", target = "paymentId")
    @Mapping(source = "payment.transactionStatus", target = "paymentStatus")
    @Mapping(target = "paymentProduct", expression = "java(toPaymentProductTO(payment.getPaymentProduct()))")
    @Mapping(target = "payments", expression = "java(payment.getTargets().stream().map(t -> toSingleBulkPartTO(payment, t)).collect(java.util.stream.Collectors.toList()))")
    BulkPaymentTO toBulkPaymentTO(PaymentBO payment, PaymentTargetBO paymentTarget);

    @Mapping(source = "paymentTarget.paymentId", target = "paymentId")
    @Mapping(source = "payment.transactionStatus", target = "paymentStatus")
    @Mapping(target = "paymentProduct", expression = "java(toPaymentProductTO(payment.getPaymentProduct()))")
    SinglePaymentTO toSingleBulkPartTO(PaymentBO payment, PaymentTargetBO paymentTarget);

    default PaymentProductTO toPaymentProductTO(String paymentProduct) {
        return PaymentProductTO.getByValue(paymentProduct).get();
    }

    public abstract PaymentBO toPaymentBO(PaymentTO payment);

    @Mapping(target = "paymentType", expression = "java(PaymentTypeBO.SINGLE)")
    @Mapping(source = "paymentStatus", target = "transactionStatus")
    @Mapping(target = "paymentProduct", expression = "java(payment.getPaymentProduct().getValue())")
    @Mapping(target = "targets", expression = "java(java.util.Collections.singletonList(toPaymentTarget(payment)))")
    PaymentBO toPaymentBO(SinglePaymentTO payment);

    @Mapping(target = "paymentType", expression = "java(PaymentTypeBO.PERIODIC)")
    @Mapping(source = "paymentStatus", target = "transactionStatus")
    @Mapping(target = "paymentProduct", expression = "java(payment.getPaymentProduct().getValue())")
    @Mapping(target = "targets", expression = "java(java.util.Collections.singletonList(toPaymentTarget(payment)))")
    PaymentBO toPaymentBO(PeriodicPaymentTO payment);

    @Mapping(target = "paymentType", expression = "java(PaymentTypeBO.BULK)")
    @Mapping(target = "requestedExecutionTime", expression = "java(java.util.Optional.ofNullable(payment.getPayments()).map(l -> l.get(0).getRequestedExecutionTime()).orElse(null))")
    @Mapping(source = "paymentStatus", target = "transactionStatus")
    @Mapping(target = "paymentProduct", expression = "java(payment.getPaymentProduct().getValue())")
    @Mapping(target = "targets", source = "payment.payments")
    PaymentBO toPaymentBO(BulkPaymentTO payment);

    PaymentTargetBO toPaymentTarget(SinglePaymentTO payment);

    PaymentTargetBO toPaymentTarget(PeriodicPaymentTO payment);

    List<PaymentTargetBO> toPaymentTarget(List<SinglePaymentTO> payment);

    List<TransactionTO> toTransactionTOList(List<TransactionDetailsBO> transactions);

    @Mapping(source = "transactionAmount", target = "amount")
    TransactionTO toTransactionTO(TransactionDetailsBO transaction);

    @Mapping(target = "transactionAmount", source = "amount")
    TransactionDetailsBO toTransactionDetailsBO(TransactionTO transaction);

    default List<PaymentTO> toPaymentTOList(List<PaymentBO> payments) {
        return payments.stream().map(this::toAbstractPaymentTO).collect(Collectors.toList());
    }

    PaymentTO toAbstractPaymentTO(PaymentBO payment);
}
