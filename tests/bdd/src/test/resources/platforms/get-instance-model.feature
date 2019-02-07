Feature: Get instance model

  Background:
    Given an authenticated user

  Scenario: get a platform module instance model
    Given an existing module with properties
    And an existing platform with this module and an instance and instance properties
    When I get the instance model
    Then the instance model is successfully retrieved

  Scenario: get instance properties with an instance property name that is the same as a global property name
    Given an existing module with properties
    And an existing platform with this module and an instance
    And the platform has instance properties with the same name as a global property
    When I get the instance model
    Then the instance model is successfully retrieved

  #issue-489
  Scenario: get instance properties with an instance property name that is the same as another module property name
    Given an existing module with properties
    And an existing platform with this module and an instance
    And the platform has instance properties with the same name as another module property
    When I get the instance model
    Then the instance model is successfully retrieved

  #issue-489
  Scenario: get instance properties with an instance property name that is the same as the module property that it's declared in
    Given an existing module with properties
    And an existing platform with this module and an instance
    And the platform has instance properties with the same name as the module property that it's declared in
    When I get the instance model
    Then the instance model is successfully retrieved

  #issue-490
  Scenario: get multiple instance properties declared in the same property value
    Given an existing module with properties
    And an existing platform with this module and an instance
    And the platform has multiple instance properties declared in the same property value
    When I get the instance model
    Then the instance model is successfully retrieved

  Scenario: get one instance property when it's been declared more than once
    Given an existing module with properties
    And an existing platform with this module and an instance
    And the platform has an instance property declared twice
    When I get the instance model
    Then the instance model is successfully retrieved
