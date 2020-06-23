package de.adorsys.ledgers.middleware.api.domain.sca;

import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScaResponse {
	private String parentId;
	private AuthorisationType authorisationType;
	private boolean partiallyAuthorised;
	private ScaStatusTO scaStatus;
	private String authorisationId;
	private List<ScaUserDataTO> scaMethods;
	private ScaUserDataTO chosenScaMethod;
	private ChallengeDataTO challengeData;
	private String psuMessage;
	private LocalDateTime statusDate;
	private int expiresInSeconds;
	private boolean multilevelScaRequired;
	private String authConfirmationCode;
	private BearerTokenTO bearerToken;
}
