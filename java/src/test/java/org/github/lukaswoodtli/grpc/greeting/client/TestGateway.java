package org.github.lukaswoodtli.grpc.greeting.client;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;

public class TestGateway {

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8081;
    }

    @Test
    public void getJson() {

        get("/v1/greet")
                .then()
                .body("result", equalTo("Hello  form C++"));
    }
}

