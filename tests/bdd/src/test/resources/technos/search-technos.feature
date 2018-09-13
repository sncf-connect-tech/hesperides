Feature: Search technos

  Regroup all use cases related to the search of technos

  Background:
    Given an authenticated user

  Scenario: search for an existing techno
    Given a list of 12 technos
    When searching for a specific techno
    Then the techno is found

  Scenario: search for existing technos
    Given a list of 12 technos
    When searching for some of those technos
    Then the number of techno results is 10

  Scenario: search for a techno that does not exist
    Given a list of 12 technos
    When searching for a techno that does not exist
    Then the number of techno results is 0
