package models;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import play.db.jpa.Model;

@Entity
public class BeneficiaryChangeLog extends Model {

	public String UID;
    public String fieldName;
    public String oldValue;
    public String newValue;
    
    //here created_at and updated_at are not default values set by Model
    //created_at = creation date of beneficiary
    //updated_at = when the beneficiary_info has been updated
    //public Date created_at;
    //public Date updated_at;
    
    public String updatedBy;


    
    
    @Override
    public String toString() {
        return "BeneficiaryChangeLog{" +
                "UID=" + UID +
                ", fieldName='" + fieldName + '\'' +
                ", oldValue='" + oldValue + '\'' +
                ", newValue='" + newValue + '\'' +
 //               ", createdAt='" + created_at + '\'' +
 //               ", updatedAt=" + updated_at +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
	
	
	
	
}
