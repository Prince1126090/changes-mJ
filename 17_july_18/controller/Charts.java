package controllers;

import groovy.util.NodeList;

import java.io.File;
import java.io.IOException;
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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.Query;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import jobs.ExtractData;
import models.ACArea;
import models.Batch;
import models.Beneficiary;
import models.BeneficiaryStatus;
import models.Data;
import models.Event;
import models.Form;
import models.FormStatus;
import models.Mauza;
import models.NewChildUIDList;
import models.NewMWRAUIDList;
import models.PathsOfFormVariables;
import models.GeoUnion;
import models.Household;
import models.HouseholdStatus;
import models.JWeek;
import models.Logic;
import models.Outcome;
import models.Role;
import models.Round;
import models.Schedule;
import models.ScheduleStatus;
import models.ScheduleType;
import models.Sector;
import models.TLPin;
import models.UnitData;
import models.User;
import models.Woman;
import models.XmlConfig;
import net.sf.ehcache.hibernate.HibernateUtil;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.asn1.cmp.OOBCertHash;
import org.fusesource.hawtbuf.ByteArrayInputStream;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.hql.classic.FromPathExpressionParser;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import play.Logger;
import play.Play;
import play.db.jpa.GenericModel.JPAQuery;
import play.db.jpa.JPA;
import play.mvc.With;
import play.templates.Template;
import play.templates.TemplateLoader;
import play.libs.WS;
import play.libs.XML;
import play.libs.XPath;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import play.libs.ws.*;
import responses.HouseholdResponse;
import utils.BeneficiaryUtil;
import utils.CheckTransmission;
import utils.CommonUtil;
import utils.ScheduleUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mysql.jdbc.log.Log;
import com.ning.http.client.Request;

import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.ExternalRestrictions;

@With(Deadbolt.class)
public class Charts extends Controller {

    public static int count = 0;
    public static int pages = 0;


    public static void loadJweekFromDate(Long id) {

        JWeek fromJweek = JWeek.findById(id);
        SimpleDateFormat s = new SimpleDateFormat("MM/dd/yyyy");
        String fromDate = s.format(fromJweek.fromDate);
        render(fromDate);
    }

    public static String loadDateFrom() {

        if (request.isAjax()) {
            String jweekFromId = request.params.get("fromJweek");
            JWeek fromJweek = JWeek.findById(Long.parseLong(jweekFromId));
            SimpleDateFormat s = new SimpleDateFormat("MM/dd/yyyy");
            String fromDate = s.format(fromJweek.fromDate);
            return fromDate;
        }

        return "1/1/2014";
    }


    public static String loadDateTo() {

        if (request.isAjax()) {
            String jweektoId = request.params.get("toJweek");
            JWeek toJweek = JWeek.findById(Long.parseLong(jweektoId));
            SimpleDateFormat s = new SimpleDateFormat("MM/dd/yyyy");
            String toDate = s.format(toJweek.toDate);
            return toDate;
        }

        return "12/31/2014";
    }
    
    public static ArrayList<ArrayList<String>> getResultFromQuery(String query){
    	ArrayList<ArrayList<String>> finalResultList = new ArrayList<ArrayList<String>>();
    	List<Object[]> result = JPA.em().createQuery(query).getResultList();
		 for (int i=0; i<result.size(); i++){
		    Object[] row = (Object[]) result.get(i);
		    ArrayList<String> rowList = new ArrayList<String>();
		    rowList.add((String)row[0]);
		    rowList.add((String)row[1].toString());
		    finalResultList.add(rowList);
		 }
		 return finalResultList;
    }
    
    public static ArrayList<ArrayList<String>> getResultFromSqlQuery(String query){
    	ArrayList<ArrayList<String>> finalResultList = new ArrayList<ArrayList<String>>();

    	List<Object[]> result = JPA.em().createNativeQuery(query).getResultList();
		 for (int i=0; i<result.size(); i++){
		    Object[] row = (Object[]) result.get(i);
		    ArrayList<String> rowList = new ArrayList<String>();
		    rowList.add((String)row[0]);
		    rowList.add((String)row[1].toString());
		    finalResultList.add(rowList);
		 }
		 return finalResultList;
    }
    
    public static String queryHouseholdMonthWise(String fromDate, String toDate, int status){
    	String query = " SELECT CONCAT( YEAR(updated_at) ,'.',LPAD(MONTH(updated_at) ,2,'0')) AS yearMonth, "
				+" COUNT(*) as quantity "
				+" FROM Household h "
				+" WHERE status = "
				+status
				+" and  (updated_at between'"
				+fromDate
				+"' AND '"
				+toDate
				+"')"
				+" GROUP BY CONCAT(YEAR(updated_at),'-',LPAD(MONTH(updated_at),2,'0')) ";
    	return query;
    }
    
    public static String queryHouseholdDayWise(String fromDate, String toDate, int status){
    	String query = " SELECT CONCAT( YEAR(updated_at) ,'-',"
    			+"LPAD(MONTH(updated_at) ,2,'0'),'-',"
    			+"LPAD(dayofmonth(updated_at),2,'0')) AS yearMonth, "
				+" COUNT(*) as quantity "
				+" FROM Household h "
				+" WHERE status = "
				+status
				+" and  (updated_at between'"
				+fromDate
				+"' AND '"
				+toDate
				+"')"
				+" GROUP BY CONCAT(YEAR(updated_at),'-',"
				+"LPAD(MONTH(updated_at),2,'0'),'-',"
				+"LPAD(dayofmonth(updated_at),2,'0')) ";
    	return query;
    }
    
    
    
    public static String queryHouseholdWeekWise(String fromDate, String toDate, int status){
    	String query = " SELECT CONCAT('JWeek - ',jw.jweekId), SUM(hh.quantity) "
    				+" FROM JWeek jw, "
    				+" (SELECT CONCAT( YEAR(updated_at),'-',"
    				+" LPAD(MONTH(updated_at),2,'0'), '-', "
    				+" LPAD(dayofmonth(updated_at),2,'0')) AS yearMonth, "
    				+" COUNT(*) AS quantity "
    				+" FROM Household h "
    				+" WHERE status = "
    				+status
    				+" AND  (updated_at between '"
    				+fromDate
    				+"' AND '"
    				+toDate
    				+"') "
    				+" GROUP BY CONCAT(YEAR(updated_at),'-', "
    				+" LPAD(MONTH(updated_at),2,'0'),'-', "
    				+" LPAD(dayofmonth(updated_at),2,'0'))) AS hh "
    				+" WHERE STR_TO_DATE(hh.yearMonth, '%Y-%m-%d') between "
    				+" jw.fromDate AND jw.toDate "
    				+" GROUP BY jw.jweekId ";
    	return query;
    }
    
    public static String queryBenMonthWise(String fromDate, String toDate, String status){
    	String query = " SELECT CONCAT( YEAR(updated_at) ,' - ',LPAD(MONTH(updated_at) ,2,'0')) AS yearMonth, "
				+" COUNT(*) as quantity "
				+" FROM Beneficiary b "
				+" WHERE status = '"
				+status
				+"' and  (updated_at between'"
				+fromDate
				+"' AND '"
				+toDate
				+"')"
				+" GROUP BY CONCAT(YEAR(updated_at),'-',LPAD(MONTH(updated_at),2,'0')) ";
    	return query;
    }
    
    public static String queryBenDayWise(String fromDate, String toDate, String status){
    	String query = " SELECT CONCAT( YEAR(updated_at) ,'-',"
    			+"LPAD(MONTH(updated_at) ,2,'0'),'-',"
    			+"LPAD(dayofmonth(updated_at),2,'0')) AS yearMonth, "
				+" COUNT(*) as quantity "
				+" FROM Beneficiary b "
				+" WHERE status = '"
				+status
				+"' AND  (updated_at between'"
				+fromDate
				+"' AND '"
				+toDate
				+"')"
				+" GROUP BY CONCAT(YEAR(updated_at),'-',"
				+"LPAD(MONTH(updated_at),2,'0'),'-',"
				+"LPAD(dayofmonth(updated_at),2,'0')) ";
    	return query;
    }
    
    public static String queryBenWeekWise(String fromDate, String toDate, String status){
    	String query = " SELECT CONCAT('JWeek - ',jw.jweekId), SUM(hh.quantity) "
    				+" FROM JWeek jw, "
    				+" (SELECT CONCAT( YEAR(updated_at),'-',"
    				+" LPAD(MONTH(updated_at),2,'0'), '-', "
    				+" LPAD(dayofmonth(updated_at),2,'0')) AS yearMonth, "
    				+" COUNT(*) AS quantity "
    				+" FROM Beneficiary b  "
    				+" WHERE status = '"
    				+status
    				+"' AND  (updated_at between '"
    				+fromDate
    				+"' AND '"
    				+toDate
    				+"') "
    				+" GROUP BY CONCAT(YEAR(updated_at),'-', "
    				+" LPAD(MONTH(updated_at),2,'0'),'-', "
    				+" LPAD(dayofmonth(updated_at),2,'0'))) AS hh "
    				+" WHERE STR_TO_DATE(hh.yearMonth, '%Y-%m-%d') between "
    				+" jw.fromDate AND jw.toDate "
    				+" GROUP BY jw.jweekId ";
    	return query;
    }
    
    
    
    public static String queryScheduleMonthWise(String fromDate, String toDate,String formId, String status){
    	String query = " SELECT CONCAT( YEAR(updated_at) ,'.',LPAD(MONTH(updated_at) ,2,'0')) AS yearMonth, "
				+" COUNT(*) as quantity "
				+" FROM Schedule s "
				+" WHERE status = '"
				+status
				+"' and formToGenerate_id = "
				+formId
				+" and  (updated_at between'"
				+fromDate
				+"' AND '"
				+toDate
				+"')"
				+" GROUP BY CONCAT(YEAR(updated_at),'-',LPAD(MONTH(updated_at),2,'0')) ";
    	return query;
    }
    
    
    
    public static String queryScheduleDayWise(String fromDate, String toDate,String formId, String status){
    	String query = " SELECT CONCAT( YEAR(updated_at) ,'-',"
    			+"LPAD(MONTH(updated_at) ,2,'0'),'-',"
    			+"LPAD(dayofmonth(updated_at),2,'0')) AS yearMonth, "
				+" COUNT(*) as quantity "
				+" FROM Schedule s "
				+" WHERE status = '"
				+status
				+"' and formToGenerate_id = "
				+formId
				+" and  (updated_at between'"
				+fromDate
				+"' AND '"
				+toDate
				+"')"
				+" GROUP BY CONCAT(YEAR(updated_at),'-',"
				+"LPAD(MONTH(updated_at),2,'0'),'-',"
				+"LPAD(dayofmonth(updated_at),2,'0')) ";
    	return query;
    }
    
    
    
    
    public static String queryScheduleWeekWise(String fromDate, String toDate,String formId, String status){
    	String query = " SELECT CONCAT('JWeek - ',jw.jweekId), SUM(hh.quantity) "
				+" FROM JWeek jw, "
				+" (SELECT CONCAT( YEAR(updated_at),'-',"
				+" LPAD(MONTH(updated_at),2,'0'), '-', "
				+" LPAD(dayofmonth(updated_at),2,'0')) AS yearMonth, "
				+" COUNT(*) AS quantity "
				+" FROM Schedule s "
				+" WHERE status = '"
				+status
				+"' and formToGenerate_id = "
				+formId
				+" and  (updated_at between'"
				+fromDate
				+"' AND '"
				+toDate
				+"')"
				+" GROUP BY CONCAT(YEAR(updated_at),'-', "
				+" LPAD(MONTH(updated_at),2,'0'),'-', "
				+" LPAD(dayofmonth(updated_at),2,'0'))) AS hh "
				+" WHERE STR_TO_DATE(hh.yearMonth, '%Y-%m-%d') between "
				+" jw.fromDate AND jw.toDate "
				+" GROUP BY jw.jweekId ";
    	return query;
    }



    
    public static void summaryChart() {
    	ArrayList<ArrayList<String>> newHHArListMW = new ArrayList<ArrayList<String>>();
    	ArrayList<ArrayList<String>> movedHHArListMW = new ArrayList<ArrayList<String>>();
    	ArrayList<ArrayList<String>> deletedHHArListMW = new ArrayList<ArrayList<String>>();
    	String newHHListMW=null, movedHHListMW=null, deletedHHListMW=null;
    	
    	ArrayList<ArrayList<String>> newHHArListDW = new ArrayList<ArrayList<String>>();
    	ArrayList<ArrayList<String>> movedHHArListDW = new ArrayList<ArrayList<String>>();
    	ArrayList<ArrayList<String>> deletedHHArListDW = new ArrayList<ArrayList<String>>();
    	String newHHListDW=null, movedHHListDW=null, deletedHHListDW=null;
    	
    	ArrayList<ArrayList<String>> newHHArListWW = new ArrayList<ArrayList<String>>();
    	ArrayList<ArrayList<String>> movedHHArListWW = new ArrayList<ArrayList<String>>();
    	ArrayList<ArrayList<String>> deletedHHArListWW = new ArrayList<ArrayList<String>>();
    	String newHHListWW=null, movedHHListWW=null, deletedHHListWW=null;

        List<JWeek> jweeks = JWeek.findAll();
        JWeek currentJweek = JWeek.findJWeekByDate(new DateTime().toDate());

        String jweekFromId = request.params.get("jweek.fromid");
        String jweekToId = request.params.get("jweek.toid");
        String fromDate = request.params.get("fromDate");
        String toDate = request.params.get("toDate");

        Logger.debug("jweek.fromid - " + jweekFromId + " - jweek.toid- " + jweekToId);
        if (currentJweek == null) {
            Logger.error("Current jweek not found.");
        }
        
        if ((jweekFromId == null || jweekFromId.isEmpty()) && (jweekToId == null || jweekToId.isEmpty())) {
        	jweekFromId = "1";
            jweekToId = "201";
            fromDate = "04/22/2016";
            toDate = "02/27/2020";
        }
        

        JWeek fromJweek = null;
        JWeek toJweek = null;

        String countQueryHH = null;
        String countQuerySch = null;

        //Search By Jweek ID
        if ((jweekFromId != null && !jweekFromId.isEmpty()) || (jweekToId != null && !jweekToId.isEmpty())) {
            if (jweekFromId.isEmpty()) {
                fromJweek = jweeks.get(0);
            } else {
                fromJweek = JWeek.findById(Long.parseLong(jweekFromId));
            }
            if (jweekToId.isEmpty()) {
                toJweek = jweeks.get(jweeks.size() - 1);
            } else {
                toJweek = JWeek.findById(Long.parseLong(jweekToId));
            }

            SimpleDateFormat s = new SimpleDateFormat("yyyy/MM/dd");
            String JfromDate = s.format(fromJweek.fromDate);
            DateTime dateTime = new DateTime(toJweek.toDate);
            dateTime = dateTime.plusDays(1);
            // as the query using between and date from simpledateformat doesn't include the
            // schedules in the last date
            String JtoDate = s.format(dateTime.toDate());
            //Logger.info("jweek from date " + JfromDate);
            //Logger.info("jweek to date " + JtoDate);

            
            countQueryHH = queryHouseholdMonthWise(JfromDate, JtoDate, 1);
            newHHArListMW = getResultFromQuery(countQueryHH);
            //Logger.info("newHHListMW : "+newHHArListMW.toString());
            
            countQueryHH = queryHouseholdMonthWise(JfromDate, JtoDate, 2);
            movedHHArListMW = getResultFromQuery(countQueryHH);
            //Logger.info("movedHHListMW : "+movedHHArListMW.toString());
            
            countQueryHH = queryHouseholdMonthWise(JfromDate, JtoDate, 3);
            deletedHHArListMW = getResultFromQuery(countQueryHH);
            //Logger.info("deletedHHListMW : "+deletedHHArListMW.toString());   
            
            Gson gson = new GsonBuilder().create();
            newHHListMW = gson.toJson(newHHArListMW);
            movedHHListMW = gson.toJson(movedHHArListMW);
            deletedHHListMW = gson.toJson(deletedHHArListMW);
            
            
            countQueryHH = queryHouseholdDayWise(JfromDate, JtoDate, 1);
            newHHArListDW = getResultFromQuery(countQueryHH);
            //Logger.info("newHHListDW : "+newHHArListDW.toString());
            
            countQueryHH = queryHouseholdDayWise(JfromDate, JtoDate, 2);
            movedHHArListDW = getResultFromQuery(countQueryHH);
            //Logger.info("movedHHListDW : "+movedHHArListDW.toString());
            
            countQueryHH = queryHouseholdDayWise(JfromDate, JtoDate, 3);
            deletedHHArListDW = getResultFromQuery(countQueryHH);
            //Logger.info("deletedHHListDW : "+deletedHHArListDW.toString());  
            
            newHHListDW = gson.toJson(newHHArListDW);
            movedHHListDW = gson.toJson(movedHHArListDW);
            deletedHHListDW = gson.toJson(deletedHHArListDW);
    	     
    	     
    	     countQueryHH = queryHouseholdWeekWise(JfromDate, JtoDate, 1);
             newHHArListWW = getResultFromSqlQuery(countQueryHH);
             //Logger.info("newHHListWW : "+newHHArListWW.toString());
             
             countQueryHH = queryHouseholdWeekWise(JfromDate, JtoDate, 2);
             movedHHArListWW = getResultFromSqlQuery(countQueryHH);
             //Logger.info("movedHHListWW : "+movedHHArListWW.toString());
             
             countQueryHH = queryHouseholdWeekWise(JfromDate, JtoDate, 3);
             deletedHHArListWW = getResultFromSqlQuery(countQueryHH);
             //Logger.info("deletedHHListWW : "+deletedHHArListWW.toString());  
             
             newHHListWW = gson.toJson(newHHArListWW);
             movedHHListWW = gson.toJson(movedHHArListWW);
             deletedHHListWW = gson.toJson(deletedHHArListWW);
            
        }

        SimpleDateFormat s = new SimpleDateFormat("MM/dd/yyyy");
        if (fromDate == null) {
            if (jweeks.size() > 0) {
                fromDate = s.format(jweeks.get(0).fromDate);
            } else {
                fromDate = "01/01/2015";
            }
        }
        if (toDate == null) {
            if (jweeks.size() > 0) {
                toDate = s.format(jweeks.get(jweeks.size() - 1).toDate);
            } else {
                toDate = "12/31/2015";
            }
        }       
        
        List<BeneficiaryStatus> bStatus = new ArrayList<BeneficiaryStatus>(
				EnumSet.allOf(BeneficiaryStatus.class));
        
        List<Form> forms = Form.findAll();
                
        render(jweeks, currentJweek, fromDate, toDate, fromJweek, jweekFromId, 
        	   jweekToId, toJweek, 
        	   newHHListMW, movedHHListMW, deletedHHListMW,
        	   newHHListDW, movedHHListDW, deletedHHListDW,
        	   newHHListWW, movedHHListWW, deletedHHListWW,
        	   bStatus,
        	   forms);
    }
    
    
    
	public static void beneficiaryChart(String status, String criteria, String fromDate, String toDate) {
		ArrayList<ArrayList<String>> benArList = new ArrayList<ArrayList<String>>();
    	String newHHListMW=null;
    	String countQueryHH = null;
    	
    	
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
		
		SimpleDateFormat s = new SimpleDateFormat("yyyy/MM/dd");
        String JfromDate = s.format(fromDateObject);
        String JtoDate = s.format(toDateObject);
		
        if(criteria.equals("1")){
        	countQueryHH = queryBenMonthWise(JfromDate, JtoDate, status);
        }else if (criteria.equals("2")){
        	countQueryHH = queryBenWeekWise(JfromDate, JtoDate, status);
        }else if (criteria.equals("3")){
        	countQueryHH = queryBenDayWise(JfromDate, JtoDate, status);
        }
		
        if (criteria.equals("2")){
        	benArList = getResultFromSqlQuery(countQueryHH);
        }else{
        	benArList = getResultFromQuery(countQueryHH);
        }
		
		/*Gson gson = new GsonBuilder().create();
	    String json = gson.toJson(benArList);*/

		renderJSON(benArList);
	}
	
	
	
	public static void scheduleChart(String formId, String status, String criteria, String fromDate, String toDate) {
		ArrayList<ArrayList<String>> benArList = new ArrayList<ArrayList<String>>();
    	String newHHListMW=null;
    	String countQueryHH = null;
    	
    	
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
		
		SimpleDateFormat s = new SimpleDateFormat("yyyy/MM/dd");
        String JfromDate = s.format(fromDateObject);
        String JtoDate = s.format(toDateObject);
		
        if(criteria.equals("1")){
        	countQueryHH = queryScheduleMonthWise(JfromDate, JtoDate,formId, status);
        }else if (criteria.equals("2")){
        	countQueryHH = queryScheduleWeekWise(JfromDate, JtoDate,formId, status);
        }else if (criteria.equals("3")){
        	countQueryHH = queryScheduleDayWise(JfromDate, JtoDate,formId, status);
        }
        Logger.info("schedule query ::::: "+ countQueryHH);
		
        if (criteria.equals("2")){
        	benArList = getResultFromSqlQuery(countQueryHH);
        }else{
        	benArList = getResultFromQuery(countQueryHH);
        }
       
		
		/*Gson gson = new GsonBuilder().create();
	    String json = gson.toJson(benArList);*/

		renderJSON(benArList);
	}
    
}
