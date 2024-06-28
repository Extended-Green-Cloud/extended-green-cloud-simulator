package org.greencloud.commons.constants.resource;

import java.util.List;

/**
 * Class with default resource types
 */
public class ResourceTypesConstants {

	public static final String ID = "id";
	public static final String TYPE  = "type";
	public static final String CPU = "cpu";
	public static final String CU = "computeUnit";
	public static final String MEMORY = "memory";
	public static final String STORAGE = "storage";
	public static final String BUDGET = "budget";
	public static final String RELIABILITY = "reliability";
	public static final String DURATION = "duration";
	public static final String PRIORITY = "priority";
	public static final String START_TIME = "startTime";
	public static final String SUFFICIENCY = "sufficiency";
	public static final String ENERGY = "energy";


	public static final List<String> BASIC_RESOURCES = List.of(CPU, MEMORY, STORAGE);
}
