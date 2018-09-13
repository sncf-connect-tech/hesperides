Feature: Create technos

  Regroup all use cases related to the creation of technos

  Background:
    Given an authenticated user

  Scenario: create a new techno (it's the same as adding a template to a techno that does not exist)
    Given a techno to create
    When creating a new techno
    Then the techno is successfully created
