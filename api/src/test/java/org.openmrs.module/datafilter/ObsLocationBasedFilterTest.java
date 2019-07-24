/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.test.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class ObsLocationBasedFilterTest extends BaseLocationBasedFilterTest {
	
	@Autowired
	private ObsService obsService;
	
	@Before
	public void before() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "encounters.xml");
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "observations.xml");
	}
	
	private List<Obs> getObservations() {
		List<Concept> questions = Collections.singletonList(new Concept(5089));
		List<Encounter> encounters = new ArrayList<>();
		encounters.add(new Encounter(1000));
		encounters.add(new Encounter(1001));
		encounters.add(new Encounter(1002));
		return obsService.getObservations(null, encounters, questions, null, null, null, null, null, null, null, null,
		    false);
	}
	
	@Test
	public void getObs_shouldReturnNoObsIfTheUserIsNotGrantedAccessToAnyBasis() {
		reloginAs("dBeckham", "test");
		assertEquals(0, getObservations().size());
	}
	
	@Test
	public void getObs_shouldReturnObsBelongingToPatientsAccessibleToTheUser() {
		reloginAs("dyorke", "test");
		int expCount = 2;
		Collection<Obs> observations = getObservations();
		assertEquals(expCount, observations.size());
		assertTrue(TestUtil.containsId(observations, 1001));
		assertTrue(TestUtil.containsId(observations, 1002));
		
		AccessUtilTest.grantLocationAccessToUser(Context.getAuthenticatedUser().getUserId(), 4001, getConnection());
		expCount = 3;
		observations = getObservations();
		assertEquals(expCount, observations.size());
		assertTrue(TestUtil.containsId(observations, 1001));
		assertTrue(TestUtil.containsId(observations, 1002));
		assertTrue(TestUtil.containsId(observations, 1003));
	}
	
	@Test
	public void getObs_shouldReturnAllObsIfTheAuthenticatedUserIsASuperUser() {
		assertTrue(Context.getAuthenticatedUser().isSuperUser());
		int expCount = 3;
		Collection<Obs> observations = getObservations();
		assertEquals(expCount, observations.size());
		assertTrue(TestUtil.containsId(observations, 1001));
		assertTrue(TestUtil.containsId(observations, 1002));
		assertTrue(TestUtil.containsId(observations, 1003));
	}
	
	@Test
	public void getObs_shouldReturnAllObsIfLocationFilteringIsDisabled() throws Exception {
		DataFilterTestUtils.disableLocationFiltering();
		reloginAs("dyorke", "test");
		assertEquals(3, getObservations().size());
	}
	
}
