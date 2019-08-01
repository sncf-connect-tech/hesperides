@require-real-ad
Feature: Get application directory groups

  Background:
    Given an authenticated prod user

  Scenario: retrieve directory groups associated with an application
    Given an application associated with the directory group ANOTHER_GROUP
    When I get the application detail
    Then the application details contains the directory group ANOTHER_GROUP