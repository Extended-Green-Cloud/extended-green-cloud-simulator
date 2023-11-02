package org.greencloud.rulescontroller.rest.controller;

import static org.greencloud.rulescontroller.rest.RuleSetRestApi.getAgentNodes;
import static org.greencloud.rulescontroller.rest.RuleSetRestApi.getAvailableRuleSets;

import org.greencloud.gui.agents.AgentNode;
import org.greencloud.rulescontroller.rest.domain.RuleSetRest;
import org.greencloud.rulescontroller.ruleset.RuleSet;
import org.greencloud.rulescontroller.ruleset.domain.ModifyAgentRuleSetEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RuleSetController {

	@PostMapping(value = "/ruleSet", consumes = "application/json")
	public ResponseEntity<String> injectNewRuleSet(@RequestBody final RuleSetRest ruleSet) {
		final RuleSet newRuleSet = new RuleSet(ruleSet);

		getAvailableRuleSets().put(newRuleSet.getName(), newRuleSet);
		return ResponseEntity.ok("Rule set successfully injected");
	}

	@PutMapping(value = "/ruleSet", consumes = "application/json")
	public ResponseEntity<String> modifyRuleSet(@RequestBody final RuleSetRest ruleSet,
			@RequestParam String agentName, @RequestParam boolean replaceFully) {
		final RuleSet newRuleSet = new RuleSet(ruleSet);

		final AgentNode agentNode = getAgentNodes().stream()
				.filter(node -> node.getAgentName().equals(agentName)).findFirst()
				.orElseThrow();
		agentNode.addEvent(new ModifyAgentRuleSetEvent(replaceFully, newRuleSet, agentName));
		return ResponseEntity.ok("Rule set change event injected in agent node.");
	}
}
