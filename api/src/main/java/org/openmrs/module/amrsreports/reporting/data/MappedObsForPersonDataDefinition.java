/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.amrsreports.reporting.data;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class MappedObsForPersonDataDefinition extends BaseDataDefinition implements PersonDataDefinition {

	private TimeQualifier which;

	private Map<Concept, Collection<Concept>> questionAnswerMap;

	private Date onOrAfter;

	private Date onOrBefore;

	/**
	 * @see org.openmrs.module.reporting.data.DataDefinition#getDataType()
	 */
	public Class<?> getDataType() {
		if (which == TimeQualifier.LAST || which == TimeQualifier.FIRST) {
			return Obs.class;
		}
		return List.class;
	}

	public TimeQualifier getWhich() {
		return which;
	}

	public void setWhich(final TimeQualifier which) {
		this.which = which;
	}

	public void addQuestionAnswer(final Concept question, final Collection<Concept> answers) {
		getQuestionAnswerMap().put(question, answers);
	}

	public Collection<Concept> getAnswers(final Concept question) {
		return getQuestionAnswerMap().get(question);
	}

	public Map<Concept, Collection<Concept>> getQuestionAnswerMap() {
		if (questionAnswerMap == null)
			questionAnswerMap = new HashMap<Concept, Collection<Concept>>();
		return questionAnswerMap;
	}

	public void setQuestionAnswerMap(final Map<Concept, Collection<Concept>> questionAnswerMap) {
		this.questionAnswerMap = questionAnswerMap;
	}

	public Date getOnOrAfter() {
		return onOrAfter;
	}

	public void setOnOrAfter(final Date onOrAfter) {
		this.onOrAfter = onOrAfter;
	}

	public Date getOnOrBefore() {
		return onOrBefore;
	}

	public void setOnOrBefore(final Date onOrBefore) {
		this.onOrBefore = onOrBefore;
	}
}
