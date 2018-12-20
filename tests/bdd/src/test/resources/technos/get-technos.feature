Feature: Get technos details

  Background:
    Given an authenticated user

  Scenario: get the detail of an existing techno
    Given an existing techno
    When I get the techno detail
    Then the techno detail is successfully retrieved

  @integ-test-only
  Scenario: get the detail of an existing techno with the wrong letter case
    Given an existing techno
    When I get the techno detail with the wrong letter case
    Then the techno detail is successfully retrieved

  Scenario: get the detail of a released techno
    Given a released techno
    When I get the techno detail
    Then the techno detail is successfully retrieved

  Scenario: get the detail of the working copy of a techno that doesn't exist
    Given a techno that doesn't exist
    When I try to get the techno detail
    Then the resource is not found

  Scenario: get the detail of a released techno that only exist as working copy
    Given an existing techno
    When I try to get the techno detail for a techno type "release"
    Then the resource is not found

  Scenario: get the detail of a techno with an invalid techno type
    Given an existing techno
    When I try to get the techno detail for a techno type "unknown"
    Then the request is rejected with a bad request error
