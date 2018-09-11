/*
 * Copyright (C) 2012 mPower Social
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package models;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.joda.time.DateTime;

import com.google.gson.annotations.Expose;
import com.jamonapi.utils.Logger;

/**
 * Woman Class -.
 */
@Entity
public class Woman extends Beneficiary {

    /**
     * Husband name of Woman
     */
	@Expose
    public String husbandName;
    /**
     * Mobile Number of Woman
     */

    public String mobileNumber;

    /**
     * Children of Woman
     */
    @OneToMany(mappedBy = "mother", cascade = CascadeType.ALL)
    public List<Child> children;

    @ManyToOne
    public Sector enrolledSector;

    public String mauza;

    public Integer vg;
    public Integer hr;
    public Integer hrp;
    public Integer flagValue;
    public Integer dangerValue;
    public Integer sortValue;


    /**
     * LMP of Woman
     */
    @Expose
    public Date lmp;
    /**
     * EDD of Woman
     */
    public Date edd;
    /**
     * Current Round of Woman
     */
    @OneToOne
    public Round round;


    public Woman(String id, String name) {
        this.UID = id;
        this.name = name;
    }

    public static Woman getNewNullInstance() {
        return new Woman("-1", "-1");
    }

    public Woman(String id, String name, String husbandName) {
        this(id, name);
        this.husbandName = husbandName;
    }

    public Woman(String id, Household household, String name, String husbandName) {
        this(id, name, husbandName);
        this.household = household;
    }

    public Woman(String id, Household household, String name, String husbandName, User user) {
        this(id, household, name, husbandName);
        this.user = user;
    }

    public Woman(String id, Household household, String name, String husbandName, User user, String mobileNumber) {
        this(id, household, name, husbandName, user);
        this.mobileNumber = mobileNumber;
    }

    public Woman(String id, Household household, String name, byte[] image) {
        this.UID = id;
        this.household = household;
        this.name = name;
        this.image = image;
        this.status = BeneficiaryStatus.ACTIVE;
    }

    public Woman(String id, Household household, String name, String husbandName, User user, Sector sector) {
        this(id, household, name, husbandName, user);
        this.enrolledSector = sector;
        //this.round = Round.findCurrentRound(new DateTime().toDate());
        this.round = null;
        this.pmStatus = 0;
        this.status = BeneficiaryStatus.ACTIVE;
    }

    public Woman(String sectorID, String mauza, String UserID, String householdID, String benificaryUID, String womanName, String husbandName, String womanAge) {

        this.user = User.findByName(UserID);
        this.enrolledSector = Sector.find("bySectorId", sectorID).first();
        this.household = Household.getHouseholdByHHIdAndSectorId(householdID, sectorID);
        this.UID = benificaryUID;
        this.name = womanName;
        this.husbandName = husbandName;
        this.age = Integer.parseInt(womanAge);
        this.mauza = mauza;
        this.round = null;
        //this.round = Round.findCurrentRound(new DateTime().toDate());
        this.pmStatus = 0;
        this.status = BeneficiaryStatus.ACTIVE;

    }


    @Override
    public String toString() {
        return "Woman{" +
                "husbandName='" + husbandName + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", children=" + children +
                ", enrolledSector=" + enrolledSector +
                ", mauza='" + mauza + '\'' +
                ", vg=" + vg +
                ", hr=" + hr +
                ", hrp=" + hrp +
                ", flagValue=" + flagValue +
                ", dangerValue=" + dangerValue +
                ", sortValue=" + sortValue +
                ", lmp=" + lmp +
                ", edd=" + edd +
                ", round=" + round +
                '}';
    }
}
