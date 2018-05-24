Feature: technos related features

  Regroup all use cases related to technos

  Background:
    Given an authenticated user

  Scenario: create a new techno (it's the same as adding a template to a techno that does not exist)
    Given a techno to create
    When creating a new techno
    Then the techno is successfully created

  Scenario: delete a techno
    Given an existing techno
    When deleting this techno
    Then the techno is successfully deleted

  Scenario: release a techno
    Given an existing techno
    When releasing this techno
    Then the techno is successfully released

  Scenario: delete a released techno
    Given an existing techno
    When releasing this techno
    And deleting this techno
    Then the techno is successfully deleted

  Scenario: copy a techno

  Scenario: retrieve all templates

  Scenario: retrieve a template

  Scenario: add a template to an existing techno

  Scenario: update an existing template in a techno

  Scenario: delete a techno template

  Scenario: search for existing technos

  Scenario: search for an existing techno
    Given a list of existing technos
    When searching for a specific techno
    Then the techno is found

  Scenario: search for existing technos
    Given a list of existing technos
    When searching for some of those technos
    Then the number of techno results is limited

  Scenario: search for a techno that does not exist
    Given a list of existing technos
    When searching for a techno that does not exist
    Then the techno results is empty