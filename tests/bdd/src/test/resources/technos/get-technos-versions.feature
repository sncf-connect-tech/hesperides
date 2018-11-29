Feature: Get technos versions

  Background:
    Given an authenticated user

  Scenario: get the list of versions of an existing techno
    Given a techno with 4 versions
    When I get the techno versions
    Then a list of 4 elements is returned

  Scenario: get the list of versions of a techno that doesn't exist
    Given a techno that doesn't exist
    When I get the techno versions
    Then a list of 0 elements is returned