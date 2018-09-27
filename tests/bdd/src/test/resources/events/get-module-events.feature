Feature: Get module events

  Background:
    Given an authenticated user

  Scenario: get the events of a newly created module
    Given an existing module
    When I get the events of this module
    Then 1 event is returned
    And event at index 0 is a ModuleCreatedEvent event type

  Scenario: create a new module and add template and read it's events
    Given an existing module with a template
    When I get the events of this module
    Then 2 events are returned
    And event at index 0 is a TemplateCreatedEvent event type
    And event at index 1 is a ModuleCreatedEvent event type

  Scenario: get the events of a module that doesn't exist
    Given a module that doesn't exist
    When I get the events of this module
    Then 0 event is returned
