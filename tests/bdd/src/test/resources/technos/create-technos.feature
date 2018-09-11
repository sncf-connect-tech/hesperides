Feature: Techno creations

  Regroup all use cases related to the creation of technos

  Background:
    Given an authenticated user

  Scenario: create a new techno (it's the same as adding a template to a techno that does not exist)
    Given a techno to create
    When creating a new techno
    Then the techno is successfully created

  Scenario: copy a techno
    Given an existing techno
    And a template in this techno that has properties
    When creating a copy of this techno
    Then the techno is successfully and completely duplicated
    And the model of the techno is also duplicated
