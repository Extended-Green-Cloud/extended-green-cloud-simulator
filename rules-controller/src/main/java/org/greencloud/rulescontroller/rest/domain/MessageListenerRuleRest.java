package org.greencloud.rulescontroller.rest.domain;

import java.io.Serializable;

import org.greencloud.commons.enums.rules.RuleType;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageListenerRuleRest extends RuleRest implements Serializable {

	String className;
	String messageTemplate;
	int batchSize;
	RuleType actionHandler;
	String selectStrategyIdx;
}
