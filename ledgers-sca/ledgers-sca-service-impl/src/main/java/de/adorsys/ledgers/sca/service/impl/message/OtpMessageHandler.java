package de.adorsys.ledgers.sca.service.impl.message;

import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.sca.message.ScaMessage;
import de.adorsys.ledgers.sca.service.SCAMethodType;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;

public interface OtpMessageHandler extends SCAMethodType {

    <T extends ScaMessage> T getMessage(AuthCodeDataBO data, ScaUserDataBO scaData, String tan);
}
