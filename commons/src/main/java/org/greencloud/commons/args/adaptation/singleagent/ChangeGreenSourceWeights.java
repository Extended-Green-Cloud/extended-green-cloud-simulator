package org.greencloud.commons.args.adaptation.singleagent;

import org.greencloud.commons.args.adaptation.AdaptationActionParameters;

/**
 * Content of the message sent when the adaptation plan which changes weights for Green Source selection is
 *  executed
 * @param greenSourceName name of the green source for which plan is to be executed
 */
public record ChangeGreenSourceWeights(String greenSourceName) implements AdaptationActionParameters {
}
