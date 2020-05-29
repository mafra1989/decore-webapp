package com.webapp.model;

import java.util.Comparator;

public class OrdenaTop5 implements Comparator<Top5Despesa> {

	@Override
	public int compare(Top5Despesa top5Despesa1, Top5Despesa top5Despesa2) {
		return top5Despesa1.getValue().compareTo(top5Despesa2.getValue());
	}

}