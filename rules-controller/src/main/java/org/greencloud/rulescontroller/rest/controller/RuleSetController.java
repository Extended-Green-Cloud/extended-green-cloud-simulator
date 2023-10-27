package org.greencloud.rulescontroller.rest.controller;

import static org.greencloud.rulescontroller.rest.RuleSetRestApi.getAvailableRuleSets;

import org.greencloud.rulescontroller.rest.domain.RuleSetRest;
import org.greencloud.rulescontroller.ruleset.RuleSet;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RuleSetController {

	@PostMapping(value = "/ruleSet", consumes = "application/json")
	public ResponseEntity<String> injectNewRuleSet(@RequestBody final RuleSetRest ruleSet) {
		final RuleSet newRuleSet = new RuleSet(ruleSet);

		getAvailableRuleSets().put(newRuleSet.getName(), newRuleSet);
		return ResponseEntity.ok("Rule set successfully injected");
	}
}
