Feature: Delete techos templates

  Background:
    Given an authenticated user

  Scenario: delete an existing template in a techno
    Given an existing techno
    When I delete this techno template
    Then the techno template is successfully deleted

  Scenario: delete an existing template in a released techno
    Given a released techno
    When I try to delete this techno template
    Then the techno template delete is rejected with a method not allowed error

  Scenario: delete a template that doesn't exist in a techno
    Given an existing techno
    And a template that doesn't exist in this techno
    When I try to delete this techno template
    Then the techno template delete is rejected with a not found error

  Scenario: delete a template of a techno that doesn't exist
    Given a techno that doesn't exist
    When I try to delete this techno template
    Then the techno template delete is rejected with a not found error
