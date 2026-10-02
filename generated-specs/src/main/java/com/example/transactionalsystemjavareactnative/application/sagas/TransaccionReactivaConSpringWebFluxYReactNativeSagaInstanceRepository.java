package com.example.transactionalsystemjavareactnative.application.sagas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransaccionReactivaConSpringWebFluxYReactNativeSagaInstanceRepository extends JpaRepository<TransaccionReactivaConSpringWebFluxYReactNativeSagaInstance, UUID> {

    List<TransaccionReactivaConSpringWebFluxYReactNativeSagaInstance> findByCurrentState(TransaccionReactivaConSpringWebFluxYReactNativeSagaInstance.SagaState state);

    @Query("SELECT s FROM TransaccionReactivaConSpringWebFluxYReactNativeSagaInstance s WHERE s.currentState IN ('COMPENSATING', 'STARTED') AND s.retryCount < :maxRetries")
    List<TransaccionReactivaConSpringWebFluxYReactNativeSagaInstance> findRetryableSagas(int maxRetries);
}
