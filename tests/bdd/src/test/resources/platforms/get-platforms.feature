Feature: Get platforms

  Background:
    Given an authenticated user

  Scenario: get the detail of an existing platform
    Given an existing module
    Given an existing platform using this module
    When I get the platform detail
    Then the platform detail is successfully retrieved

  Scenario: get a platform that doesn't exist
    Given a platform that doesn't exist
    When I try to get the platform detail
    Then the platform is not found

#  Scenario: search for an existing platform with an application name and a platform name
#    Given a list of 25 platforms
#    When searching for one of them giving an application name and a platform name
#    Then the platform is found
#
#  Scenario: search all platforms from an application
#    Given a list of 25 platforms
#    When asking for the platform list of an application
#    Then platform list is established for the targeted application
