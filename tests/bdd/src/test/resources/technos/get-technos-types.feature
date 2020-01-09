Feature: Get technos type

  Background:
    Given an authenticated user

  Scenario: get the types of a released techno
    Given an existing released techno
    When I get the techno types
    Then a list containing workingcopy and release is returned

  Scenario: get the types of a techno that is not released
    Given an existing techno
    When I get the techno types
    Then a list containing workingcopy is returned

  Scenario: get the types of a techno that doesn't exist
    Given a techno that doesn't exist
    When I get the techno types
    Then an empty list is returned