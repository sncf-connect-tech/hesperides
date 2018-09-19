Feature: Get technos detail

  Background:
    Given an authenticated user

  Scenario: get the detail of an existing techno
    Given an existing techno
    When I get the techno detail
    Then the techno detail is successfully retrieved

  Scenario: get the detail of a released techno
    Given a released techno
    When I get the techno detail
    Then the techno detail is successfully retrieved

  Scenario: get the detail of a techno that doesn't exist
    Given a techno that doesn't exist
    When I try to get the techno detail
    Then the techno is not found
