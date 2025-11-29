package org.cucumber.step_definitions;

import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.cucumber.pages.TicketPage;
import org.cucumber.utilities.BrowserUtils;
import org.cucumber.utilities.ConfigurationReader;
import org.cucumber.utilities.Driver;

import org.cucumber.utilities.ExcelUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Ticket_Defs {
    @Given("Navigate to {string}")
    public void navigate_to(String environment) {
        System.out.println("environment = " + environment);

        String url = ConfigurationReader.get(environment);
        //WebDriver driver = Driver.get();
        Driver.get().get(url);

        BrowserUtils.waitFor(0.2);

        BrowserUtils.waitFor(5);
    }


    @And("pass human check if exists")
    public void passHumanCheckIfExists() {
        // check for human check and pass it if exists

        WebDriverWait shortWait = new WebDriverWait(Driver.get(), 3);

        // common slider/hold selectors to try
        List<By> candidates = Arrays.asList(
                By.cssSelector(".geetest_slider_button"),
                By.cssSelector(".geetest_slider"),
                By.cssSelector(".slider-button"),
                By.cssSelector(".rc-slider-handle"),
                By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'hold')]") ,
                By.xpath("//*[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'press and hold')]")
        );

        boolean solved = false;
        for (By candidate : candidates) {
            try {
                WebElement handle = shortWait.until(ExpectedConditions.elementToBeClickable(candidate));

                // try to locate a track/slider parent to compute distance
                WebElement track;
                try {
                    track = handle.findElement(By.xpath("ancestor::*[contains(@class,'slider') or contains(@class,'geetest') or contains(@class,'track')][1]"));
                } catch (Exception e) {
                    // fallback to parent element
                    try { track = handle.findElement(By.xpath("..")); } catch (Exception ex) { track = null; }
                }

                int moveX = 200; // default fallback
                if (track != null) {
                    int trackWidth = track.getSize().getWidth();
                    int handleWidth = handle.getSize().getWidth();
                    moveX = Math.max(50, trackWidth - handleWidth - 5);
                }

                Actions actions = new Actions(Driver.get());
                // click and hold, move in small steps to mimic a human
                actions.clickAndHold(handle).pause(Duration.ofMillis(300)).perform();

                int steps = Math.max(4, Math.min(12, moveX / 30));
                int stepX = moveX / steps;
                for (int i = 0; i < steps; i++) {
                    actions.moveByOffset(stepX, 0).pause(Duration.ofMillis(200)).perform();
                }

                // final small move and release
                actions.moveByOffset(moveX - stepX * steps, 0).pause(Duration.ofMillis(200)).release().perform();

                // give page time to validate
                BrowserUtils.waitFor(2);

                solved = true;
                break;
            } catch (Exception ignore) {
                // try next selector
            }
        }

        if (!solved) {
            System.out.println("No human-check detected or auto-solve failed");
        }

    }
    TicketPage ticketPage = new TicketPage();
    @Then("search for flights from {string} to {string}")
    public void searchForFlightsFromTo(String from, String to) {

        ticketPage.fromText.clear();
        ticketPage.fromText.sendKeys(from);
        BrowserUtils.waitFor(0.5);
        ticketPage.fromText.sendKeys(Keys.ARROW_DOWN);
        BrowserUtils.waitFor(1);
        ticketPage.fromText.sendKeys(Keys.ENTER);
        ticketPage.toText.clear();
        ticketPage.toText.sendKeys(to);
        BrowserUtils.waitFor(1);
        ticketPage.toText.sendKeys(Keys.ARROW_DOWN+ ""+Keys.ENTER);
        BrowserUtils.waitFor(4);
        ticketPage.searchFormSubmit.click();
        BrowserUtils.waitFor(20);

    }

    String dateStr;
    @And("select departure date as {string}")
    public void selectDepartureDateAs(String date) {
        Driver.get().get("https://www.ucuzabilet.com/dis-hat-arama-sonuc?from=ESB&to=DUS&toIsCity=1&ddate="+date+"&adult=1&directflightsonly=on&flightType=2");
//        Driver.get().get("https://www.ucuzabilet.com/dis-hat-arama-sonuc?from=ESB&to=DUS&toIsCity=1&ddate="+date+"&adult=1&flightType=2");

        dateStr = date;
        BrowserUtils.waitFor(2);
    }

    @Then("collect flight list")
    public void collectFlightList() {
        List<WebElement> flightList = ticketPage.flightItem;
        System.out.println("Total flights found: "+flightList.size());
//        int n = 1;
        for (int i = 0; i < flightList.size(); i++) {
            String id = "item-"+(i+1);
            System.out.println("id = " + id);
            WebElement itemLocater = Driver.get().findElement(By.id(id));
//            System.out.println("itemLocater = " + itemLocater);
//            System.out.println("itemLocater.isDisplayed() = " + itemLocater.isDisplayed());

            System.out.println("Rota = " + itemLocater.getAttribute("data-airports"));
            System.out.println("Fiyat = " + itemLocater.getAttribute("data-price")+" --> "+itemLocater.getAttribute("data-currency"));
//            String transactionAmount = Driver.get().findElement(By.cssSelector()
//            System.out.println(flight.getText());
            System.out.println("---------------------------------------------------");
//            n++;

        }
    }

    @And("select from {string} to {string} departure date as {string}")
    public void selectFromToDepartureDateAs(String from, String to, String dateStr) {
        Driver.get().get("https://www.ucuzabilet.com/dis-hat-arama-sonuc?from="+from+"&to="+to+"&toIsCity=1&ddate="+dateStr+"&adult=1&directflightsonly=on&flightType=2");
        BrowserUtils.waitFor(2);
    }

    List<Map<String, Object>> csvRecords = new ArrayList<>();
    @Then("read search data from csv {string}")
    public void readSearchDataFromCsv(String csvFile) {
        csvRecords = ExcelUtil.readCSVtoListofMapWithPath(csvFile);
        // print csvRecords
//        for (Map<String, Object> record : csvRecords) {
//            System.out.println("Record: " + record);
//        }
    }

    @And("search for each flight in flight list")
    public void searchForEachFlightInFlightList() {
        for (Map<String, Object> record : csvRecords) {
            String from = (String) record.get("from");
//            System.out.println("from = " + from);
            String to = (String) record.get("to");
//            System.out.println("to = " + to);
            String dateStr = (String) record.get("date");
            System.out.println("Record: " + record);
            Driver.get().get("https://www.ucuzabilet.com/dis-hat-arama-sonuc?from="+from+"&to="+to+"&toIsCity=1&ddate="+dateStr+"&adult=1&directflightsonly=on&flightType=2");
            BrowserUtils.waitFor(2);

            List<WebElement> flightList = ticketPage.flightItem;
            System.out.println("Total flights found: "+flightList.size());

            for (int i = 0; i < flightList.size(); i++) {
                String id = "item-"+(i+1);
                System.out.println("id = " + id);
                WebElement itemLocater = Driver.get().findElement(By.id(id));
//            System.out.println("itemLocater = " + itemLocater);
//            System.out.println("itemLocater.isDisplayed() = " + itemLocater.isDisplayed());

                System.out.println("Rota = " + itemLocater.getAttribute("data-airports"));
                System.out.println("Fiyat = " + itemLocater.getAttribute("data-price")+" --> "+itemLocater.getAttribute("data-currency"));

                System.out.println("---------------------------------------------------");


            }
        }
    }
}
