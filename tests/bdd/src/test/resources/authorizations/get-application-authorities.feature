Feature: Get application authorities

  Background:
    Given an authenticated prod user

  Scenario: retrieve authority groups associated with an application
    Given an application associated with the following authority groups
      | GG_XX |
      | GG_YY |
    When I get the application details
    Then the application details contains these authority groups

  Scenario: retrieve the password count for all platforms of an application
    Given an existing module with a template and properties and password properties
    And an existing prod platform with this module
    When I get the application details requesting the passwords count
    Then the platform has at least 1 password
