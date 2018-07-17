package controllers;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Query;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jobs.ExtractData;
import models.*;
import play.Logger;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.mvc.Controller;
import utils.CheckTransmission;
import utils.CheckTransmissionV2;
import utils.ScheduleFactory;

import static utils.JpaCommit.JpaCommit;

public class Apis extends Controller {
	public static void submit(@Valid Woman beneficiary, Woman bnew) {
		if (validation.hasErrors()) {
			render("@edit", beneficiary);
		}

		String old_name = beneficiary.name;
		String new_name = bnew.name;
		if (new_name != null && !new_name.isEmpty()) {

			beneficiary.name = new_name;
			int i = insertBCLog(beneficiary, "name", old_name, new_name);

		}

		String old_hb_name = beneficiary.husbandName;
		String new_hb_name = bnew.husbandName;
		if (new_hb_name != null && !new_hb_name.isEmpty()) {
			beneficiary.husbandName = new_hb_name;
			int j = insertBCLog(beneficiary, "husbandName", old_hb_name,
					new_hb_name);
		}

		String old_nID = beneficiary.nID;
		String new_nID = bnew.nID;
		if (new_nID != null && !new_nID.isEmpty()) {
			beneficiary.nID = new_nID;
			int k = insertBCLog(beneficiary, "nID", old_nID, new_nID);
		}
		
		String old_bID = beneficiary.bID;
		String new_bID = bnew.bID;
		if (new_bID != null && !new_bID.isEmpty()) {
			beneficiary.bID = new_bID;
			int l = insertBCLog(beneficiary, "bID", old_bID, new_bID);
		}
		
		String old_age = beneficiary.age.toString();
		if(bnew.age != null){
			String new_age = bnew.age.toString();
			int n_age = bnew.age;
			if (new_age != null && !new_age.isEmpty()) {
				beneficiary.age = n_age;
				int m = insertBCLog(beneficiary, "age", old_age, new_age);
			}
		}
		

		beneficiary.save();
		flash.success("Record saved successfully.");
		listBeneficiary(0, 10);
	}

	public static int insertBCLog(Woman beneficiary, String fieldName,
			String old_value, String new_value) {

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

	public static void viewBeneficiary(Long UID) {

		Beneficiary beneficiary = Beneficiary.find("byUID",UID).first();
		long id= beneficiary.id;

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
		}

		// Iterator<BeneficiaryStatus> bStaus =
		// EnumSet.allOf(BeneficiaryStatus.class).iterator();
		List<BeneficiaryStatus> bStatus = new ArrayList<BeneficiaryStatus>(
				EnumSet.allOf(BeneficiaryStatus.class));
		ArrayList<String> stat = new ArrayList<String>();
		for (BeneficiaryStatus schdl : bStatus) {
			stat.add(schdl.getName());
		}

		render(beneficiary, allCompleteSchedules, allDueSchedules, bStatus,
				stat);
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
