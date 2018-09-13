Feature: Get technos

  Regroup all use cases related to the retrieval of technos and their information

  Background:
    Given an authenticated user

  Scenario: get info for a given techno
    Given an existing techno
    When retrieving the techno's info
    Then the techno's info is retrieved
