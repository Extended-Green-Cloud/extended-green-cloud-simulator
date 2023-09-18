package org.greencloud.commons.args.adaptation.singleagent;

import org.greencloud.commons.args.adaptation.AdaptationActionParameters;

public record ChangeGreenSourceWeights(String greenSourceName) implements AdaptationActionParameters {
}
