Feature: Search technos

  Background:
    Given an authenticated user

  Scenario: search for an existing techno
    Given a list of 12 technos
    When I search for one specific techno
    Then the techno is found

  Scenario: search for existing technos
    Given a list of 12 technos
    When I search for some of those technos
    Then the list of techno results is limited to 10 items

  Scenario: search for a techno that does not exist
    Given a list of 12 technos
    When I search for a techno that does not exist
    Then an empty list is returned

  Scenario: search for a limited number of existing technos
    Given a list of 12 technos
    When I search for some of those technos, limiting the number of results to 100
    Then the list of techno results is limited to 12 items