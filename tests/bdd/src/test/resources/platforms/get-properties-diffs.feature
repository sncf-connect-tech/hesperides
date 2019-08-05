Feature: Get properties diffs

  Background:
    Given an authenticated user

  Scenario: get properties diffs between two platforms
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
    When I get the properties diff on final values between platforms "P1" and "P2"
    Then the diff is successfully retrieved
    And the resulting diff match these values
      | only_left           | only_right          | common                  | differing               |
      | property-only-in-p1 | property-only-in-p2 | property-Y-in-p1-and-p2 | property-X-in-p1-and-p2 |

  Scenario: get properties diff on stored values between two platforms
    Given an existing module with this template content
      """
      {{property-a}}
      """
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
    When I get the properties diff on stored values between platforms "P1" and "P2"
    Then the diff is successfully retrieved
    And the resulting diff match these values
      | only_left | only_right | common     | differing  |
      |           |            | property-a | property-b |

  Scenario: get properties diff on final values between two platforms
    Given an existing module with this template content
      """
      {{property-a}}
      """
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
    When I get the properties diff on final values between platforms "P1" and "P2"
    Then the diff is successfully retrieved
    And the resulting diff match these values
      | only_left | only_right | common | differing  |
      |           |            |        | property-a |
      |           |            |        | property-b |

  Scenario: get properties diff on final values between two platforms with default valued properties
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
    When I get the properties diff on final values between platforms "P1" and "P2"
    Then the diff is successfully retrieved
    And the resulting diff match these values
      | only_left | only_right | common          | differing |
      |           |            | simple-property |           |

  Scenario: get properties diff on stored values between two platforms with default valued properties
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
    When I get the properties diff on stored values between platforms "P1" and "P2"
    Then the diff is successfully retrieved
    And the resulting diff match these values
      | only_left | only_right      | common | differing |
      |           | simple-property |        |           |

  Scenario: get global properties diff on final values between two platforms
    Given an existing module with this template content
      """
      {{property-X-in-p1-and-p2}}
      {{property-Y-in-p1-and-p2}}
      {{property-only-in-p1}}
      {{property-only-in-p2}}
      """
    And an existing platform named "P1" with this module
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
    When I get the global properties diff on final values between platforms "P1" and "P2"
    Then the diff is successfully retrieved
    And the resulting diff match these values
      | only_left           | only_right          | common                  | differing               |
      | property-only-in-p1 | property-only-in-p2 | property-Y-in-p1-and-p2 | property-X-in-p1-and-p2 |

  Scenario: get global properties diff on stored values between two platforms
    Given an existing module with this template content
      """
      {{property-a}}
      """
    And an existing platform named "P1" with this module
    And the platform "P1" has these global properties
      | name       | value          |
      | property-a | {{property-b}} |
      | property-b | p1             |
    And an existing platform named "P2"
    And the platform "P2" has these global properties
      | name       | value          |
      | property-a | {{property-b}} |
      | property-b | p2             |
    When I get the global properties diff on stored values between platforms "P1" and "P2"
    Then the diff is successfully retrieved
    And the resulting diff match these values
      | only_left | only_right | common     | differing  |
      |           |            | property-a | property-b |

  Scenario: get iterable properties diff on final values between two platforms

  Scenario: get iterable properties diff on stored values between two platforms

  Scenario: get properties diff on the same platform at a different timestamp

  Scenario: get properties diff between two platforms at a different timestamp

  Scenario: get global properties diff on the same platform at a different timestamp

  Scenario: get global properties diff between two platforms at a different timestamp

  Scenario: get iterable properties diff on the same platform at a different timestamp

  Scenario: get iterable properties diff between two platforms at a different timestamp
