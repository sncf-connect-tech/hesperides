Feature: Delete module templates

  Background:
    Given an authenticated user

  Scenario: delete an existing template in a module
    Given an existing module with a template
    When I delete this module template
    Then the module template is successfully deleted

  Scenario: delete an existing template in a released module
    Given an existing released module with a template
    When I try to delete this module template
    Then the module template delete is rejected with a method not allowed error

  Scenario: delete a template that doesn't exist in a module
    Given an existing module
    And a template that doesn't exist in this module
    When I try to delete this module template
    Then the module template delete is rejected with a not found error

  Scenario: delete a template of a module that doesn't exist
    Given a module that doesn't exist
    When I try to delete this module template
    Then the module template delete is rejected with a not found error

  Scenario: delete a template with a slash within the title
    Given a template named "conf/domains.json"
    And an existing module with this template
    When I delete this module template
    Then the module template is successfully deleted

  Scenario: delete a template with a url-encoded slash within the title
    Given a template named "conf/domains.json"
    And an existing module with this template
    When I delete this module template using an url-encoded template name
    Then the module template is successfully deleted
