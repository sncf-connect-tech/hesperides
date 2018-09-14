Feature: Events related features

  Regroup all uses cases related to module events queries.

  Background:
    Given an authenticated user

  Scenario: create a new module and read it's events
    Given a module to create
    When creating a new module
    And get events occurred for the module created
    Then 1 event is returned
    And event at index 0 is a ModuleCreatedEvent event type

  Scenario: create a new module and add template and read it's events
    Given a module to create
    When creating a new module
    And adding a new template to this module
    And get events occurred for the module created
    Then 2 events are returned
    And event at index 0 is a TemplateCreatedEvent event type
    And event at index 1 is a ModuleCreatedEvent event type
