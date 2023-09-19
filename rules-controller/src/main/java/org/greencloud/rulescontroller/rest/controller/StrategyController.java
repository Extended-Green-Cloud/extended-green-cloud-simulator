package org.greencloud.rulescontroller.rest.controller;

import static org.greencloud.rulescontroller.rest.StrategyRestApi.getAvailableStrategies;

import org.greencloud.rulescontroller.rest.domain.StrategyRest;
import org.greencloud.rulescontroller.strategy.Strategy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StrategyController {

	@PostMapping(value = "/strategy", consumes = "application/json")
	public ResponseEntity<String> injectNewStrategy(@RequestBody final StrategyRest strategy) {
		final Strategy newStrategy = new Strategy(strategy);

		getAvailableStrategies().put(newStrategy.getName(), newStrategy);
		return ResponseEntity.ok("Strategy successfully injected");
	}
}
