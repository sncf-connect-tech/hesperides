Feature: Get application authorities

  Background:
    Given an authenticated prod user

  Scenario: retrieve authorities associated to an application
    Given an application with authorities
      | GG_XX |
      | GG_YY |
    When I get the application details
    Then the application details contains these authorities

  Scenario: retrieve the password count for all platforms of an application
    Given an existing module with a template and properties and password properties
    And an existing prod platform with this module
    When I get the application details requesting the passwords count
    Then the platform has at least 1 password
