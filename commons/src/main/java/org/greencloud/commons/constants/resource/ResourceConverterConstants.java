package org.greencloud.commons.constants.resource;

import static java.util.Map.entry;

import java.util.Map;

/**
 * Class contains common expressions responsible for converting the units of resources
 */
public class ResourceConverterConstants {

	//	COMMON CORE UNIT CONVERTERS
	public static final String TO_CPU_CORES_CONVERTER = """
			return value / 1000;
			""";
	public static final String FROM_CPU_CORES_CONVERTER = """
			return value * 1000;
			""";

	public static final String FROM_KI_CUDA_CORES_CONVERTER = """
			return value * 1000;
			""";

	public static final String TO_KI_CUDA_CORES_CONVERTER = """
			return value / 1000;
			""";

	// COMMON BYTE UNIT CONVERTERS

	public static final String FROM_KI_TO_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) Math.pow(2, 10) * value;
			""";
	public static final String FROM_MI_TO_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) Math.pow(2, 20) * value;
			""";
	public static final String FROM_GI_TO_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) Math.pow(2, 30) * value;
			""";
	public static final String FROM_TI_TO_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) Math.pow(2, 40) * value;
			""";
	public static final String FROM_PI_TO_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) Math.pow(2, 50) * value;
			""";
	public static final String FROM_EI_TO_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) Math.pow(2, 60) * value;
			""";
	public static final String FROM_ZI_TO_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) Math.pow(2, 70) * value;
			""";
	public static final String FROM_YI_TO_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) Math.pow(2, 80) * value;
			""";
	public static final String FROM_KB_TO_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) Math.pow(10, 3) * value;
			""";
	public static final String FROM_MB_TO_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) Math.pow(10, 6) * value;
			""";
	public static final String FROM_GB_TO_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) Math.pow(10, 9) * value;
			""";
	public static final String FROM_TB_TO_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) Math.pow(10, 12) * value;
			""";
	public static final String FROM_PB_TO_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) Math.pow(10, 15) * value;
			""";
	public static final String FROM_EB_TO_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) Math.pow(10, 18) * value;
			""";

	public static final String TO_KI_FROM_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) value / Math.pow(2, 10);
			""";
	public static final String TO_MI_FROM_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) value / Math.pow(2, 20);
			""";
	public static final String TO_GI_FROM_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) value / Math.pow(2, 30);
			""";
	public static final String TO_TI_FROM_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) value / Math.pow(2, 40);
			""";
	public static final String TO_PI_FROM_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) value / Math.pow(2, 50);
			""";
	public static final String TO_EI_FROM_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) value / Math.pow(2, 60);
			""";
	public static final String TO_ZI_FROM_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) value / Math.pow(2, 70);
			""";
	public static final String TO_YI_FROM_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) value / Math.pow(2, 80);
			""";
	public static final String TO_KB_FROM_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) value / Math.pow(10, 3);
			""";
	public static final String TO_MB_FROM_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) value / Math.pow(10, 6);
			""";
	public static final String TO_GB_FROM_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) value / Math.pow(10, 9);
			""";
	public static final String TO_TB_FROM_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) value / Math.pow(10, 12);
			""";
	public static final String TO_PB_FROM_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) value / Math.pow(10, 15);
			""";
	public static final String TO_EB_FROM_BYTE_CONVERTER = """
			import java.lang.Math;
			return (double) value / Math.pow(10, 18);
			""";

	public static final Map<String, String> commonConverters = Map.ofEntries(
			entry("TO_CPU_CORES_CONVERTER", TO_CPU_CORES_CONVERTER),
			entry("FROM_CPU_CORES_CONVERTER", FROM_CPU_CORES_CONVERTER),
			entry("FROM_KI_CUDA_CORES_CONVERTER", FROM_KI_CUDA_CORES_CONVERTER),
			entry("TO_KI_CUDA_CORES_CONVERTER", TO_KI_CUDA_CORES_CONVERTER),
			entry("FROM_KI_TO_BYTE_CONVERTER", FROM_KI_TO_BYTE_CONVERTER),
			entry("FROM_MI_TO_BYTE_CONVERTER", FROM_MI_TO_BYTE_CONVERTER),
			entry("FROM_GI_TO_BYTE_CONVERTER", FROM_GI_TO_BYTE_CONVERTER),
			entry("FROM_TI_TO_BYTE_CONVERTER", FROM_TI_TO_BYTE_CONVERTER),
			entry("FROM_PI_TO_BYTE_CONVERTER", FROM_PI_TO_BYTE_CONVERTER),
			entry("FROM_EI_TO_BYTE_CONVERTER", FROM_EI_TO_BYTE_CONVERTER),
			entry("FROM_ZI_TO_BYTE_CONVERTER", FROM_ZI_TO_BYTE_CONVERTER),
			entry("FROM_YI_TO_BYTE_CONVERTER", FROM_YI_TO_BYTE_CONVERTER),
			entry("FROM_KB_TO_BYTE_CONVERTER", FROM_KB_TO_BYTE_CONVERTER),
			entry("FROM_MB_TO_BYTE_CONVERTER", FROM_MB_TO_BYTE_CONVERTER),
			entry("FROM_GB_TO_BYTE_CONVERTER", FROM_GB_TO_BYTE_CONVERTER),
			entry("FROM_TB_TO_BYTE_CONVERTER", FROM_TB_TO_BYTE_CONVERTER),
			entry("FROM_PB_TO_BYTE_CONVERTER", FROM_PB_TO_BYTE_CONVERTER),
			entry("FROM_EB_TO_BYTE_CONVERTER", FROM_EB_TO_BYTE_CONVERTER),
			entry("TO_KI_FROM_BYTE_CONVERTER", TO_KI_FROM_BYTE_CONVERTER),
			entry("TO_MI_FROM_BYTE_CONVERTER", TO_MI_FROM_BYTE_CONVERTER),
			entry("TO_GI_FROM_BYTE_CONVERTER", TO_GI_FROM_BYTE_CONVERTER),
			entry("TO_PI_FROM_BYTE_CONVERTER", TO_PI_FROM_BYTE_CONVERTER),
			entry("TO_EI_FROM_BYTE_CONVERTER", TO_EI_FROM_BYTE_CONVERTER),
			entry("TO_ZI_FROM_BYTE_CONVERTER", TO_ZI_FROM_BYTE_CONVERTER),
			entry("TO_YI_FROM_BYTE_CONVERTER", TO_YI_FROM_BYTE_CONVERTER),
			entry("TO_KB_FROM_BYTE_CONVERTER", TO_KB_FROM_BYTE_CONVERTER),
			entry("TO_MB_FROM_BYTE_CONVERTER", TO_MB_FROM_BYTE_CONVERTER),
			entry("TO_GB_FROM_BYTE_CONVERTER", TO_GB_FROM_BYTE_CONVERTER),
			entry("TO_TB_FROM_BYTE_CONVERTER", TO_TB_FROM_BYTE_CONVERTER),
			entry("TO_PB_FROM_BYTE_CONVERTER", TO_PB_FROM_BYTE_CONVERTER),
			entry("TO_EB_FROM_BYTE_CONVERTER", TO_EB_FROM_BYTE_CONVERTER),
			entry("TO_TI_FROM_BYTE_CONVERTER", TO_TI_FROM_BYTE_CONVERTER)
	);
}
