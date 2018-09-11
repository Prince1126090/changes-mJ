package controllers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.NamedNativeQueries;
import javax.persistence.Query;

import org.hibernate.Session;

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
import models.User;
import models.Woman;
import play.Logger;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import responses.ChildInfoResponse;
import responses.PregnantWomanInfoResponse;
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
	
/*	public static ArrayList<ArrayList<Long>> getOsrpTransferLogStat(Date fromDate, Date toDate) {
		
		
		String queryToGetCount =         "SELECT "+
										 "form.id as id, COUNT(*) as count "+
                                         "FROM "+
                                         "OpensrpTransferLog "+
                                         "WHERE "+
                                         "created_at >= ? "+
                                         "and created_at <= ? "+
                                         "GROUP BY form.id ";
		Query query = JPA.em().createQuery(queryToGetCount)
				.setParameter(1,fromDate)
				.setParameter(2,toDate);

		ArrayList<ArrayList<Long>> resultList = new ArrayList<ArrayList<Long>>();
		List<Object[]> result = query.getResultList();
		 for (int i=0; i<result.size(); i++){
		    Object[] row = (Object[]) result.get(i);
		    ArrayList<Long> rowList = new ArrayList<Long>();
		    rowList.add((long)row[0]);
		    rowList.add((long)row[1]);
		    resultList.add(rowList);
		 }
		 
		 return resultList;
	}
	
	
	public static void osrpTransferLog() {
		
		Calendar cal1 = Calendar.getInstance();
		cal1.add(Calendar.DATE, 1);
		Date tomorrow = cal1.getTime();
		Calendar cal2 = Calendar.getInstance();
		cal2.add(Calendar.MONTH, -1);
		Date oneMonthAgo = cal2.getTime();
		Date fromDate = oneMonthAgo;
		Date toDate = tomorrow;

		String fromDateStr = new SimpleDateFormat("MM/dd/yyyy").format(fromDate);
		String toDateStr = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
		ArrayList<ArrayList<Long>> resultList = getOsrpTransferLogStat(fromDate, toDate);
		render(resultList, fromDateStr, toDateStr);
	}
	
	
	public static void osrpTransferLogDateWise(String fromDate ,String toDate){
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
		
		//String fromDateStr = new SimpleDateFormat("MM/dd/yyyy").format(fromDate);
		//String toDateStr = new SimpleDateFormat("MM/dd/yyyy").format(toDate);
		ArrayList<ArrayList<Long>> resultList = getOsrpTransferLogStat(fromDateObject, toDateObject);
		renderJSON(resultList);
	}
	
	*/

	
	@ExternalRestrictions("Edit Beneficiary")
	public static void submit(@Valid Woman beneficiary, Woman bnew,ReasonToChangeBen reasonToChange, String remarks) {
		if (validation.hasErrors()) {
			//render("@edit", beneficiary);
			Logger.info("NOT A VALID WOMAN ::::: "+ beneficiary);
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
	public static void viewBeneficiary(String UID) {
		
		String UIDStr = UID.toString();
		/*	Beneficiary beneficiary = Beneficiary
				.find("select b from Beneficiary b "
					 +"Where DTYPE = 'Woman' "
					 +"and b.household IS NOT NULL "
					 +"and b.status != 'INACTIVE' "
					 +"and b.status != 'OUT_OF_JIVITA' "
					 +"and b.status != 'WOMAN_DIED' "
					 +"and b.status != 'DELETED' "
					 +"and b.UID = ? "
					 +"order by b.updated_at desc",idStr)
				.first();*/
		
		/*Beneficiary beneficiary = Beneficiary
				.find("select b from Beneficiary b "
					 +"Where DTYPE = 'Woman' "
					 +"and b.household IS NOT NULL "
					 +"and b.UID = ? "
					 +"order by b.updated_at desc",UIDStr)
				.first();*/
		
		 String findQuery= " SELECT a.* "
		    		+" FROM Beneficiary AS a, "
		    		+" ( "
		    		+" select UID, "
		    		+" max(updated_at) AS updated_at "
		    		+" FROM Beneficiary "
		    		+" Where DTYPE = 'Woman' AND "
		    		+" household_id IS NOT NULL AND "
		    		+" UID = ? "
		    		+" ) AS c "
		    		+" WHERE a.updated_at = c.updated_at "
		    		+" AND a.UID = c.UID "
		    		+" AND a.status NOT IN ('INACTIVE', "
					+"	'OUT_OF_JIVITA', "
	                +"  'WOMAN_DIED', "
	                +"  'DELETED') "
	                +" LIMIT 1";
		   
		    Query query = JPA.em().createNativeQuery(findQuery,  Beneficiary.class)
		    		.setParameter(1, UIDStr);
		    Beneficiary beneficiary= (Beneficiary) query.getSingleResult();
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

		/*List<BeneficiaryStatus> bStatus = new ArrayList<BeneficiaryStatus>(
				EnumSet.allOf(BeneficiaryStatus.class));
		ArrayList<String> stat = new ArrayList<String>();
		for (BeneficiaryStatus schdl : bStatus) {
			stat.add(schdl.getName());
		}*/
		
		
		List<ReasonToChangeBen> reasonList = new ArrayList<ReasonToChangeBen>(
				EnumSet.allOf(ReasonToChangeBen.class));

		render(beneficiary, reasonList);
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
				.find(" select b from Beneficiary b "
		             +" Where DTYPE = 'Woman' "
		             +" AND b.UID IS NOT NULL "
					 +" AND b.household IS NOT NULL "
		             +" AND b.status NOT IN ('INACTIVE', "
					 +"	'OUT_OF_JIVITA', "
		             +" 'WOMAN_DIED', "
		             +" 'DELETED') "
					 +"order by b.updated_at desc")
				.from(from_pos).fetch(total);
		ArrayList<Beneficiary> allBeneficiaries = new ArrayList<Beneficiary>();
		for (Beneficiary ben : beneficiaries) {
			
			//Logger.info("BENEFICIARY HOUSEHOLD %s", ben.household);
			
			allBeneficiaries.add(ben);
		}

		// renderJSON(allBeneficiaries);
		render(allBeneficiaries);
	}

	public static void searchBeneficiary(String searchInput, Integer from) {
		
		String s = "%" + searchInput + "%";
		int from_pos = (from >= 0) ? from : 0;
		
		/*List<Beneficiary> beneficiaries = Beneficiary
				.find("select b from Beneficiary b Where DTYPE = 'Woman' and "
						+" b.household IS NOT NULL and "
						+" b.status != 'INACTIVE' and"
						+" (b.UID like ? or" 
						+ " b.name like ? or"
						+" b.husbandName like ? or" 
						+" b.nID like ? or"
						+" b.bID like ? ) "
						+" order by b.updated_at desc", s, s, s, s,s)
				.from(from_pos).fetch(10);
		ArrayList<Beneficiary> allBeneficiaries = new ArrayList<Beneficiary>();
		for (Beneficiary ben : beneficiaries) {
			allBeneficiaries.add(ben);
		}*/

		
		/*Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	    String json = gson.toJson(allBeneficiaries);*/
	    

	    String findQuery= " SELECT a.* "
	    		+" FROM Beneficiary AS a, "
	    		+" ( "
	    		+" select UID, "
	    		+" max(updated_at) AS updated_at "
	    		+" FROM Beneficiary "
	    		+" Where DTYPE = 'Woman' AND "
	    		+" household_id IS NOT NULL AND "
	    		+" (UID LIKE ? OR "
	    		+" name LIKE ? OR "
	    		+" husbandName LIKE ? OR "
	    		+" nID LIKE ? OR "
	    		+" bID LIKE ? ) "
	    		+" GROUP BY UID " 
	    		+" ORDER BY updated_at DESC "
	    		+" LIMIT "
	    		+ from_pos
	    		+" ,10 "
	    		+" ) AS c "
	    		+" WHERE a.updated_at = c.updated_at "
	    		+" AND a.UID = c.UID "
	    		+" AND a.status NOT IN ('INACTIVE', "
				+"	'OUT_OF_JIVITA', "
                +"  'WOMAN_DIED', "
                +"  'DELETED') ";
	   
	    Query query = JPA.em().createNativeQuery(findQuery,  Beneficiary.class)
	    		.setParameter(1, s)
	    		.setParameter(2, s)
	    		.setParameter(3, s)
	    		.setParameter(4, s)
	    		.setParameter(5, s);
	    List<Beneficiary> resultList = query.getResultList();
	   // Logger.info("newly fetched list >>>>>"+ resultList);
	    ArrayList<Beneficiary> allCBeneficiaries = new ArrayList<Beneficiary>();
		for (Beneficiary ben : resultList) {
			allCBeneficiaries.add(ben);
			
		} 
		
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	    String json = gson.toJson(allCBeneficiaries);

		renderJSON(json);
	}

/*	public static <T> List<T> map(Class<T> type, List<Object[]> records){
		   List<T> result = new LinkedList<>();
		   for(Object[] record : records){
		      result.add(map(type, record));
		   }
		   return result;
		}
	
	public static <T> List<T> getResultList(Query query, Class<T> type){
		  @SuppressWarnings("unchecked")
		  List<Object[]> records = query.getResultList();
		  return map(type, records);
		}*/
	
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
        //query changed for MJIV-133
        Woman woman = Woman.find("Select s from Beneficiary s where UID=? order by id desc", previousUid).first();
        Sector sector = Sector.find("bySECTORID", "777").first();

        List<Schedule> activeScheduleList = new ArrayList<>();
        List<Schedule> donePMVScheduleList = new ArrayList<>();
        WomanInfoResponse womanresponse = null;
        Form formToGenrate = Form.findById(Long.parseLong("8"));

        if (woman != null) {
            Logger.debug("existing woman: " + woman);
            activeScheduleList = Schedule.find("select sh from Schedule sh  where sh.formToGenerate=? "
                    + "and sh.status=? and sh.beneficiary = ?",
                    formToGenrate, ScheduleStatus.ACTIVE, (Beneficiary) woman).fetch();
            donePMVScheduleList = Schedule.find("select sh from Schedule sh  where sh.formToGenerate=? "
                    + "and sh.status=? and sh.beneficiary = ?",
                    formToGenrate, ScheduleStatus.DONE, (Beneficiary) woman).fetch();

            /*During MJIV-89 it was decided that
            sector-777 will be not be restricted during NWVM even if there is no PMV reported*/
            if ((donePMVScheduleList != null && !donePMVScheduleList.isEmpty())
                    || (sector.id == woman.enrolledSector.id)) {
                womanresponse = new WomanInfoResponse(woman);
                womanresponse.pmvStatus = "0";

                Logger.info("pmv completed found or sector id 777 for beneficiary uid: " + previousUid
                        + " ,beneficiary_id:" + ((Beneficiary) woman).id + " ,pmvstatus:" + womanresponse.pmvStatus);

                renderJSON(womanresponse);
            } else if (activeScheduleList != null && !activeScheduleList.isEmpty()) {
                womanresponse = new WomanInfoResponse(woman);
                womanresponse.pmvStatus = "1";

                Logger.info("active pmv found for beneficiary uid: " + previousUid
                        + " ,beneficiary_id:" + ((Beneficiary) woman).id + " ,pmvstatus:" + womanresponse.pmvStatus);

                renderJSON(womanresponse);
            } else {
                womanresponse = new WomanInfoResponse(woman);
                womanresponse.pmvStatus = "2";

                Logger.info("No pmv scheduled found for beneficiary uid: " + previousUid
                        + " ,beneficiary_id:" + ((Beneficiary) woman).id + " ,pmvstatus:" + womanresponse.pmvStatus);

                renderJSON(womanresponse);
            }
        } else {
            womanresponse = new WomanInfoResponse(woman);
            womanresponse.pmvStatus = "0";

            Logger.info("No similar woman found for beneficiary uid: " + previousUid
                    + " ,beneficiary_id:" + ((Beneficiary) woman).id + " ,pmvstatus:" + womanresponse.pmvStatus);
            renderJSON(womanresponse);
        }
    }

    @Unrestricted
    @Mobile
    public static void getPregnantWomenList() {
        User apiUser = User.findByName(session.get("apiUser"));
        //User apiUser = User.findByName("6666");
        Logger.info("apiUser id: " + apiUser.id);
        // Query for finding active schedules
        String search_query = "SELECT b.name as name, b.husbandName as husName, b.UID as uid "
                + ", h.householdId as hhid, b.age as age, b.lmp as lmp "
                + ", b.bID as brid, b.nID as nid "
                + " FROM Beneficiary b "
                + " JOIN Household h "
                + " ON h.id = b.household_id "
                + " JOIN Schedule s "
                + " ON s.beneficiary_id = b.id "
                + " WHERE s.status ='ACTIVE' "
                + " and s.formToGenerate_id = 32 "
                + " and b.user_id = " + apiUser.id
                + " order by h.householdId ";

        renderPregnantWomanList(search_query);
    }

    @Unrestricted
    @Mobile
    public static void getPregnantWoman(String beneficiary_uid) {
        User apiUser = User.findByName(session.get("apiUser"));
        //User apiUser = User.findByName("6666");
        // Query for finding active schedules
        String search_query = "SELECT b.name as name, b.husbandName as husName, b.UID as uid "
                + ", h.householdId as hhid, b.age as age, b.lmp as lmp "
                + ", b.bID as brid, b.nID as nid "
                + " FROM Beneficiary b "
                + " JOIN Household h "
                + " ON h.id = b.household_id "
                + " JOIN Schedule s "
                + " ON s.beneficiary_id = b.id "
                + " WHERE s.status ='ACTIVE' "
                + " and s.formToGenerate_id = 32 "
                + " and b.UID = " + beneficiary_uid
                + " and b.user_id = " + apiUser.id
                + " order by h.householdId ";

        renderPregnantWomanList(search_query);
    }

    private static void renderPregnantWomanList(String search_query) {
        List<PregnantWomanInfoResponse> pregnantWomanInfoResponseList = new ArrayList<PregnantWomanInfoResponse>();
        ResultSet rs = null;
        Statement stmt = null;
        try {
            Connection conn = play.db.DB.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(search_query);

            while (rs.next()) {
                String name = rs.getString("name");
                String husName = rs.getString("husName");
                String uid = rs.getString("uid");
                String hhid = rs.getString("hhid");
                String age = rs.getString("age");
                String lmp = rs.getString("lmp");
                String brid = rs.getString("brid");
                String nid = rs.getString("nid");

                //Logger.debug("name: "+ name + ", hhid: " + hhid + "uid: "+ uid + ", husName: " + husName
                //      + "lmp: "+ lmp + ", age: " + age + "brid: "+ brid + ", nid: " + nid);
                PregnantWomanInfoResponse pregnantWomanInfoResponse = new PregnantWomanInfoResponse(name, husName
                        ,uid, hhid, age, lmp, brid, nid);
                pregnantWomanInfoResponseList.add(pregnantWomanInfoResponse);
            }
        } catch (SQLException e) {
                e.printStackTrace();
            }
        renderJSON(pregnantWomanInfoResponseList);
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
