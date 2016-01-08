package org.cyanogenmod.changelog;

/*
 * Copyright (c) 2016.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Model class to use as the data source for the Adapter
 */
public class Change {

    private String subject;
    private String project;
    private String lastUpdate;
    private String changeId;

    /**
     * Constructs a new Change.
     * @param subject the subject of the Change
     * @param project the project package affected by the Change
     * @param lastUpdate last update date of the Change
     * @param id id of the Change
     */
    public Change(String subject, String project, String lastUpdate, String id) {
        this.subject = subject;
        this.project = project;
        this.lastUpdate = lastUpdate;
        this.changeId = id;
    }

    public String getSubject() {
        return subject;
    }

    public String getProject() {
        return project;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public String getChangeId() {
        return changeId;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setChangeId(String changeId) {
        this.changeId = changeId;
    }
}