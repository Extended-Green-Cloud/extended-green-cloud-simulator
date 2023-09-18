package org.greencloud.commons.constants;

/**
 * Constants describing the services registered in DF.
 * Used patterns:
 * <p/>
 * ..._TYPE - type of service
 * <p/>
 * ..._NAME - name of service
 */
public class DFServiceConstants {
	public static final String CNA_SERVICE_TYPE = "CNA";
	public static final String SCHEDULER_SERVICE_TYPE = "SCHEDULER";
	public static final String SA_SERVICE_TYPE = "SA";
	public static final String GS_SERVICE_TYPE = "GS";

	public static final String CNA_SERVICE_NAME = "network-handler";
	public static final String SA_SERVICE_NAME = "job-executor";
	public static final String GS_SERVICE_NAME = "power-supplier";
	public static final String SCHEDULER_SERVICE_NAME = "job-scheduling";
}
