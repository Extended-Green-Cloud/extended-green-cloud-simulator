package org.greencloud.commons.enums.allocation;

import java.util.List;

/**
 * Possible types of allocation algorithms modification
 */
public enum AllocationModificationEnum {

	NO_MODIFICATION, RESOURCE_SUFFICIENCY_CHECK, ENERGY_PREFERENCE, PRE_CLUSTERED_RESOURCES;

	/**
	 * Method verifies if a modification is included in the list.
	 *
	 * @param modificationsList modifications list
	 * @return boolean indicating if modification is included
	 */
	public boolean isModificationIncluded(final List<AllocationModificationEnum> modificationsList) {
		return modificationsList.contains(this);
	}
}
