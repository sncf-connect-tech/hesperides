Feature: Release technos

  Regroup all use cases related to the release of technos

  Background:
    Given an authenticated user

  Scenario: release a techno
    Given an existing techno
    When releasing this techno
    Then the techno is successfully released
