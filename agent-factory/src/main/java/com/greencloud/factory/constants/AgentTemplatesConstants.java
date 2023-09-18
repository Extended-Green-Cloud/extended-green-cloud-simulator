package com.greencloud.factory.constants;

import static org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum.WIND;

import org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum;
import org.greencloud.commons.domain.resources.HardwareResources;
import org.greencloud.commons.domain.resources.ImmutableHardwareResources;

public class AgentTemplatesConstants {

    // SERVER TEMPLATE CONSTANTS
    public static final Long TEMPLATE_SERVER_MAXIMUM_CAPACITY = 200L;
    public static final Integer TEMPLATE_SERVER_MAX_POWER = 200;
    public static final Integer TEMPLATE_SERVER_IDLE_POWER = 30;
    public static final Double TEMPLATE_SERVER_PRICE = 20D;
    public static final HardwareResources TEMPLATE_SERVER_RESOURCES =
            ImmutableHardwareResources.builder().cpu(4D).memory(20D).storage(32D).build();
    public static final Integer TEMPLATE_SERVER_JOB_LIMIT = 20;

    // GREEN ENERGY TEMPLATE CONSTANTS
    public static final String TEMPLATE_GREEN_ENERGY_LATITUDE = "50";
    public static final String TEMPLATE_GREEN_ENERGY_LONGITUDE = "20";
    public static final Long TEMPLATE_GREEN_ENERGY_PRICE = 10L;
    public static final Long TEMPLATE_GREEN_ENERGY_MAXIMUM_CAPACITY = 200L;
    public static final GreenEnergySourceTypeEnum TEMPLATE_GREEN_ENERGY_TYPE = WIND;
}
