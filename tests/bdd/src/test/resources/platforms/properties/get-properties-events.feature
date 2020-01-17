Feature: Get properties events

  Background:
    Given: an authenticated user

  Scenario: get module properties events of a platform
    Given an existing module with this template content
      """
      {{added-property}}
      {{modified-property}}
      {{removed-property}}
      """
    And an existing platform named "P1" with this module
    And the platform "P1" has these valued properties
      | name              | value |
      | added-property    | added |
      | modified-property | added |
      | removed-property  | added |
    And the platform "P1" has these valued properties
      | name              | value    |
      | modified-property | modified |
    When I get last module properties events
    Then the resulting properties events matches
      | author | comment | addedProperties | removedProperties | updatedProperties                                              |
      | user   | comment | added-property  | removed-property  | {name: update_property, old_value: added, new_value: modified} |

  Scenario: get module properties events of a platform containing only one event
    Given an existing module with this template content
      """
      {{added-property}}
      {{added-second-property}}
      """
    And an existing platform named "P1" with this module
    And the platform "P1" has these valued properties
      | name                  | value |
      | added-property        | added |
      | added-second-property | added |
    When I get last module properties events
    Then the resulting properties events matches
      | author | comment | addedProperties                       | removedProperties | updatedProperties |
      | user   | comment | added-property, added-second-property |                   |                   |

  Scenario: get previous module properties events of a platform
    Given an existing module with this template content
      """
      {{added-property}}
      {{modified-property}}
      {{removed-property}}
      """
    And an existing platform named "P1" with this module
    And the platform "P1" has these valued properties
      | name              | value |
      | added-property    | added |
      | modified-property | added |
      | removed-property  | added |
    And the platform "P1" has these valued properties
      | name              | value    |
      | modified-property | modified |
    And the platform "P1" has these valued properties
      | name              | value                |
      | modified-property | modified_second_time |
    When I get previous module properties events
    Then the resulting properties events matches
      | author | comment | addedProperties | removedProperties | updatedProperties                                                               |
      | user   | comment |                 |                   | {name: modified_property, old_value: modified, new_value: modified_second_time} |

  Scenario: get module properties events of a platform with an incorrect property path
    Given an existing module with properties
    And an existing platform with this module and valued properties
    When I try to get module properties events with an invalid property path
    Then there is no events founded

  Scenario: get module properties events of a platform with an incorrect version type
    Given an existing module with properties
    And an existing platform with this module and valued properties
    When I try to get module properties events with an invalid platform version type
    Then there is no events founded