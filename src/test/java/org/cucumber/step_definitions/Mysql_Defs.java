package org.cucumber.step_definitions;

import io.cucumber.java.en.Given;
import org.cucumber.utilities.ConfigurationReader;
import org.cucumber.utilities.SqlUtils;

public class Mysql_Defs {
    @Given("Get data from {string} db")
    public void getDataFromDb(String mysqlDbName) {

        SqlUtils.createConnection(ConfigurationReader.get("MySQLIp"), mysqlDbName, "root", "retrace123");




    }



}
