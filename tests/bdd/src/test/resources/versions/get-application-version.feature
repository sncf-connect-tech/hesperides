Feature: Get application version

  Background:
    Given an authenticated user

  Scenario: get backend and api version
    When I get the application versions
    Then the versions are returned