Feature: Update techno templates

  Background:
    Given an authenticated user

  Scenario: update an existing template in a techno
    Given an existing techno
    And a template to update
    When I update this techno template
    Then the techno template is successfully updated

  Scenario: update an existing template in a released techno
    Given a released techno
    And a template to update
    When I try to update this techno template
    Then the techno template update is rejected with a method not allowed error

  Scenario: update a template that doesn't exist in a techno
    Given an existing techno
    And a template that doesn't exist in this techno
    When I try to update this techno template
    Then the techno template update is rejected with a not found error

  Scenario: update the wrong version of a template
    Given an existing techno
    And the template is outdated
    When I try to update this techno template
    Then the techno template update is rejected with a conflict error

  Scenario: update a template of a techno that doesn't exist
    Given a techno that doesn't exist
    And a template to update
    When I try to update this techno template
    Then the techno template update is rejected with a not found error

    #TODO Différencier outdated version et ?