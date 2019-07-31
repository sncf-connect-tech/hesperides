Feature: Get properties diffs

  Background:
    Given an authenticated user

  Scenario: get properties diffs between two platforms with valued properties
    Given an existing module with this template content
      """
      {{property-X-in-p1-and-p2}}
      {{property-Y-in-p1-and-p2}}
      {{property-only-in-p1}}
      {{property-only-in-p2}}
      """
    And an existing platform named "P1" with this module
    And the platform "P1" has these valued properties
      | name                    | value         |
      | property-X-in-p1-and-p2 | a-value       |
      | property-Y-in-p1-and-p2 | another-value |
      | property-only-in-p1     | p1-value      |
    And an existing platform named "P2" with this module
    And the platform "P2" has these valued properties
      | name                    | value             |
      | property-X-in-p1-and-p2 | a-differing-value |
      | property-Y-in-p1-and-p2 | another-value     |
      | property-only-in-p2     | p2-value          |
    When I get the properties diff of this module between platforms "P1" and "P2"
    Then the diff is successfully retrieved
    And the resulting diff match these values
      | only_left  | property-only-in-p1     |
      | only_right | property-only-in-p2     |
      | common     | property-Y-in-p1-and-p2 |
      | differing  | property-X-in-p1-and-p2 |
