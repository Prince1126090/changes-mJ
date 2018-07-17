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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyClass;
import javax.persistence.Transient;

import com.google.gson.annotations.Expose;

import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.Model;
import play.modules.chronostamp.NoChronostamp;
import utils.Observer;
import utils.Subject;

@Entity
@NoChronostamp
public abstract class Beneficiary extends Model implements Subject {

    @Transient
    private List<Observer> observers = new ArrayList<Observer>();

    @Enumerated(EnumType.STRING)
    public BeneficiaryType type;

    @ManyToOne
    public User user;

    /**
     * The Jivita id.
     */
    @Expose
    @Unique
    public String UID;

    /**
     * The name.
     */
    @Expose
    public String name;
    /**
     * Age of Beneficiary
     */
    @Expose
    public Integer age;

    public Sex sex;

    public String gobHHID;
    @Expose
    public String nID;
    public String bID;

    public Boolean isMigratedToOpenSRP;

    public String idForOpensrp;

    public Long lastMigratedFormId;

    @ManyToOne(cascade = {CascadeType.ALL})
    public Household household;

    @Enumerated(EnumType.STRING)
    public BeneficiaryStatus status;

    @Column(columnDefinition = "integer default 0")
    public Integer pmStatus;

    public byte[] image;

    public Date doo;

    @ElementCollection(targetClass = Date.class)
    @MapKeyClass(Event.class)
    public Map<Event, Date> events = new HashMap<Event, Date>();


    @Override
    public String toString() {
        return "" + this.UID;
    }


    @Override
    public void attach(Observer o) {
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    @Override
    public void detach(Observer o) {
        if (!observers.contains(o)) {
            observers.remove(o);
        }
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.update(this);
        }
    }


}
