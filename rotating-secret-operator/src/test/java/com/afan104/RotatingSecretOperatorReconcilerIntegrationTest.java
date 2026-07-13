package com.afan104;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.javaoperatorsdk.operator.junit.LocallyRunOperatorExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class RotatingSecretOperatorReconcilerIntegrationTest {

    public static final String RESOURCE_NAME = "test1";
    public static final String TARGET_SECRET_NAME = "test1-secret";
    public static final String SECRET_KEY = "SECRET_VALUE";
    public static final int ROTATION_INTERVAL_SECONDS = 5;
    public static final int SECRET_LENGTH = 16;

    @RegisterExtension
    LocallyRunOperatorExtension extension =
            LocallyRunOperatorExtension.builder()
                    .withReconciler(RotatingSecretOperatorReconciler.class)
                    .build();

    @Test
    void testCRUDOperations() {
        var cr = extension.create(testResource());

        // the operator should create the target Secret on the very first reconcile
        String[] firstValue = new String[1];
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            var secret = extension.get(Secret.class, TARGET_SECRET_NAME);
            assertThat(secret).isNotNull();
            assertThat(secret.getData()).containsKey(SECRET_KEY);
            firstValue[0] = secret.getData().get(SECRET_KEY);
            assertThat(firstValue[0]).isNotEmpty();
        });

        // once rotationIntervalSeconds passes, the operator should rotate on its own,
        // no changes made to the CR by this test, this is the self-requeue behavior
        await().atMost(Duration.ofSeconds(ROTATION_INTERVAL_SECONDS + 15)).untilAsserted(() -> {
            var secret = extension.get(Secret.class, TARGET_SECRET_NAME);
            assertThat(secret.getData().get(SECRET_KEY)).isNotEqualTo(firstValue[0]);

            var updatedCr = extension.get(RotatingSecretOperatorCustomResource.class, RESOURCE_NAME);
            assertThat(updatedCr.getStatus()).isNotNull();
            assertThat(updatedCr.getStatus().getRotationCount()).isGreaterThanOrEqualTo(2);
        });

        extension.delete(cr);

        // owner reference should cascade-delete the Secret once the CR is gone
        await().untilAsserted(() -> {
            var secret = extension.get(Secret.class, TARGET_SECRET_NAME);
            assertThat(secret).isNull();
        });
    }

    RotatingSecretOperatorCustomResource testResource() {
        var resource = new RotatingSecretOperatorCustomResource();
        resource.setMetadata(new ObjectMetaBuilder()
                .withName(RESOURCE_NAME)
                .build());
        resource.setSpec(new RotatingSecretOperatorSpec());
        resource.getSpec().setSecretName(TARGET_SECRET_NAME);
        resource.getSpec().setRotationIntervalSeconds(ROTATION_INTERVAL_SECONDS);
        resource.getSpec().setLength(SECRET_LENGTH);
        return resource;
    }
}
