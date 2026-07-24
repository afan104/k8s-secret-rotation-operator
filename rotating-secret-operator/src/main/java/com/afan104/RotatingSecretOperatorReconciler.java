package com.afan104;

import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.Context;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.time.Instant;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

public class RotatingSecretOperatorReconciler implements Reconciler<RotatingSecretOperatorCustomResource> {

    private final Counter rotationCounter;
    private final Gauge secondsSinceLastRotation;
    private final AtomicReference<Instant> lastRotatedAt;

    public RotatingSecretOperatorReconciler (MeterRegistry meterRegistry) {
        this.rotationCounter = meterRegistry.counter("rotation_count");
        this.lastRotatedAt = new AtomicReference<>(Instant.now());
        this.secondsSinceLastRotation = Gauge.builder("seconds_since_last_rotation", lastRotatedAt,
                ref -> Duration.between(ref.get(), Instant.now()).getSeconds())
            .register(meterRegistry);
    }

    public UpdateControl<RotatingSecretOperatorCustomResource> reconcile(RotatingSecretOperatorCustomResource primary,
                                                     Context<RotatingSecretOperatorCustomResource> context) {
        // grab specs for reaching k8s objs/updating obj values
        String targetSecretName=primary.getSpec().getSecretName();
        int rotationIntervalSeconds=primary.getSpec().getRotationIntervalSeconds();
        int length=primary.getSpec().getLength();

        // grab secret from k8s api
        Secret existing = context.getClient().secrets()
            .inNamespace(primary.getMetadata().getNamespace())
            .withName(targetSecretName)
            .get();

        // reconcile if mismatched
        if (existing == null
                || primary.getStatus() == null
                || primary.getStatus().getExpiresAt() == null
                || primary.getStatus().getExpiresAt().isBefore(Instant.now())) {
            if (primary.getStatus() == null) {
                primary.setStatus(new RotatingSecretOperatorStatus());
            }
            // rotate secret
            String secretValue = generateSecretValue(length);

            OwnerReference ownerRef = new OwnerReferenceBuilder()
                .withApiVersion(primary.getApiVersion())
                .withKind(primary.getKind())
                .withName(primary.getMetadata().getName())
                .withUid(primary.getMetadata().getUid())
                .withController(true)
                .build();

            Secret secret = new SecretBuilder()
                .withNewMetadata()
                    .withName(targetSecretName)
                    .withNamespace(primary.getMetadata().getNamespace())
                    .withOwnerReferences(ownerRef)
                .endMetadata()
                .withStringData(Map.of("SECRET_VALUE", secretValue))
                .build();

            context.getClient().resource(secret)
                .inNamespace(primary.getMetadata().getNamespace())
                .createOrReplace();
            
            // update MeterRegistry
            this.rotationCounter.increment();
            this.lastRotatedAt.set(Instant.now());


            // update status vals
            primary.getStatus().setLastRotatedAt(Instant.now());
            primary.getStatus().setExpiresAt(Instant.now().plusSeconds(rotationIntervalSeconds));

            int rotationCount=primary.getStatus().getRotationCount();
            primary.getStatus().setRotationCount(rotationCount+1);
            
            return UpdateControl.patchStatus(primary)
            .rescheduleAfter(Duration.ofSeconds(rotationIntervalSeconds));
        }

        else {
            return UpdateControl.<RotatingSecretOperatorCustomResource>noUpdate()
        .rescheduleAfter(Duration.between(Instant.now(), primary.getStatus().getExpiresAt()));
        }
    }

    // spec.length is the final credential string's character count, not the raw byte count.
    // Base64 encodes 3 bytes -> 4 chars, so we over-generate bytes then truncate to hit
    // the exact requested length instead of whatever the encoding happens to produce.
    private static String generateSecretValue(int length) {
        int byteLength = (int) Math.ceil(length * 3.0 / 4);
        byte[] secretBytes = new byte[byteLength];
        new SecureRandom().nextBytes(secretBytes);
        return Base64.getEncoder().withoutPadding().encodeToString(secretBytes).substring(0, length);
    }
}

// - Read `spec.targetSecretName`, `rotationIntervalSeconds`, `length`.
// - Fetch the target `Secret` via the fabric8 client. If it doesn't exist, or `status.expiresAt` is in the past, rotate: generate a new value with `SecureRandom`, create or update the `Secret` (with an owner reference back to the `RotatingSecret`, so deleting the CR cleans up the `Secret` too).
// - Update `status`: `lastRotatedAt = now`, `expiresAt = now + rotationIntervalSeconds`, `rotationCount += 1`.
// - Return `UpdateControl.patchStatus(resource).rescheduleAfter(...)`, requeuing for whenever the credential is next due to expire. If it isn't expired yet, skip rotation and just reschedule for the remaining time.
