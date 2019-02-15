Feature: Get modules using techno

  Background:
    Given an authenticated user

  Scenario: get modules using techno
    Given an existing techno
    And an existing module named "M1" with this techno
    And an existing module named "M2" with this techno
    When I get the modules using this techno
    Then the modules using this techno are successfully retrieved
