package com.afan104;
import java.time.Instant;

public class RotatingSecretOperatorStatus {
    private Instant lastRotatedAt;
    private Instant expiresAt;
    private int rotationCount;

    public Instant getLastRotatedAt() {return this.lastRotatedAt; }
    public Instant getExpiresAt() {return this.expiresAt; }
    public int getRotationCount() {return this.rotationCount;}

    // after rotating
    public void setRotationCount(int count) {this.rotationCount=count; }
    public void setLastRotatedAt(Instant time) {this.lastRotatedAt=time;}
    public void setExpiresAt(Instant time) {this.expiresAt=time;}
}