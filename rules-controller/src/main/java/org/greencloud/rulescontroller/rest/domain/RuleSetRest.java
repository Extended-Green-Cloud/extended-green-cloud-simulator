package org.greencloud.rulescontroller.rest.domain;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Rest representation of the rule set
 */
@Getter
@Setter
public class RuleSetRest implements Serializable {

	String name;
	List<RuleRest> rules;
}
