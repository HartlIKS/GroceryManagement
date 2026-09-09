package de.iks.grocery_manager.server.testdata;

import de.iks.rtrm.ResourceInitializer;
import io.quarkus.arc.Arc;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.persistence.EntityManager;

import java.io.InputStream;

public class SqlInitializer implements ResourceInitializer {
    @Override
    public void initialize(Class<?> resource) {
        SqlResource sql = resource.getAnnotation(SqlResource.class);
        if(sql == null) return;
        var container = Arc.requireContainer();
        container
            .requestContext()
            .activate();
        EntityManager entityManager = container
            .instance(EntityManager.class)
            .get();
        QuarkusTransaction
            .joiningExisting()
            .call(() -> {
                try(InputStream script = resource.getResourceAsStream(sql.value())) {
                    assert script != null;
                    entityManager
                        .createNativeQuery(new String(script.readAllBytes()))
                        .executeUpdate();
                }
                return null;
            });
    }
}
