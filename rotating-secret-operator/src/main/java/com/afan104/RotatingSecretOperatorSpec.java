package com.afan104;

public class RotatingSecretOperatorSpec {

    private String targetSecretName;
    private int length;
    private int rotationIntervalSeconds;


    public String getSecretName() {
        return targetSecretName;
    }

    public void setSecretName(String targetSecretName) {
        this.targetSecretName = targetSecretName;
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
