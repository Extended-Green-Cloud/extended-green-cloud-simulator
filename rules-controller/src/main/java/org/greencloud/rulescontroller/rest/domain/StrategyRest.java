package org.greencloud.rulescontroller.rest.domain;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Rest representation of strategy
 */
@Getter
@Setter
public class StrategyRest implements Serializable {

	String name;
	List<RuleRest> rules;
}
