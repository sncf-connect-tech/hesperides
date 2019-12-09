Feature: key value related features

  Background:
    Given an authenticated user

  Scenario: create a key value
    Given a key value to create
    When creating this key value
    Then the key value is successfully created

  Scenario: get a key value
    Given an existing key value
    When I get this this key value
    Then the key value is successfully retrieved

  Scenario: update a key value
    Given an existing key value
    When I update this key value
    Then the key value is successfully updated
