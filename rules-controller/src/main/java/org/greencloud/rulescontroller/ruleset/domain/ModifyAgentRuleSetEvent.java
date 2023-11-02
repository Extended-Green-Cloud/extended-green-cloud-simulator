package org.greencloud.rulescontroller.ruleset.domain;

import static org.greencloud.gui.event.domain.EventTypeEnum.MODIFY_RULE_SET;

import java.util.Map;

import org.greencloud.gui.agents.egcs.EGCSNode;
import org.greencloud.gui.event.AbstractEvent;
import org.greencloud.rulescontroller.ruleset.RuleSet;

import lombok.Getter;

@Getter
public class ModifyAgentRuleSetEvent extends AbstractEvent {

	Boolean replaceFully;
	RuleSet newRuleSet;

	/**
	 * Default event constructor
	 */
	public ModifyAgentRuleSetEvent(final Boolean replaceFully, final RuleSet newRuleSet, final String agentName) {
		super(MODIFY_RULE_SET, null, agentName);
		this.replaceFully = replaceFully;
		this.newRuleSet = newRuleSet;
	}

	@Override
	public void trigger(final Map<String, EGCSNode> agentNodes) {
		// no communication with agents here
	}
}
