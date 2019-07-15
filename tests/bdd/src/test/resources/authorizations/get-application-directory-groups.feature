@require-real-ad
Feature: Get application directory groups

  Background:
    Given an authenticated prod user

  Scenario: retrieve directory groups associated with an application
    Given an application associated with the following directory groups
      | GG_XX |
      | GG_YY |
    When I get the application detail
    Then the application details contains these directory groups

  Scenario: retrieve the password count for all platforms of an application
    Given an existing module with a template and properties and password properties
    And an existing prod platform with this module
    When I get the application detail requesting the passwords count
    Then the platform has at least 1 password
