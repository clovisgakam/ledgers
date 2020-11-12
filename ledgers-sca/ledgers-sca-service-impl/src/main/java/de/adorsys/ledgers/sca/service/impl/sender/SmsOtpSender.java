package de.adorsys.ledgers.sca.service.impl.sender;

import de.adorsys.ledgers.sca.domain.sca.message.MailScaMessage;
import de.adorsys.ledgers.sca.domain.sca.message.ScaMessage;
import de.adorsys.ledgers.sca.service.SCASender;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO.SMS_OTP;

@Service
@RequiredArgsConstructor
public class SmsOtpSender implements SCASender {
    private final EmailSender emailSender;

    @Override
    public <T extends ScaMessage> boolean send(T message) {
        MailScaMessage scaMessage = (MailScaMessage) message;
        return emailSender.send(scaMessage);
    }

    @Override
    public ScaMethodTypeBO getType() {
        return SMS_OTP;
    }
}
