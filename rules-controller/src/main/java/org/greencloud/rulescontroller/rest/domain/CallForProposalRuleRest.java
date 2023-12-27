package org.greencloud.rulescontroller.rest.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CallForProposalRuleRest extends RuleRest implements Serializable {

	String createCFP;
	String compareProposals;
	String handleRejectProposal;
	String handleNoResponses;
	String handleNoProposals;
	String handleProposals;
}
