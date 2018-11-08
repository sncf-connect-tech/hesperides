Feature: Create techno templates

  Background:
    Given an authenticated user

  Scenario: add a template to an existing techno
    Given an existing techno
    And a template to create
    When I add this template to the techno
    Then the template is successfully added to the techno

  Scenario: add a template to a released techno
    Given a released techno
    And a template to create
    When I try to add this template to the techno
    Then the techno template creation is rejected with a method not allowed error

  Scenario: add a template to a techno that doesn't exist (it the same as creating a new techno)

  Scenario: add a template without a name to an existing techno
    Given an existing techno
    And a template to create without a name
    When I try to add this template to the techno
    Then the techno template creation is rejected with a bad request error

  Scenario: add a template without a filename to an existing techno
    Given an existing techno
    And a template to create without a filename
    When I try to add this template to the techno
    Then the techno template creation is rejected with a bad request error

  Scenario: add a template without a location to an existing techno
    Given an existing techno
    And a template to create without a location
    When I try to add this template to the techno
    Then the techno template creation is rejected with a bad request error

  Scenario: create a template after it has been deleted
    Given an existing techno
    When I delete this techno template
    And I add this template to the techno
    Then the template is successfully added to the techno

    Scenario: add a template that already exists
      Given an existing techno
      And a template to create with the same name as the existing one
      When I try to add this template to the techno
      Then the module template creation is rejected with a conflict error