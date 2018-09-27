Feature: Search technos

  Background:
    Given an authenticated user

  Scenario: search for an existing techno
    Given a list of 12 technos
    When I search for one specific techno
    Then the techno is found

  Scenario: search for existing technos
    Given a list of 42 technos
    When I search for some of those technos
    Then the list of techno results is limited to 10 items

  Scenario: search for a techno that does not exist
    Given a list of 12 technos
    When I search for a techno that does not exist
    Then the list of techno results is empty
