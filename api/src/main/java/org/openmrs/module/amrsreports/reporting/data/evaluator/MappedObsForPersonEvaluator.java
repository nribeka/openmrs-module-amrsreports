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
package org.openmrs.module.amrsreports.reporting.data.evaluator;

import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.amrsreports.reporting.data.MappedObsForPersonDataDefinition;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.ListMap;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.person.EvaluatedPersonData;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.data.person.evaluator.PersonDataEvaluator;
import org.openmrs.module.reporting.dataset.query.service.DataSetQueryService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
@Handler(supports = MappedObsForPersonDataDefinition.class, order = 50)
public class MappedObsForPersonEvaluator implements PersonDataEvaluator {

	/**
	 * @should return the obs that match the passed definition configuration
	 * @see PersonDataEvaluator#evaluate(org.openmrs.module.reporting.data.person.definition.PersonDataDefinition, org.openmrs.module.reporting.evaluation.EvaluationContext)
	 */
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context) throws EvaluationException {

		MappedObsForPersonDataDefinition mappedDataDefinition = (MappedObsForPersonDataDefinition) definition;
		EvaluatedPersonData data = new EvaluatedPersonData(mappedDataDefinition, context);
		Cohort cohort = context.getBaseCohort();
		if (cohort != null && !cohort.isEmpty()) {
			DataSetQueryService qs = Context.getService(DataSetQueryService.class);

			StringBuilder hql = new StringBuilder();
			Map<String, Object> mappings = new HashMap<String, Object>();

			hql.append(" from Obs ");
			hql.append(" where voided = false ");

			hql.append("   and personId in (:patientIds) ");
			mappings.put("patientIds", context.getBaseCohort());

			int counter = 0;
			for (Concept question : mappedDataDefinition.getQuestionAnswerMap().keySet()) {
				// create the question clause, this question is mandatory!
				String questionClause = " concept.conceptId = :question" + counter;
				mappings.put("question" + counter, question.getConceptId());
				// create the answer clause, this clause might have answer null.
				Collection<Concept> answers = mappedDataDefinition.getAnswers(question);
				String answerClause = " valueCoded.conceptId is null";
				if (answers != null) {
					answerClause = " valueCoded.conceptId in ( :answer" + counter + ")";
					List<Integer> answerIds = new ArrayList<Integer>();
					for (Concept answer : answers)
						answerIds.add(answer.getConceptId());
					mappings.put("answer" + counter, answerIds);
				}
				hql.append("   and ( ").append(questionClause).append(" and ").append(answerClause).append(" ) ");
				counter ++;
			}

			if (mappedDataDefinition.getOnOrAfter() != null) {
				hql.append("   and obsDatetime >= :onOrAfter ");
				mappings.put("onOrAfter", mappedDataDefinition.getOnOrAfter());
			}

			if (mappedDataDefinition.getOnOrBefore() != null) {
				hql.append("   and obsDatetime <= :onOrBefore ");
				mappings.put("onOrBefore", DateUtil.getEndOfDayIfTimeExcluded(mappedDataDefinition.getOnOrBefore()));
			}

			String ordering = " asc ";
			if (mappedDataDefinition.getWhich() == TimeQualifier.LAST)
				ordering = " desc ";

			hql.append(" order by obsDatetime ").append(ordering);

			List<Object> queryResult = qs.executeHqlQuery(hql.toString(), mappings);

			ListMap<Integer, Obs> obsForPatients = new ListMap<Integer, Obs>();
			for (Object object : queryResult) {
				Obs obs = (Obs) object;
				obsForPatients.putInList(obs.getPersonId(), obs);
			}

			for (Integer personId : obsForPatients.keySet()) {
				List<Obs> obsList = obsForPatients.get(personId);
				if (mappedDataDefinition.getWhich() == TimeQualifier.LAST
						|| mappedDataDefinition.getWhich() == TimeQualifier.FIRST) {
					data.addData(personId, obsList.get(0));
				} else {
					data.addData(personId, obsList);
				}
			}
		}

		return data;
	}
}
