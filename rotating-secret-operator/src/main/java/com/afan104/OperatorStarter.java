package com.afan104;

import io.javaoperatorsdk.operator.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class OperatorStarter implements CommandLineRunner{
    private final MeterRegistry meterRegistry;
    
    // constructor, when springbootapplication instantiates this method it will inject this in
    public OperatorStarter(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
    }

    private static final Logger log = LoggerFactory.getLogger(OperatorStarter.class);

    @Override
    public void run(String... args) {
        Operator operator = new Operator();
        operator.register(new RotatingSecretOperatorReconciler(this.meterRegistry),
                override -> override.settingNamespace("default"));
        operator.start();
        log.info("Operator started.");
    }
}