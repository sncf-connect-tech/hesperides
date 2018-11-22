Feature: Get instance or module files

  Background:
    Given an authenticated user

  Scenario: get files of an instance
    Given a techno template to create
    And an existing techno with this template
    And a module template to create
    And an existing module with this template and this techno
    And an existing platform with this module and an instance
    When I get the instance files
    Then the files are successfully retrieved

  Scenario: get files of a deployed module
    Given a techno template to create
    And an existing techno with this template
    And a module template to create
    And an existing module with this template and this techno
    And an existing platform with this module
    When I get the module files
    Then the files are successfully retrieved

  Scenario: get files of a deployed module that has multiple templates
    Given an existing module
    And a template to create with name "template-1" with filename "template-1.json" with location "/etc-1"
    And I add this template to the module
    And a template to create with name "template-2" with filename "template-2.json" with location "/etc-2"
    And I add this template to the module
    And an existing platform with this module
    When I get the module files
    Then the files are successfully retrieved

  Scenario: get files of an instance that doesn't exist
    Given an existing module
    And an existing platform with this module
    When I try to get the instance files
    Then the resource is not found

  Scenario: get files of a deployed module that doesn't exist
    And an existing platform
    When I try to get the module files
    Then the resource is not found
