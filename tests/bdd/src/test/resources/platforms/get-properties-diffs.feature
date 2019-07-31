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
    When I get the final properties diff of this module between platforms "P1" and "P2"
    Then the diff is successfully retrieved
    And the resulting diff match these values
      | only_left  | property-only-in-p1     |
      | only_right | property-only-in-p2     |
      | common     | property-Y-in-p1-and-p2 |
      | differing  | property-X-in-p1-and-p2 |

  Scenario: get stored properties diffs between two platforms
    Given an existing module
    And an existing platform named "P1" with this module
    And the platform "P1" has these valued properties
      | name       | value          |
      | property-a | {{property-b}} |
      | property-b | p1             |
    And an existing platform named "P2" with this module
    And the platform "P2" has these valued properties
      | name       | value          |
      | property-a | {{property-b}} |
      | property-b | p2             |
    When I get the stored properties diff of this module between platforms "P1" and "P2"
    Then the diff is successfully retrieved
    And the resulting diff match these values
      | only_left  |            |
      | only_right |            |
      | common     | property-a |
      | differing  | property-b |

  Scenario: get properties diffs between two platforms with default valued properties
    Given an existing module with this template content
    """
    {{ simple-property | @default 42}}
    """
    And an existing platform named "P1" with this module
    And the platform "P1" has these valued properties
      | name            | value |
      | simple-property |       |
    And an existing platform named "P2" with this module
    And the platform "P2" has these valued properties
      | name            | value |
      | simple-property | 42    |
    When I get the final properties diff of this module between platforms "P1" and "P2"
    Then the diff is successfully retrieved
    And the resulting diff match these values
      | only_left  |                 |
      | only_right |                 |
      | common     | simple-property |
      | differing  |                 |

  Scenario: get stored properties diffs between two platforms with default valued properties
    Given an existing module with this template content
    """
    {{ simple-property | @default 42}}
    """
    And an existing platform named "P1" with this module
    And the platform "P1" has these valued properties
      | name            | value |
      | simple-property |       |
    And an existing platform named "P2" with this module
    And the platform "P2" has these valued properties
      | name            | value |
      | simple-property | 42    |
    When I get the stored properties diff of this module between platforms "P1" and "P2"
    Then the diff is successfully retrieved
    And the resulting diff match these values
      | only_left  |                 |
      | only_right |                 |
      | common     |                 |
      | differing  | simple-property |

  Scenario: get globals properties diff between two platforms
    Given an existing platform named "P1"
    And the platform "P1" has these global properties
      | name                    | value         |
      | property-X-in-p1-and-p2 | a-value       |
      | property-Y-in-p1-and-p2 | another-value |
      | property-only-in-p1     | p1-value      |
    And an existing platform named "P2"
    And the platform "P2" has these global properties
      | name                    | value             |
      | property-X-in-p1-and-p2 | a-differing-value |
      | property-Y-in-p1-and-p2 | another-value     |
      | property-only-in-p2     | p2-value          |
    When I get the final global properties diff between platforms "P1" and "P2"
    Then the diff is successfully retrieved
    And the resulting diff match these values
      | only_left  | property-only-in-p1     |
      | only_right | property-only-in-p2     |
      | common     | property-Y-in-p1-and-p2 |
      | differing  | property-X-in-p1-and-p2 |

  Scenario: get stored global properties diffs between two platforms
    Given an existing platform named "P1"
    And the platform "P1" has these global properties
      | name       | value          |
      | property-a | {{property-b}} |
      | property-b | p1             |
    And an existing platform named "P2"
    And the platform "P2" has these global properties
      | name       | value          |
      | property-a | {{property-b}} |
      | property-b | p2             |
    When I get the stored global properties diff between platforms "P1" and "P2"
    Then the diff is successfully retrieved
    And the resulting diff match these values
      | only_left  |            |
      | only_right |            |
      | common     | property-a |
      | differing  | property-b |


  Scenario: get iterables properties diff between two platforms

  Scenario: get stored iterables properties diffs between two platforms

  Scenario: get properties diff on the same platform at a different timestamp

  Scenario: get properties diff between two platforms at a different timestamp

  Scenario: get globals properties diff on the same platform at a different timestamp

  Scenario: get globals properties diff between two platforms at a different timestamp

  Scenario: get iterables properties diff on the same platform at a different timestamp

  Scenario: get iterables properties diff between two platforms at a different timestamp
