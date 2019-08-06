Feature: Get properties diffs

  Background:
    Given an authenticated user

  Scenario: get properties diffs between two platforms
    Given an existing module with this template content
      """
      {{only-p1}}
      {{only-p2}}
      {{common-property}}
      {{differing-property}}
      """
    And an existing platform named "P1" with this module
    And the platform "P1" has these valued properties
      | name               | value    |
      | only-p1            | value    |
      | common-property    | value    |
      | differing-property | p1-value |
    And an existing platform named "P2" with this module
    And the platform "P2" has these valued properties
      | name               | value    |
      | only-p2            | value    |
      | common-property    | value    |
      | differing-property | p2-value |
    When I get the properties diff on final values between platforms "P1" and "P2"
    Then the diff is successfully retrieved
    And the resulting diff match these values
      | only_left | only_right | common          | differing          |
      | only-p1   | only-p2    | common-property | differing-property |

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

  Scenario: get global properties diffs between two platforms
    Given an existing platform named "P1"
    And the platform "P1" has these global properties
      | name               | value    |
      | only-p1            | value    |
      | common-property    | value    |
      | differing-property | p1-value |
    And an existing platform named "P2" with this module
    And the platform "P2" has these global properties
      | name               | value    |
      | only-p2            | value    |
      | common-property    | value    |
      | differing-property | p2-value |
    When I get the global properties diff on final values between platforms "P1" and "P2"
    Then the diff is successfully retrieved
    And the resulting diff match these values
      | only_left | only_right | common          | differing          |
      | only-p1   | only-p2    | common-property | differing-property |

  Scenario: get global properties diff on final values between two platforms
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
    When I get the global properties diff on final values between platforms "P1" and "P2"
    Then the diff is successfully retrieved
    And the resulting diff match these values
      | only_left | only_right | common | differing  |
      |           |            |        | property-a |
      |           |            |        | property-b |

  Scenario: get global properties diff on stored values between two platforms
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
    When I get the global properties diff on stored values between platforms "P1" and "P2"
    Then the diff is successfully retrieved
    And the resulting diff match these values
      | only_left | only_right | common     | differing  |
      |           |            | property-a | property-b |

  Scenario: get instance properties diffs on final values between two platforms
    Given an existing module with this template content
      """
      {{a-property}}
      """
    And an existing platform named "P1" with this module
    And the platform "P1" has these valued properties
      | name       | value                   |
      | a-property | {{ instance-property }} |
    And the platform has these instance properties
      | name              | value           |
      | instance-property | instance-value1 |
    And an existing platform named "P2" with this module
    And the platform "P2" has these valued properties
      | name       | value                   |
      | a-property | {{ instance-property }} |
    And the platform has these instance properties
      | name              | value           |
      | instance-property | instance-value2 |
    When I get the instance properties diff on final values between platforms "P1" and "P2"
    Then the diff is successfully retrieved
    And the resulting diff match these values
      | only_left | only_right | common | differing         |
      |           |            |        | a-property        |
      |           |            |        | instance-property |

  Scenario: get instance properties diffs on stored values between two platforms
    Given an existing module with this template content
      """
      {{a-property}}
      """
    And an existing platform named "P1" with this module
    And the platform "P1" has these valued properties
      | name       | value                   |
      | a-property | {{ instance-property }} |
    And the platform has these instance properties
      | name              | value           |
      | instance-property | instance-value1 |
    And an existing platform named "P2" with this module
    And the platform "P2" has these valued properties
      | name       | value                   |
      | a-property | {{ instance-property }} |
    And the platform has these instance properties
      | name              | value           |
      | instance-property | instance-value2 |
    When I get the instance properties diff on stored values between platforms "P1" and "P2"
    Then the diff is successfully retrieved
    And the resulting diff match these values
      | only_left | only_right | common     | differing         |
      |           |            | a-property | instance-property |

  Scenario: get iterable properties diff on final values between two platforms

  Scenario: get iterable properties diff on stored values between two platforms

  Scenario: get properties diff on the same platform at a different timestamp

  Scenario: get properties diff between two platforms at a different timestamp

  Scenario: get global properties diff on the same platform at a different timestamp

  Scenario: get global properties diff between two platforms at a different timestamp

  Scenario: get iterable properties diff on the same platform at a different timestamp

  Scenario: get iterable properties diff between two platforms at a different timestamp
