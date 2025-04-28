Feature: Sample Api

  @suleyman
  Scenario: Get data from Api
    When Scenario Started "Get data from Api" - Browser Not Necessary
    And Get api response
    Then verify data
