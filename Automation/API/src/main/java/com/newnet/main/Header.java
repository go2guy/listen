package com.newnet.main;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.SSLConfig;
import io.restassured.specification.RequestSpecification;

public class Header {

    private final static String authToken = TestngContext.getParam("authToken");

    RequestSpecification requestSpecification = new RequestSpecBuilder().addHeader("Content-Type", "application/json")
        .addHeader("Accept", "application/json").addHeader("Authorization", authToken).build();

    public static void setHeader() {
        RestAssured.baseURI = TestngContext.getParam("baseURI");
        RestAssured.basePath = TestngContext.getParam("basePath");
        RestAssured.config = RestAssured.config().sslConfig(new SSLConfig().allowAllHostnames());
        RestAssured.requestSpecification = new RequestSpecBuilder().addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json").addHeader("Authorization", authToken).build();
    }

}
