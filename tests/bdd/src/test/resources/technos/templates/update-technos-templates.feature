Feature: Update technos templates

  Regroup all use cases related to the update of technos templates

  Background:
    Given an authenticated user

  Scenario: update an existing template in a techno
    Given an existing techno
    When updating the template in this techno
    Then the template in this techno is updated
