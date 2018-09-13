Feature: Delete techos templates

  Regroup all use cases related to the deletion of technos template

  Background:
    Given an authenticated user

  Scenario: delete an existing template in a techno
    Given an existing techno
    When deleting the template in this techno
    Then the template in this techno is successfully deleted
