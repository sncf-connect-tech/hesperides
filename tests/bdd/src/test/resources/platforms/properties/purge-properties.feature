#issue-803
Feature: Get rid of unneeded values

  Background:
    Given an authenticated user

  Scenario: clean all modules in platform
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
    And the module "loh" has these valued properties
      | name       | value        |
      | a_property | a-value      |
      | awwwww     | right        |
      | b_property | other-value  |
      | c_property | misplaced    |
    And the module "behold" has these valued properties
      | name       | value        |
      | c_property | something    |
      | d_property | barrel-roll  |
      | foo        | bar          |
    When I purge unneeded properties of this platform
    Then the module "loh" contains only the following properties
      | a_property |
      | b_property |
    And the module "behold" contains only the following properties
      | c_property |

  Scenario: clean specific module
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
    And the module "shall_pass" has these valued properties
      | name       | value        |
      | a_property | a-value      |
      | b_property | other-value  |
      | c_property | misplaced    |
    And the module "Istari" has these valued properties
      | name       | value        |
      | c_property | something    |
      | d_property | barrel-roll  |
      | foo        | bar          |
    When I purge unneeded properties of module "shall_pass"
    Then the module "shall_pass" contains only the following properties
      | a_property |
      | b_property |
    And the module "Istari" still contains all the following properties
      | c_property |
      | d_property |
      | foo        |

  Scenario: clean unknown module
    Given an existing module named "bob"
    And an existing platform with this module and valued properties
    When I try to purge unneeded properties of unknown module "not_bob"
    Then the resource is not found

  Scenario: clean global properties
    Given an existing module
    And an existing platform with this module and valued properties and global properties
    When I try to purge unneeded global properties of this platform
    Then the request is rejected with a bad request error

  Scenario: do not clean values referenced by other properties
    Given an existing module named "basic" with this template content
      """
      {{ a_property }}
      {{ b_property }}
      """
    And an existing platform with this module
    And the module "basic" has these valued properties
      | name       | value                  |
      | a_property | {{ind1}}//url/{{ind3}} |
      | b_property | {{ind1}}{{ind2}}?max=9 |
      | ind1       | http:                  |
      | ind2       | //other_url/{{ind4}}   |
      | ind4       | service_path           |
    When I purge unneeded properties of this platform
    Then the module "basic" still contains all the following properties
      | a_property |
      | b_property |
      | ind1       |
      | ind2       |
      | ind4       |

  Scenario: do not clean values referenced by cleanable properties
    Given an existing module named "onion" with this template content
      """
      {{ a_property }}
      {{ b_property }}
      """
    And an existing platform with this module
    And the module "onion" has these valued properties
      | name       | value        |
      | a_property | http://url   |
      | b_property | direct_value |
      | c_property | {{ind1}}ms   |
      | ind1       | 600          |
    When I purge unneeded properties of this platform
    Then the module "onion" contains only the following properties
      | a_property |
      | b_property |
      | ind1       |

  Scenario: do not clean values referenced by instance
    Given an existing module named "instanced" with this template content
      """
      {{ a_property }}
      {{ b_property }}
      {{ c_property }}
      """
    And an existing platform with this module and an instance
    And the module "instanced" has these valued properties
      | name       | value         |
      | a_property | 10s           |
      | b_property | topicA        |
      | ind1       | http:         |
      | ind2       | www.perdu.com |
    And the platform has these instance properties
      | name       | value              |
      | c_property | {{ind1}}//{{ind2}} |
    When I purge unneeded properties of this platform
    Then the module "instanced" still contains all the following properties
      | a_property |
      | b_property |
      | ind1       |
      | ind2       |
