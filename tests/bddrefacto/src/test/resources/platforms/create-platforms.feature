Feature: Create platform

  Background:
    Given an authenticated user

  Scenario: create a platform
    Given a platform to create
    When I create this platform
    Then the platform is successfully created

  Scenario: create a faulty platform
    Given a platform to create, named "oops space"
    When I try to create this platform
    Then a 400 error is returned, blaming "platform_name contains an invalid character"
