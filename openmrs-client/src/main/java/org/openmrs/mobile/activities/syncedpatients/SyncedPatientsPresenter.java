/*
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

package org.openmrs.mobile.activities.syncedpatients;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.openmrs.mobile.R;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.models.retrofit.Patient;
import org.openmrs.mobile.utilities.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SyncedPatientsPresenter implements SyncedPatientsContract.Presenter {

    // View
    @NonNull
    private final SyncedPatientsContract.View mSyncedPatientsView;

    // Query for data filtering
    @Nullable
    private String mQuery;

    public SyncedPatientsPresenter(@NonNull SyncedPatientsContract.View syncedPatientsView) {
        mSyncedPatientsView = syncedPatientsView;
        mSyncedPatientsView.setPresenter(this);
    }

    /**
     * Used to display initial data on activity trigger
     */
    @Override
    public void start() {
        updateLocalPatientsList();
    }

    /**
     * Sets query used to filter (used by Activity's ActionBar)
     */
    @Override
    public void setQuery(String query) {
        mQuery = query;
    }

    /**
     * Used to update local patients list
     * It handles search events and replaces View's data to display
     */
    @Override
    public void updateLocalPatientsList() {
        List<Patient> patientList = new PatientDAO().getAllPatients();
        final int NO_STRING_ID = R.string.last_vitals_none_label;
        boolean isFiltering = StringUtils.notNull(mQuery);

        if (isFiltering) {
            patientList = getPatientsFilteredByQuery(patientList, mQuery);
            if (patientList.isEmpty()) {
                mSyncedPatientsView.updateListVisibility(false, R.string.search_patient_no_result_for_query, mQuery);
            }
            else {
                mSyncedPatientsView.updateListVisibility(true, NO_STRING_ID, null);
            }
        }
        else {
            if (patientList.isEmpty()) {
                mSyncedPatientsView.updateListVisibility(false, R.string.search_patient_no_results, null);
            }
            else {
                mSyncedPatientsView.updateListVisibility(true, NO_STRING_ID, null);
            }
        }
        mSyncedPatientsView.updateAdapter(patientList, isFiltering);
    }

    /**
     * Used to filter list by specified query
     * Its possible to filter patients by: Name, Surname (Family Name) or ID.
     * @param patientList list of patients to filter
     * @param query query that needs to be contained in Name, Surname or ID.
     * @return patient list filtered by query
     */
    private List<Patient> getPatientsFilteredByQuery(List<Patient> patientList, String query) {
        List<Patient> filteredList = new ArrayList<>();
        query = query.toLowerCase();

        for (Patient patient : patientList) {

            String patientName = patient.getPerson().getNames().get(0).getGivenName().toLowerCase();
            String patientSurname = patient.getPerson().getNames().get(0).getFamilyName().toLowerCase();
            String patientIdentifier = patient.getIdentifier().getIdentifier();

            boolean isPatientNameFitQuery = patientName.length() >= query.length() && patientName.substring(0,query.length()).equals(query);
            boolean isPatientSurnameFitQuery = patientSurname.length() >= query.length() && patientSurname.substring(0,query.length()).equals(query);
            boolean isPatientIdentifierFitQuery = false;
            if (patientIdentifier != null) {
                isPatientIdentifierFitQuery = patientIdentifier.length() >= query.length() && patientIdentifier.substring(0,query.length()).toLowerCase().equals(query);
            }
            if (isPatientNameFitQuery || isPatientSurnameFitQuery || isPatientIdentifierFitQuery) {
                filteredList.add(patient);
            }
        }
        return filteredList;
    }

}
