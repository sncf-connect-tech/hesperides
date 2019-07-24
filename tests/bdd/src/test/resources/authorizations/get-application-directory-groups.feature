@require-real-ad
Feature: Get application directory groups

  Background:
    Given an authenticated prod user

  Scenario: retrieve directory groups associated with an application
    Given an application associated with the directory group A_PROD_GROUP
    When I get the application detail
    Then the application details contains the directory group A_PROD_GROUP

  Scenario: retrieve the password count for all platforms of an application
    Given an existing module with a template and properties and password properties
    And an existing prod platform with this module
    When I get the application detail requesting the passwords count
    Then the platform has at least 1 password