Feature: Techno retrieval

  Regroup all use cases related to the retrieval of technos and their information

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

  Scenario: get info for a given techno
    Given an existing techno
    When retrieving the techno's info
    Then the techno's info is retrieved
