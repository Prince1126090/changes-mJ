package models;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.Size;

import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.data.validation.Unique;
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
    //@Required
    //validation is set on js
    public String reason;
    // @Size(min = 0, max = 10)
    //validation is set on js
    public String remarks;


    
    
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
