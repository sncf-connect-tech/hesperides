Feature: Get techno model

  Background:
    Given an authenticated user

  Scenario: get the model of a techno with properties
    Given a techno template with properties
    When I get the model of this techno
    Then the model of this techno contains the properties

  Scenario: get the model of a techno that doesn't exist
    Given a techno that doesn't exist
    When I try to get the model of this techno
    Then the techno model if not found
