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
    Then the resulting diff matches
      | onlyLeft | onlyRight | common          | differing          |
      | only-p1  | only-p2   | common-property | differing-property |

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
    Then the resulting diff matches
      | onlyLeft | onlyRight | common | differing  |
      |          |           |        | property-a |
      |          |           |        | property-b |

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
    Then the resulting diff matches
      | onlyLeft | onlyRight | common     | differing  |
      |          |           | property-a | property-b |

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
    Then the resulting diff matches
      | onlyLeft | onlyRight | common          | differing |
      |          |           | simple-property |           |

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
    Then the resulting diff matches
      | onlyLeft | onlyRight       | common | differing |
      |          | simple-property |        |           |

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
    Then the resulting diff matches
      | onlyLeft | onlyRight | common          | differing          |
      | only-p1  | only-p2   | common-property | differing-property |

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
    Then the resulting diff matches
      | onlyLeft | onlyRight | common | differing  |
      |          |           |        | property-a |
      |          |           |        | property-b |

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
    Then the resulting diff matches
      | onlyLeft | onlyRight | common     | differing  |
      |          |           | property-a | property-b |

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
    Then the resulting diff matches
      | onlyLeft | onlyRight | common | differing         |
      |          |           |        | a-property        |
      |          |           |        | instance-property |

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
    Then the resulting diff matches
      | onlyLeft | onlyRight | common     | differing         |
      |          |           | a-property | instance-property |

  Scenario: get iterable properties diff on final values between two platforms

  Scenario: get iterable properties diff on stored values between two platforms

  Scenario: get properties diff on the same platform at a different timestamp
    Given an existing module with this template content
      """
      {{only-p1}}
      {{only-p2}}
      {{common-property}}
      {{differing-property}}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name               | value    |
      | only-p1            | value    |
      | common-property    | value    |
      | differing-property | p1-value |
    And the platform has these valued properties
      | name               | value    |
      | only-p2            | value    |
      | common-property    | value    |
      | differing-property | p2-value |
    When I get the properties diff on final values between the first and second version of the platform values
    Then the resulting diff matches
      | onlyLeft | onlyRight | common          | differing          |
      | only-p2  | only-p1   | common-property | differing-property |

  Scenario: get global properties diff on the same platform at a different timestamp
    Given an existing platform
    And the platform has these global properties
      | name               | value    |
      | only-p1            | value    |
      | common-property    | value    |
      | differing-property | p1-value |
    And the platform has these global properties
      | name               | value    |
      | only-p2            | value    |
      | common-property    | value    |
      | differing-property | p2-value |
    When I get the global properties diff on final values between the first and second version of the platform values
    Then the resulting diff matches
      | onlyLeft | onlyRight | common          | differing          |
      | only-p2  | only-p1   | common-property | differing-property |

  Scenario: get iterable properties diff on the same platform at a different timestamp

  Scenario: get global properties diffs with a property that is empty in the left and not provided in the right
    Given an existing platform named "P1"
    And the platform "P1" has these global properties
      | name   | value |
      | common |       |
    And an existing platform named "P2"
    When I get the global properties diff on final values between platforms "P1" and "P2"
    Then the resulting diff matches
      | onlyLeft | onlyRight | common | differing |
      |          |           | common |           |


  Scenario: get global properties diffs with a property that is empty in the right and not provided in the left
    Given an existing platform named "P1"
    And an existing platform named "P2"
    And the platform "P2" has these global properties
      | name   | value |
      | common |       |
    When I get the global properties diff on final values between platforms "P1" and "P2"
    Then the resulting diff matches
      | onlyLeft | onlyRight | common | differing |
      |          |           | common |           |

  #issue-752
  Scenario: a diff between modules does not contain global properties
    Given an existing module
    And an existing platform named "P1" with this module
    And an existing platform named "P2" with this module
    And the platform "P2" has these global properties
      | name            | value        |
      | global-property | GLOBAL_VALUE |
    When I get the properties diff on final values between platforms "P1" and "P2"
    Then the diff is empty

  #issue-792
  Scenario: get properties diffs with a property that references a global property
    Given an existing module with this template content
      """
      {{property}}
      """
    And an existing platform named "P1" with this module
    And the platform "P1" has these valued properties
      | name     | value        |
      | property | {{global-a}} |
    And the platform "P1" has these global properties
      | name         | value            |
      | global-a     | {{global-final}} |
      | global-final | global-value     |
    And an existing platform named "P2" with this module
    And the platform "P2" has these valued properties
      | name     | value            |
      | property | {{global-final}} |
    And the platform "P2" has these global properties
      | name         | value        |
      | global-final | global-value |
    When I get the properties diff on final values between platforms "P1" and "P2"
    Then the resulting diff matches
      | onlyLeft | onlyRight | common   | differing |
      |          |           | property |           |

  #issue-792
  Scenario: get properties diffs with a property that references a global property with different values
    Given an existing module with this template content
      """
      {{property}}
      """
    And an existing platform named "P1" with this module
    And the platform "P1" has these valued properties
      | name     | value      |
      | property | {{global}} |
    And the platform "P1" has these global properties
      | name         | value            |
      | global       | {{global-final}} |
      | global-final | value-a          |
    And an existing platform named "P2" with this module
    And the platform "P2" has these valued properties
      | name     | value      |
      | property | {{global}} |
    And the platform "P2" has these global properties
      | name         | value            |
      | global       | {{global-final}} |
      | global-final | value-b          |
    When I get the properties diff on final values between platforms "P1" and "P2"
    Then the resulting diff matches
      | onlyLeft | onlyRight | common | differing |
      |          |           |        | property  |

  #issue-810
  Scenario: compare an undefined property to a property with default value
    Given an existing module with version "1"
    And an existing module with version "2" with this template content
      """
      {{ property | @default false }}
      """
    And an existing platform with those modules
    When I get the properties diff on final values of this platform between module versions "1" and "2"
    Then the resulting diff matches
      | onlyLeft | onlyRight | common | differing |
      |          | property  |        |           |

  #issue-818
  Scenario: comparison including an iterable property that only exists on the left side
    Given an existing module with this template content
      """
      {{#iterable_property}}
        {{ simple_property }}
      {{/iterable_property}}
      """
    And another existing module
    And an existing platform with those modules
    When I get the properties diff on final values between the currently deployed modules
    Then the resulting diff matches
      | onlyLeft | onlyRight | common            | differing |
      |          |           | iterable_property |           |
