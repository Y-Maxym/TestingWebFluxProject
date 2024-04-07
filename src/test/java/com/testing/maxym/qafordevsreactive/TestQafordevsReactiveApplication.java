package com.testing.maxym.qafordevsreactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestQafordevsReactiveApplication {

    public static void main(String[] args) {
        SpringApplication.from(QafordevsReactiveApplication::main).with(TestQafordevsReactiveApplication.class).run(args);
    }

}
