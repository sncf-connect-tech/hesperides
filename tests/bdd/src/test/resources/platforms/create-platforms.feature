Feature: Platform creation.

  Background:
    Given an authenticated user

  Scenario: create a platform
    Given a platform to create
    When creating this platform
    Then the platform is successfully created

  Scenario: create a faulty platform
    Given a platform to create, named "oops space"
    When creating this faulty platform
    Then a 400 error is returned, blaming "platform_name contains an invalid character"
