Feature: Get file

  Background:
    Given an authenticated user

  Scenario: get valued template file with module properties
    Given an existing module with a template with properties
    And an existing platform with this module and valued properties
    When I get the module template file
    Then the file is successfully retrieved
    And the content type is "text/plain" with UTF-8 encoding

  Scenario: get valued template file with instance proprerties
    Given an existing module with a template with properties
    And an existing platform with this module with an instance and valued instance properties
    When I get the instance template file
    Then the file is successfully retrieved
    And the content type is "text/plain" with UTF-8 encoding

  Scenario: get valued template file of a deployed module within a platform that doesn't exist
    Given an existing module with a template with properties
    When I try to get the module template file
    Then the resource is not found

  Scenario: get valued template file of a deployed module that doesn't exist
    Given an existing platform
    When I try to get the module template file
    Then the resource is not found






