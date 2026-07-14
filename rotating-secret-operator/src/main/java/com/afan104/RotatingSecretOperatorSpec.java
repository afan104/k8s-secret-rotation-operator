package com.afan104;

public class RotatingSecretOperatorSpec {

    private String secretName;
    private int length;
    private int rotationIntervalSeconds;


    public String getSecretName() {
        return secretName;
    }

    public void setSecretName(String secretName) {
        this.secretName = secretName;
    }
    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
    public int getRotationIntervalSeconds() {
        return rotationIntervalSeconds;
    }

    public void setRotationIntervalSeconds(int rotationIntervalSeconds) {
        this.rotationIntervalSeconds = rotationIntervalSeconds;
    }
}
