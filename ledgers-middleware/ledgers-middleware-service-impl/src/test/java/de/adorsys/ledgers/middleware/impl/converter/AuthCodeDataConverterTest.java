package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.middleware.api.domain.sca.AuthCodeDataTO;
import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

class AuthCodeDataConverterTest {

    @Test
    void toAuthCodeDataBO() {
        // Given
        AuthCodeDataConverter mapper = Mappers.getMapper(AuthCodeDataConverter.class);

        AuthCodeDataTO to = readYml();

        // When
        AuthCodeDataBO bo = mapper.toAuthCodeDataBO(to);

        // Then
        assertThat(bo.getOpId(), is(to.getOpId()));
        assertThat(bo.getUserLogin(), is(to.getUserLogin()));
        assertThat(bo.getOpData(), is(to.getOpData()));
        assertThat(bo.getScaUserDataId(), is(to.getScaUserDataId()));
    }

    private <T> T readYml() {
        try {
            return YamlReader.getInstance().getObjectFromInputStream(getClass().getResourceAsStream("auth-code-data.yml"), (Class<T>) AuthCodeDataTO.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Resource file not found", e);
        }
    }
}