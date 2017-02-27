package com.newnet.main;

import static io.restassured.RestAssured.given;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class VerbActions {

    public static Response post(String endpoint, String json) {
        return given()
            .body(json)
            .when()
            .post("/" + endpoint);
    }


    public static Response get(String endpoint) {

        //        given()
        //            .when()
        //            .get("/" + endpoint)
        //            .then()
        //            .log().body();

        return given()
            .when()
            .get("/" + endpoint);


    }

    public static Response delete(String endpoint) {
        return given()
            .when()
            .delete("/" + endpoint);
    }

    public static Response put(String endpoint, String json) {
        return given()
            .body(json)
            .when()
            .put("/" + endpoint);
    }

    public static Response put(String endpoint) {
        return given()
            .when()
            .put("/" + endpoint);
    }

    public static String getID(String endpoint, String by, String what) {

        //Map devicetypes = jp.get("find {e -> e.name =~ /HERB/}");
        String findBy = "find {e -> e." + by + "=~ /" + what + "/}";
        Response res = given()
            .when()
            .get("/" + endpoint);

        JsonPath jp = new JsonPath(res.asString());
        //Set a Root in the Json if needed
        //jp.setRoot("person");

        @SuppressWarnings("rawtypes")
        Map devicetypes = jp.get(findBy);

        return devicetypes.get("id").toString();
    }

    public static String getID(String endpoint, String root, String by, String what) {

        //Map devicetypes = jp.get("find {e -> e.name =~ /HERB/}");
        String findBy = "find {e -> e." + by + "=~ /" + what + "/}";
        Response res = given()
            .when()
            .get("/" + endpoint);

        JsonPath jp = new JsonPath(res.asString());
        //Set a Root in the Json if needed
        jp.setRoot(root);

        @SuppressWarnings("rawtypes")
        Map elements = jp.get(findBy);

        return elements.get("id").toString();
    }

    public static String getAPIKey(String webserver, String username) {

        String apiKey = null;
        //Need to lookup the apikey to issue API from this user
        try {
            String url = "jdbc:mysql://" + webserver + "/listen2";
            Connection conn = DriverManager.getConnection(url, "root", "");
            Statement stmt = conn.createStatement();
            ResultSet rs;

            rs = stmt.executeQuery("SELECT api_key FROM organization WHERE name = '" + username + "'");
            while (rs.next()) {

                apiKey = rs.getString("api_key");
                System.out.println("APIKEY = [" + apiKey + "]");
            }
            conn.close();
        } catch (Exception e) {
            System.err.println("Got an exception on getAPIKey! ");
            System.err.println(e.getMessage());
        }
        return apiKey;
    } //End getAPIKey

}
