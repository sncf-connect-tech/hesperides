Feature: technos related features

  Regroup all uses cases releated to techno manipulations.

  Background:
    Given an authenticated user

  Scenario: create a new techno working copy
    Given a techno to create
    When creating a new techno
    Then the techno is successfully created