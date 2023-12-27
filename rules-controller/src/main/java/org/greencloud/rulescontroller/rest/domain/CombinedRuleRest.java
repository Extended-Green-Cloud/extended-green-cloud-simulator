package org.greencloud.rulescontroller.rest.domain;

import java.io.Serializable;
import java.util.List;

import org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CombinedRuleRest extends RuleRest implements Serializable {

	AgentCombinedRuleType combinedRuleType;
	List<RuleRest> rulesToCombine;
}
