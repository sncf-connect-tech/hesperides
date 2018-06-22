Feature: platforms related features.

  Background:
    Given an authenticated user

  Scenario: create a platform
    Given a platform to create
    When creating this platform
    Then the platform is successfully created

#  Scenario: update a platform
#    Given an existing platform
#    When updating this platform
#    Then the platform is successfully updated
#
#  Scenario: get a platform
#    Given an existing platform
#    When retrieving this platform
#    Then the platform is successfully retrieved
#
#  Scenario: get an application
#    Given an existing platform
#    When retrieving this platform's application
#    Then the application is successfully retrieved
#
#  Scenario: delete a platform
#    Given an existing platform
#    When deleting this platform
#    Then the platform is successfully deleted