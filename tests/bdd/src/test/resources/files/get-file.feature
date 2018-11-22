Feature: Get file

  Background:
    Given an authenticated user

  Scenario: get file
    Given an existing module with a template with properties
    And an existing platform with this module and valued properties
    When I get the module template file
    Then the file is successfully retrieved
    And the content type is "text/plain" with UTF-8 encoding