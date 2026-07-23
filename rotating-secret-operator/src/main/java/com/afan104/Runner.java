package com.afan104;

import io.javaoperatorsdk.operator.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;


public class Runner {

    private static final Logger log = LoggerFactory.getLogger(Runner.class);

    public static void main(String[] args) {
        SpringApplication.run(ActuatorServiceApplication.class, args);
        Operator operator = new Operator();
        operator.register(new RotatingSecretOperatorReconciler(),
                override -> override.settingNamespace("default"));
        operator.start();
        log.info("Operator started.");
    }
}
