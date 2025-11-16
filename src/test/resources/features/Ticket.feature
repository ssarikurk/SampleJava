@webApp
Feature: Skyscanner Feature

  @sul
  Scenario: Skyscanner Ticket Search Tests
    When Scenario Started "Skyscanner Ticket Search"
    Given Navigate to "skyscanner"
    #And pass human check if exists
    Then search for flights from "Esenboğa" to "Dusseldorf"
    And select departure date as "2025-12-20"



