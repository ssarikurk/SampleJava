package org.cucumber.step_definitions;

import io.cucumber.java.en.And;
import io.restassured.path.json.JsonPath;
import org.cucumber.utilities.ApiUtils;

public class Api_Defs {
    @And("Get api response")
    public void getApiResponse() {
        JsonPath petJson = ApiUtils.getPetWithPetid(3);

        String petName = petJson.getString("category.name");
        System.out.println("petName = " + petName);


    }
}
