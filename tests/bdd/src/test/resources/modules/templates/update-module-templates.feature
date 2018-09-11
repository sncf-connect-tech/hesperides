Feature: Module template updates

  Regroup all uses cases releated to the update of module templates.

  Background:
    Given an authenticated user

  Scenario: update an existing template from a module
    Given an existing module
    And an existing template in this module
    When updating this template
    Then the template is successfully updated

  Scenario: trying to update a template twice at the same time
    Given an existing module
    And an existing template in this module
    When updating this template
    And updating the same template at the same time
    Then the template update is rejected