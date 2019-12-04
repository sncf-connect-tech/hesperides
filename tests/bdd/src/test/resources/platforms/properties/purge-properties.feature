Feature: Get rid of unneeded values

  Background:
    Given an authenticated user

  #issue-803
  Scenario: clean all platforms
    Given an existing module named "loh" with this template content
      """
      {{ a_property }}
      {{ b_property }}
      """
    And an existing module named "behold" with this template content
      """
      {{ c_property }}
      """
    And an existing platform with those modules
    And the module "loh" of the platform has these valued properties
      | name       | value        |
      | a_property | a-value      |
      | awwwww     | right        |
      | b_property | other-value  |
      | c_property | misplaced    |
    And the module "behold" of the platform has these valued properties
      | name       | value        |
      | c_property | something    |
      | d_property | barrel-roll  |
      | foo        | bar          |
    When I purge unneeded properties
    Then module "loh" contains only the following properties
      | a_property |
      | b_property |
    And module "behold" contains only the following properties
      | c_property |

  Scenario: clean specific path
    Given an existing module named "shall_pass" with this template content
      """
      {{ a_property }}
      {{ b_property }}
      """
    And an existing module named "Istari" with this template content
      """
      {{ c_property }}
      """
    And an existing platform with those modules
    And the module "shall_pass" of the platform has these valued properties
      | name       | value        |
      | a_property | a-value      |
      | b_property | other-value  |
      | c_property | misplaced    |
    And the module "Istari" of the platform has these valued properties
      | name       | value        |
      | c_property | something    |
      | d_property | barrel-roll  |
      | foo        | bar          |
    When I purge unneeded properties on path "#ABC#DEF#shall_pass#1.0#WORKINGCOPY"
    Then module "shall_pass" contains only the following properties
      | a_property |
      | b_property |
    And module "Istari" contains only the following properties
      | c_property |
      | d_property |
      | foo        |

