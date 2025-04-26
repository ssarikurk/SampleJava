package org.cucumber.step_definitions;

import org.cucumber.utilities.*;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import io.appium.java_client.windows.WindowsDriver;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.restassured.path.json.JsonPath;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Assert;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class Hooks implements EventListener {

    @Before (value = "@webApp", order = 1)
    public void setUp(Scenario scenario){

        System.out.println("----------------------------------------------------------------------\nScenario Started = " + scenario.getName()+"\n----------------------------------------------------------------------");
        Driver.get().manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
        Driver.get().manage().window().maximize();
       // BrowserUtils.waitFor(5);
    }

    @After (value = "@webApp", order = 0)
    public void tearDown(Scenario scenario){
        if(scenario.isFailed()){
            final byte[] screenshot = ((TakesScreenshot) Driver.get()).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot,"image/png","screenshot");
            System.out.println("***FAILED*** \n----------------------------------------------------------------------");
            System.out.println(scenario.getName()+" ----> "+scenario.getStatus());
        }else {
            System.out.println("***PASSED*** \n----------------------------------------------------------------------");
            System.out.println(scenario.getName()+" ----> "+scenario.getStatus());
        }

        Driver.closeDriver();

    }

}