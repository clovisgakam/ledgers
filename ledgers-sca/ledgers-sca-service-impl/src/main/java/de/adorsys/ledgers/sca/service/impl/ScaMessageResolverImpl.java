package de.adorsys.ledgers.sca.service.impl;

import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.service.ScaMessageResolver;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ScaMessageResolverImpl implements ScaMessageResolver {
    @Value("${ledgers.sca.authCode.email.body}")
    private String authCodeEmailBody;
    @Value("${ledgers.sca.authCode.push.body:http://localhost:8083/tpp/push/tan}")
    private String authCodePushBody;

    @Override
    public String resolveMessage(AuthCodeDataBO data, String tan, ScaMethodTypeBO methodType) {
        String message;
        if (methodType == ScaMethodTypeBO.PUSH_OTP) {
            message = StringUtils.isBlank(authCodePushBody)
                              ? String.format(data.getUserMessage(), tan)
                              : String.format(authCodePushBody, data.getUserLogin(), data.getOpId(), tan);
        } else {
            String userMessageTemplate = StringUtils.isBlank(authCodeEmailBody)
                                                 ? data.getUserMessage()
                                                 : authCodeEmailBody;
            message = String.format(userMessageTemplate, tan);
        }
        return message;
    }
}
