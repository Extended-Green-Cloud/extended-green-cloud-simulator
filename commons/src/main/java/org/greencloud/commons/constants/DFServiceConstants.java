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
	public static final String RMA_SERVICE_TYPE = "RMA";
	public static final String CMA_SERVICE_TYPE = "CMA";
	public static final String SA_SERVICE_TYPE = "SA";
	public static final String GS_SERVICE_TYPE = "GS";

	public static final String RMA_SERVICE_NAME = "rma-handler";
	public static final String SA_SERVICE_NAME = "job-executor";
	public static final String GS_SERVICE_NAME = "power-supplier";
	public static final String CMA_SERVICE_NAME = "job-scheduling";
}
