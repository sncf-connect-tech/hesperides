Feature: Search technos

  Background:
    Given an authenticated user

  Scenario: search for an existing techno
    Given a list of 12 technos
    When I search for one specific techno
    Then the techno is found

  Scenario: search for existing technos
    Given a list of 12 technos
    When I search for some of these technos
    Then the list of techno results is limited to 10 items

  Scenario: search for a techno that does not exist
    Given a list of 12 technos
    When I search for a techno that does not exist
    Then an empty list is returned

  Scenario: search for a limited number of existing technos
    Given a list of 12 technos
    When I search for some of these technos, limiting the number of results to 100
    Then the list of techno results is limited to 12 items

  #issue-863
  Scenario: search for an existing techno using the wrong case
    Given an existing techno named "aTechno"
    When I search for the techno named "ATECHNO"
    Then the techno is found

  #issue-863
  Scenario: search for an existing techno using the wrong case again
    Given an existing techno named "aTechno"
    When I search for the techno named "atechno"
    Then the techno is found
