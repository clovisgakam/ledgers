package de.adorsys.ledgers.um.db.repository;

import de.adorsys.ledgers.um.db.domain.ScaUserDataEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ScaUserDataRepository extends CrudRepository<ScaUserDataEntity, String> {

    List<ScaUserDataEntity> findByMethodValue(String email);

}
