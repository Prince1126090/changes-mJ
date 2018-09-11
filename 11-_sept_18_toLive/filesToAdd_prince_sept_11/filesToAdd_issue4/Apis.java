package controllers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.joda.time.DateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import jobs.ExtractData;
import models.*;
import play.Logger;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.mvc.Controller;
import responses.ScheduleResponse;
import utils.CheckTransmission;
import utils.CheckTransmissionV2;
import utils.CommonUtil;
import utils.ScheduleFactory;

import static utils.JpaCommit.JpaCommit;

public class Apis extends Controller {
	
	
	public static void activeScheduleForHH(String householdId) {
		Logger.debug(" check active schedule for householdId: " + householdId);
		
		Map <String, Object> result= new HashMap<String, Object>();
		result.put("isActive", 0);
		result.put("response", "has no active schedule");
		
		User apiUser = User.findByName(session.get("apiUser"));
    	//User apiUser = User.findByName("6666");
    	if(apiUser == null){
    		Logger.debug("user not found");
    		
    		result.put("isActive", -1);
    		result.put("response", "user not found");
    		renderJSON(result);
    	}
    	Logger.debug(" apiUser: " + apiUser);
		
        Sector currentSector = apiUser.sectors.iterator().next();
        if(currentSector == null){
        	Logger.debug("sector not found");
        	
        	result.put("isActive", -2);
    		result.put("response", "sector not found");
    		renderJSON(result);
        }
        Logger.debug(" currentSector: " + currentSector);

        Household household = Household.find("select h from Household h where h.householdId = ? and h.sector = ? and (h.status = ? or h.status = ?)",
                householdId, currentSector, HouseholdStatus.NEW, HouseholdStatus.OLD).first();
        if(household == null){
        	Logger.debug("household not found");
        	
        	result.put("isActive", -3);
    		result.put("response", "household not found");
    		renderJSON(result);
        }
        Logger.debug(" received householdID: " + householdId + " -db id- " + household.id + " -sector id- " + currentSector.id);

	    List<Schedule> scheduleList = Schedule.find("Select s from Schedule s where s.status = ? and s.household = ? ",
	    	 ScheduleStatus.ACTIVE, household).fetch();
	    
	    Logger.debug(" active schedules:" + scheduleList.size());
	   
	    if(scheduleList.size() > 0){
	    	result.put("isActive", 1);
    		result.put("response", "has active schedule");
	    }

		renderJSON(result);

	}
	
	
	public static void activeScheduleForHHAndSector(String householdId, String sectorId) {
		Logger.debug(" check active schedule for householdId: " + householdId +" & sectorId: " + sectorId);
		
		Map <String, Object> result= new HashMap<String, Object>();
		result.put("isActive", 0);
		result.put("response", "has no active schedule");
		
		User apiUser = User.findByName(session.get("apiUser"));
    	//User apiUser = User.findByName("6666");
    	if(apiUser == null){
    		Logger.debug("user not found");
    		
    		result.put("isActive", -1);
    		result.put("response", "user not found");
    		renderJSON(result);
    	}
		
    	Sector currentSector = Sector.find("bySectorID",sectorId).first();
        if(currentSector == null){
        	Logger.debug("sector not found");
        	
        	result.put("isActive", -2);
    		result.put("response", "sector not found");
    		renderJSON(result);
        }
        Logger.debug(" currentSector: " + currentSector);

        Household household = Household.find("select h from Household h where h.householdId = ? and h.sector = ? and (h.status = ? or h.status = ?)",
                householdId, currentSector, HouseholdStatus.NEW, HouseholdStatus.OLD).first();
        if(household == null){
        	Logger.debug("household not found");
        	
        	result.put("isActive", -3);
    		result.put("response", "household not found");
    		renderJSON(result);
        }
        Logger.debug(" received householdID: " + householdId + " -db id- " + household.id + " -sector id- " + currentSector);

	    List<Schedule> scheduleList = Schedule.find("Select s from Schedule s where s.status = ? and s.household = ? ",
	    		ScheduleStatus.ACTIVE, household).fetch();
	    
	    Logger.debug(" active schedules:" + scheduleList.size());
	   
	    if(scheduleList.size() > 0){
	    	result.put("isActive", 1);
    		result.put("response", "has active schedule");
	    }
	    
	  		renderJSON(result);

	}
	
	

	public static void getBeneficiary() {

		List<Beneficiary> beneficiaries = Beneficiary.findAll();
		ArrayList<Beneficiary> allBeneficiaries = new ArrayList<Beneficiary>();

		for (Beneficiary beneficiary : beneficiaries) {
			allBeneficiaries.add(beneficiary);
		}

		renderJSON(allBeneficiaries);

	}

	public static void getHousehold() {

		List<Household> households = Household.findAll();
		ArrayList<Household> allHousehold = new ArrayList<Household>();

		for (Household household : households) {
			allHousehold.add(household);
		}

		renderJSON(allHousehold);

	}

	public static void getHousehold(String beneficiaryUId) {
		Beneficiary ben = Beneficiary.find(
				"select b from Beneficiary b where UID = ?", beneficiaryUId)
				.first();
		ArrayList<Beneficiary> beneficiary = new ArrayList<Beneficiary>();
		beneficiary.add(ben);
		renderJSON(beneficiary);
	}

	public static String testDocument(long id) {
		Schedule schedule = Schedule.findById(id);
		/*
		 * Data data = schedule.data; Logger.debug(schedule.toString()); try {
		 * ScheduleFactory.createSchedule(schedule, data); } catch
		 * (InterruptedException e) { e.printStackTrace(); } catch
		 * (NullPointerException e) { e.printStackTrace();
		 * Logger.error("Failed to Generate Schedule" +
		 * e.getCause().getMessage()); }
		 */
		/*
		 * Schedule schedule = Schedule.findById(id); new ExtractData(schedule,
		 * schedule.data).now();
		 */
		// Form form = Form.findByShortName("CensusEnrollment");
		// Woman checkWom = Woman.find("byUID", 609435).first();
		// Woman checkWom = (Woman)
		// Beneficiary.find("select b from Beneficiary b where b.UID =" + 210702
		// +
		// " and b.status not in ('DELETED','INACTIVE','OUT_OF_JIVITA','WOMAN_DIED')").first();
		/*
		 * Woman targetBen = (Woman) Beneficiary.find(
		 * "select b from Beneficiary b where b.UID =? and (b.status!=? or b.status!=? or b.status!=? or b.status!=?)"
		 * , id, BeneficiaryStatus.INACTIVE, BeneficiaryStatus.DELETED,
		 * BeneficiaryStatus.WOMAN_DIED, BeneficiaryStatus.INACTIVE).first();
		 */
		/*
		 * Schedule censusSchedule = Schedule.find(
		 * "Select s from Schedule s where beneficiary=? and formToGenerate_id = ? "
		 * + "and status=?", checkWom, form.id, ScheduleStatus.ACTIVE).first();
		 */
		// Logger.debug("checkWom" + targetBen.toString());
		// Logger.debug("censusSchedule: " + censusSchedule.id);
		/*
		 * //System.out.println(id); Schedule schedule = Schedule.findById(id);
		 * //System.out.println(schedule);
		 * 
		 * //System.out.println(checkTransmissionV2.isMigratable());
		 * CheckTransmission
		 * .printXml(checkTransmissionV2.getXmlForSubmission()); return false;
		 */
		CheckTransmissionV2 checkTransmissionV2 = new CheckTransmissionV2(
				schedule);
		return "Yes";
	}

	public static void adapterResponse() {
		Logger.info("Final response from Adapter - "
				+ controllers.Controller.request.querystring);
		Logger.info("End - " + System.currentTimeMillis());
		String requestId = request.params.get("request_id");
		String entityId = request.params.get("entity_id");

		String relationalId = request.params.get("relational_id");
		String status = request.params.get("status");

		Logger.info(requestId + " - " + entityId + " - " + relationalId + " - "
				+ status);
		OpensrpTransferLog opensrpTransferLog = OpensrpTransferLog.find(
				"Select s from OpensrpTransferLog s where requestId=?",
				requestId).first();
		if (opensrpTransferLog != null) {
			Logger.info(opensrpTransferLog.toString());
			if (opensrpTransferLog.form.id == 29) {
				opensrpTransferLog.household.opensrp_entityId = opensrpTransferLog
						.getOpensrp_entityId();
				opensrpTransferLog.household.save();
				JpaCommit();

				saveTransferLogStatus(status, opensrpTransferLog);

				opensrpTransferLog.save();
				JpaCommit();
			} else if (opensrpTransferLog.form.id == 30) {
				opensrpTransferLog.relationalId = relationalId;
				opensrpTransferLog.save();
				opensrpTransferLog.beneficiary.idForOpensrp = relationalId;
				opensrpTransferLog.beneficiary.isMigratedToOpenSRP = true;
				opensrpTransferLog.beneficiary.save();
				JpaCommit();
				saveTransferLogStatus(status, opensrpTransferLog);
				opensrpTransferLog.save();
				JpaCommit();
			} else {
				opensrpTransferLog.relationalId = relationalId;
				opensrpTransferLog.opensrp_entityId = entityId;
				saveTransferLogStatus(status, opensrpTransferLog);
				opensrpTransferLog.save();
				JpaCommit();
			}

		} else {
			Logger.info("opensrpTransferLog is null ");
		}

	}

	private static void saveTransferLogStatus(String status,
			OpensrpTransferLog opensrpTransferLog) {
		if (status.equalsIgnoreCase("S")) {
			opensrpTransferLog.setStatus(OpensrpTransferLogStatus.DONE);
		} else if (status.equalsIgnoreCase("F")) {
			opensrpTransferLog.setStatus(OpensrpTransferLogStatus.FAILED);
		}
	}

}
