Feature: Get technos detail

  Background:
    Given an authenticated user

  Scenario: get detail of an existing techno
    Given an existing techno
    When I get the techno detail
    Then the techno detail is successfully retrieved

  Scenario: try to get detail of a techno that doesn't exist
    Given a techno that doesn't exist
    When I try to get the techno detail
    Then the techno is not found and I get a not found error
