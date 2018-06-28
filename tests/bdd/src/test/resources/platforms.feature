Feature: platforms related features.

  Background:
    Given an authenticated user

  Scenario: create a platform
    Given a platform to create
    When creating this platform
    Then the platform is successfully created

  Scenario: create a faulty platform
    Given a platform to create, named "oops space"
    When creating this faulty platform
    Then a 400 error is returned, blaming "platform_name contains an invalid character"

#  Scenario: update a platform
#    Given an existing platform
#    When updating this platform
#    Then the platform is successfully updated

  Scenario: get a platform
    Given an existing platform
    When retrieving this platform
    Then the platform is successfully retrieved

  Scenario: get an application
    Given an existing platform
    When retrieving this platform's application
    Then the application is successfully retrieved

  Scenario: search for an existing application
    Given a list of applications
    When searching for one of those applications
    Then application found

  Scenario: search for existing application
    Given a list of applications
    When searching for some of those applications
    Then the number of application results is 2

  Scenario: search for an application that doesn't exist
    Given a list of applications
    When searching for an application that does not exist
    Then the number of application results is 0

  Scenario: delete a platform
    Given an existing platform
    When deleting this platform
    Then the platform is successfully deleted
