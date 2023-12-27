package org.greencloud.rulescontroller.rest.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchRuleRest extends RuleRest implements Serializable {

	String searchAgents;
	String handleNoResults;
	String handleResults;
}
