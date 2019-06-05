@require-real-ad
Feature: Get application prod groups

  Background:
    Given an authenticated lambda user

  Scenario: retrieve prod groups associated with an application
    Given an application with prod group A_GROUP
    When I get the application details
    Then A_GROUP is listed in the application authorities

  Scenario: retrieve the password count for all platforms of an application
    Given an existing module with a template and properties and password properties
    And an existing prod platform with this module
    When I get the application details requesting the passwords count
    Then the password count of the platform is greater than 1
