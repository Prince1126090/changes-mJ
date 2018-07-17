package controllers;

import java.text.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.Query;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import models.Beneficiary;
import models.BeneficiaryChangeLog;
import models.BeneficiaryStatus;
import models.Child;
import models.Event;
import models.Form;
import models.Household;
import models.NewChildUIDList;
import models.NewMWRAUIDList;
import models.ReasonToChangeBen;
import models.Schedule;
import models.ScheduleStatus;
import models.UnitData;
import models.Woman;
import play.Logger;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import responses.ChildInfoResponse;
import responses.WomanInfoResponse;
import annotations.Mobile;

import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.ExternalRestrictions;
import controllers.deadbolt.Unrestricted;

//from APIS
import jobs.ExtractData;
import models.*;
import play.Logger;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.With;
import utils.CheckTransmission;
import utils.CheckTransmissionV2;
import utils.ScheduleFactory;
import static utils.JpaCommit.JpaCommit;

@With(Deadbolt.class)
public class Beneficiaries extends Controller {
	
	//form apis
	
	
	@ExternalRestrictions("View BeneficiaryChangeLog")
	public static void beneficiaryChangeLogDateWise(String fromDate ,String toDate, Integer from){
		DateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		String fromDateString = fromDate;
		String toDateString = toDate;
		Date fromDateObject = null;
		Date toDateObject = null;
		try {
			 fromDateObject = sdf.parse(fromDateString);
			 toDateObject = sdf.parse(toDateString);
// increment the date by one day			 
			 GregorianCalendar cal = new GregorianCalendar();
			 cal.setTime(toDateObject);
			 cal.add(Calendar.DATE, 1);		
			 toDateObject= cal.getTime();
			 
			 
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int from_pos = from;
		String queryToGetCount =         "SELECT "+
										 "b "+
                                         "FROM "+
                                         "BeneficiaryChangeLog b "+
                                         "WHERE "+
                                         "b.created_at >= ? "+
                                         "and b.created_at <= ? "+
                                         "order by b.created_at desc ";
                                        // +"limit "+from_pos+",3";
		Query query = JPA.em().createQuery(queryToGetCount)
				.setParameter(1,fromDateObject)
				.setParameter(2,toDateObject)
				.setFirstResult(from_pos)
                .setMaxResults(10) ;
		List <BeneficiaryChangeLog> result = query.getResultList();
		ArrayList<BeneficiaryChangeLog> bCLArrayList = new ArrayList<BeneficiaryChangeLog>();
		for (BeneficiaryChangeLog b : result) {
			bCLArrayList.add(b);
		}
		
		renderJSON(bCLArrayList);
	}
	
	@ExternalRestrictions("View BeneficiaryChangeLog")
	public static void beneficiaryChangeLog(Integer from, Integer count){
		int from_pos= from;
		int count_val = count;
		List <BeneficiaryChangeLog> result = BeneficiaryChangeLog.find("order by created_at desc").from(from_pos).fetch(count_val);
		ArrayList<BeneficiaryChangeLog> bCLArrayList = new ArrayList<BeneficiaryChangeLog>();
		for (BeneficiaryChangeLog b : result) {
			bCLArrayList.add(b);
		}
		render(bCLArrayList);
	}
	
	
	
	public static void osrpTransferLog() {

		/*List<OpensrpTransferLog> opensrpTransferLog = OpensrpTransferLog.findAll();
		ArrayList<OpensrpTransferLog> osrpTransferLog = new ArrayList<OpensrpTransferLog>();
		for (OpensrpTransferLog osrptl : opensrpTransferLog) {
			osrpTransferLog.add(osrptl);
		}
		renderJSON(osrpTransferLog);*/
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String fromDateString = "2018-6-25";
		//String toDateString = fromDateString;
		String toDateString = "2018-6-27";
		Date fromDateObject = null;
		Date toDateObject = null;
		try {
			 fromDateObject = sdf.parse(fromDateString);
			 toDateObject = sdf.parse(toDateString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Date fromDate = new Date();
		Date fromDate = fromDateObject;
		Date toDate = toDateObject;
		String queryToGetCount =         "SELECT "+
										 "form.id, COUNT(*) "+
                                         "FROM "+
                                         "OpensrpTransferLog "+
                                         "WHERE "+
                                         "created_at > ? "+
                                         "and created_at < ? "+
                                         "GROUP BY form.id ";
		Query query = JPA.em().createQuery(queryToGetCount)
				.setParameter(1,fromDate)
				.setParameter(2,toDate);
		//Query query = JPA.em().createQuery(queryToGetCount);
		List result = query.getResultList();
		
		
		//long count = OpensrpTransferLog.count();
		//renderJSON(result);
		render(result);


	}
	
	
	
	@ExternalRestrictions("Edit Beneficiary")
	public static void submit(@Valid Woman beneficiary, Woman bnew,ReasonToChangeBen reasonToChange, String remarks) {
		if (validation.hasErrors()) {
			render("@edit", beneficiary);
		}

		String old_name = beneficiary.name;
		String new_name = bnew.name;
		if (new_name != null && !new_name.isEmpty()) {

			beneficiary.name = new_name;
			int i = insertBCLog(beneficiary, "name", old_name, new_name,reasonToChange,remarks);

		}

		String old_hb_name = beneficiary.husbandName;
		String new_hb_name = bnew.husbandName;
		if (new_hb_name != null && !new_hb_name.isEmpty()) {
			beneficiary.husbandName = new_hb_name;
			int j = insertBCLog(beneficiary, "husbandName", old_hb_name,
					new_hb_name,reasonToChange,remarks);
		}

		String old_nID = beneficiary.nID;
		String new_nID = bnew.nID;
		if (new_nID != null && !new_nID.isEmpty()) {
			beneficiary.nID = new_nID;
			int k = insertBCLog(beneficiary, "nID", old_nID, new_nID,reasonToChange,remarks);
		}
		
		String old_bID = beneficiary.bID;
		String new_bID = bnew.bID;
		if (new_bID != null && !new_bID.isEmpty()) {
			beneficiary.bID = new_bID;
			int l = insertBCLog(beneficiary, "bID", old_bID, new_bID,reasonToChange,remarks);
		}
		
		String old_age = beneficiary.age.toString();
		if(bnew.age != null){
			String new_age = bnew.age.toString();
			int n_age = bnew.age;
			if (new_age != null && !new_age.isEmpty()) {
				beneficiary.age = n_age;
				int m = insertBCLog(beneficiary, "age", old_age, new_age,reasonToChange,remarks);
			}
		}
		
		
		String old_mbNum= beneficiary.mobileNumber;
		String new_mbNum = bnew.mobileNumber;
		if (new_mbNum != null && !new_mbNum.isEmpty()) {
			beneficiary.mobileNumber = new_mbNum;
			int n = insertBCLog(beneficiary, "mobileNumber", old_mbNum, new_mbNum,reasonToChange,remarks);
		}

		beneficiary.save();
		//flash.success("Record saved successfully.");
		listBeneficiary(0, 10);
	}

	public static int insertBCLog(Woman beneficiary, String fieldName,
			String old_value, String new_value,ReasonToChangeBen reason, String remarks) {

		// DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		/*
		Date upadateDate = new Date();

		String queryToGetCreatedAtDate = "SELECT created_at FROM Beneficiary where id=?";
		Query query = JPA.em().createQuery(queryToGetCreatedAtDate)
				.setParameter(1, beneficiary.id);
		List result = query.getResultList();
		Date createDate = (Date) result.get(0);
		*/

		BeneficiaryChangeLog bCLog = new BeneficiaryChangeLog();
		bCLog.UID = beneficiary.UID;
		bCLog.fieldName = fieldName;
		bCLog.oldValue = old_value;
		bCLog.newValue = new_value;
		bCLog.reason = reason.name();
		bCLog.remarks = remarks;
		
		//bCLog.created_at = createDate;
		//bCLog.updated_at = upadateDate;
		String user;
		if (session.contains("username")) {
			user = session.get("username");
		} else {
			user = "not set yet";
		}
		bCLog.updatedBy = user;

		bCLog.save();
		return 1;
	}

	@ExternalRestrictions("Edit Beneficiary")
	public static void viewBeneficiary(Long UID) {

		Beneficiary beneficiary = Beneficiary.find("byUID",UID).first();
		
		/*long id= beneficiary.id;
		
		List<Schedule> completeSchedules = Schedule
				.find("select s from Schedule s Where beneficiary_id = ? and status='DONE'",
						id).fetch();
		ArrayList<Schedule> allCompleteSchedules = new ArrayList<Schedule>();
		for (Schedule schdl : completeSchedules) {
			allCompleteSchedules.add(schdl);
		}

		List<Schedule> dueSchedules = Schedule
				.find("select s from Schedule s Where beneficiary_id = ? and status='ACTIVE' ",
						id).fetch();
		ArrayList<Schedule> allDueSchedules = new ArrayList<Schedule>();
		for (Schedule schdl : dueSchedules) {
			allDueSchedules.add(schdl);
		}*/

		List<BeneficiaryStatus> bStatus = new ArrayList<BeneficiaryStatus>(
				EnumSet.allOf(BeneficiaryStatus.class));
		ArrayList<String> stat = new ArrayList<String>();
		for (BeneficiaryStatus schdl : bStatus) {
			stat.add(schdl.getName());
		}
		
		
		List<ReasonToChangeBen> reasonList = new ArrayList<ReasonToChangeBen>(
				EnumSet.allOf(ReasonToChangeBen.class));

		render(beneficiary, bStatus, stat, reasonList);
		//render(beneficiary, allCompleteSchedules, allDueSchedules, bStatus,stat);
		// renderJSON(beneficiary);

	}

	public static void listBeneficiary(Integer from, Integer count) {

		// List <String> beneficiaries =
		// Beneficiary.find("select b.name from Beneficiary b order by b.updated_at desc").fetch(10);
		// ArrayList<String> allBeneficiaries = new ArrayList<String>();
		// for (String name : beneficiaries) {
		// allBeneficiaries.add(name);
		// }
		int from_pos = (from >= 0) ? from : 0;
		int total = (count > 0) ? count : 10;
		List<Beneficiary> beneficiaries = Beneficiary
				.find("select b from Beneficiary b Where DTYPE = 'Woman' order by b.updated_at desc")
				.from(from_pos).fetch(total);
		ArrayList<Beneficiary> allBeneficiaries = new ArrayList<Beneficiary>();
		for (Beneficiary ben : beneficiaries) {
			allBeneficiaries.add(ben);
		}

		// renderJSON(allBeneficiaries);
		render(allBeneficiaries);
	}

	public static void searchBeneficiary(String searchInput, Integer from) {
		
		String s = "%" + searchInput + "%";
		int from_pos = (from >= 0) ? from : 0;
		// List <Beneficiary> beneficiaries = Beneficiary.find("byUIDLike",
		// s).fetch(10);
		List<Beneficiary> beneficiaries = Beneficiary
				.find("select b from Beneficiary b Where DTYPE = 'Woman' and "
						+ "(b.UID like ? or" + " b.name like ? or"
						+ " b.husbandName like ? or" + " b.nID like ? ) "
						+ "order by b.updated_at desc", s, s, s, s)
				.from(from_pos).fetch(10);
		ArrayList<Beneficiary> allBeneficiaries = new ArrayList<Beneficiary>();
		for (Beneficiary ben : beneficiaries) {
			allBeneficiaries.add(ben);
		}
		
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	    String json = gson.toJson(allBeneficiaries);

		renderJSON(json);
		//render(allBeneficiaries);
	}

	
	
	//end : from apis
	
	
	
	
	
	
	
	

    /* Women */
    @ExternalRestrictions("Edit User")
    public static void womanList() {
        List<Woman> women = Woman.findAll();
        render(women);
    }

    @ExternalRestrictions("Edit User")
    public static void womanCreate() {
        render("@womanEdit");
    }

    @ExternalRestrictions("Edit User")
    public static void womanEdit(Long id) {
        Woman woman = Woman.findById(id);
        notFoundIfNull(woman, "woman not found");
        render(woman);
    }

    @ExternalRestrictions("Edit User")
    public static void womanSubmit(@Valid Woman woman) {
        if (validation.hasErrors()) {
            render("@womanEdit", woman);
        }
        woman.save();
        womanList();
    }

    @ExternalRestrictions("View User")
    public static void details(Long id) {
        Woman woman = Woman.findById(id);
        notFoundIfNull(woman, "user not found");

        List<Event> events;
        if (woman.events.size() > 0) {
            events = Event.find("SELECT u FROM Woman AS u WHERE u NOT IN (:ids) ORDER BY u.cId ASC").bind("ids", woman.events).fetch();
        } else {
            events = Event.find("ORDER BY cId ASC").fetch();
        }
        render(woman, events);
    }

    @ExternalRestrictions("Edit User")
    public static void assignEvent(Long womanId, Long[] events, Boolean remove) {
        notFoundIfNull(womanId, "No userId provided");
        if (events.length > 0) {
            Woman woman = Woman.findById(womanId);
            notFoundIfNull(woman, "ben not found");
            for (Long eventId : events) {
                Event event = Event.findById(eventId);
                if (remove) {
                    woman.events.remove(event);
                } else {
                    //woman.events.put(event, new Date(0));
                }
            }
            woman.save();
        }
        ok();
    }


    @ExternalRestrictions("Edit User")
    public static void womanDelete(Long id) {
        if (request.isAjax()) {
            notFoundIfNull(id, "id not provided");
            Woman woman = Woman.findById(id);
            notFoundIfNull(woman, "woman not found");
            woman.delete();
            ok();
        }
    }
    
    /*Search Beneficiary by HouseholdId */

    @ExternalRestrictions("Edit User")
    public static void loadWomanUIDList(Long id) {
        if (request.isAjax()) {
            notFoundIfNull(id, "Household Id not provided");
            Household household = Household.findById(id);
            List<Beneficiary> beneficiaries = Beneficiary.find("householdId = ?", Integer.parseInt("1234")).fetch();
            Logger.info(beneficiaries.get(0).UID);
            notFoundIfNull(beneficiaries, "Woman not found");
            render(beneficiaries);
        }
    }

    /**
     * Mobile end api
     */
    
    /*Return a New Woman UID*/
    @Unrestricted
    @Mobile
    public static void getNewUIDForWoman() {

//    	NewMWRAUIDList MWRAnewUID = NewMWRAUIDList.find("byIsUsed", false).first();
//    	renderJSON(MWRAnewUID.newUID);


        //////////////////////////new mods julkar bhai/////////////////////
        NewMWRAUIDList MWRAnewUID = NewMWRAUIDList.find("byIsUsed", false).first();
        if (MWRAnewUID != null) {
            MWRAnewUID.isUsed = true;
            //Logger.info("Status" + MWRAnewUID.pmStatus);
            MWRAnewUID.save();
        }
        renderJSON(MWRAnewUID.newUID);
    }

    /*Return Existing Woman by UID*/
    @Unrestricted
    @Mobile
    public static void getExistingWoman(String previousUid) {
        Woman woman = Woman.find("byUID", previousUid).first();
        //List<Child> children = woman.children;
        List<Schedule> scheduleList = new ArrayList<>();
        WomanInfoResponse womanresponse = null;
        Form formToGenrate = Form.findById(Long.parseLong("8"));
        if (woman != null) {
            scheduleList = Schedule.find("select sh from Schedule sh  where sh.formToGenerate=? and sh.status=? and sh.beneficiary = ?",
                    formToGenrate, ScheduleStatus.ACTIVE, (Beneficiary) woman).fetch();
            if (!scheduleList.isEmpty()) {
                womanresponse = new WomanInfoResponse(woman);
                womanresponse.pmvStatus = "1";
                Logger.info("active pmv found beneficiary uid: " + previousUid + " ,beneficiary_id:" + ((Beneficiary) woman).id + " ,pmvstatus:" + womanresponse.pmvStatus);
                renderJSON(womanresponse);
            } else {
                womanresponse = new WomanInfoResponse(woman);
                womanresponse.pmvStatus = "0";
                Logger.info("No pmv found beneficiary uid: " + previousUid + " ,beneficiary_id:" + ((Beneficiary) woman).id + " ,pmvstatus:" + womanresponse.pmvStatus);
                renderJSON(womanresponse);
            }
        } else {
            womanresponse = new WomanInfoResponse(woman);
            womanresponse.pmvStatus = "0";
            Logger.info("No pmv found beneficiary uid: " + previousUid + " ,beneficiary_id:" + ((Beneficiary) woman).id + " ,pmvstatus:" + womanresponse.pmvStatus);
            renderJSON(womanresponse);
        }


    }

    /*Return a New Child UID*/
    @Unrestricted
    @Mobile
    public static void getNewUIDForChild() {

        NewChildUIDList ChildnewUID = NewChildUIDList.find("byIsUsed", false).first();
        renderJSON(ChildnewUID.newChildUID);

    }

    @Unrestricted
    @Mobile
    public static void mGetChildInfo(Long id) {

        Schedule schedule = Schedule.findById(id);
        notFoundIfNull(schedule);
        String titleVar = null;
        String titleVar2 = null;

        if (schedule.data.form.formId == 4) {
            titleVar = "PDR_CHILDNAME";
            titleVar2 = "PDR_CHILDSEX";
        }
        if (schedule.data.form.formId == 5) {
            titleVar = "PVR_CHILDNAME";
            titleVar2 = "PVR_CHILDSEX";
        }

        UnitData unitData = UnitData.find("SELECT u FROM UnitData u WHERE data = ? and titleVar = ? ", schedule.data, titleVar).first();
        String childName = unitData.valueVar;

        unitData = UnitData.find("SELECT u FROM UnitData u WHERE data = ? and titleVar = ? ", schedule.data, titleVar2).first();
        String childSex = unitData.valueVar;

        NewChildUIDList ChildnewUID = NewChildUIDList.find("byIsUsed", false).first();
        String childUID = ChildnewUID.newChildUID;
        ChildnewUID.isUsed = true;
        ChildnewUID.save();

        Beneficiary beneficiary = Beneficiary.findById(schedule.beneficiary.id);
        Woman mother = (Woman) beneficiary;

        new Child(childUID, childName, Integer.parseInt(childSex), mother).save();

        renderJSON(new ChildInfoResponse(childName, childUID));
    }

}
