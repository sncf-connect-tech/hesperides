Feature: workshop property related features.

  Background:
    Given an authenticated user

  Scenario: create a workshop property
    Given a workshop property to create
    When creating this workshop property
    Then the workshop property is successfully created

  Scenario: update a workshop property
    Given an existing workshop property
    When updating this workshop property
    Then the workshop property is successfully updated

  Scenario: get a workshop property
    Given an existing workshop property
    When retrieving this workshop property
    Then the workshop property is successfully retrieved
