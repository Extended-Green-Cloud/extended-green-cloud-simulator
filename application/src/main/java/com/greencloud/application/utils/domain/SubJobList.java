package com.greencloud.application.utils.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Class which represents a job list which is used in algorithm finding set of jobs withing given power
 */
public class SubJobList {
	public int size;
	public List<?> subList;

	public SubJobList() {
		this(0, new ArrayList<>());
	}

	public SubJobList(int size, List<?> subList) {
		this.size = size;
		this.subList = subList;
	}
}
