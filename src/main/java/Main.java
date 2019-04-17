import java.time.format.DateTimeFormatter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.*;

import com.sun.javafx.scene.paint.GradientUtils.Parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Main
{
  private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // We initialise our formatter, so we can parse dates quickly
  
  // arguments are passed using the text field below this editor
  public static void main(String[] args)
  { 
	  JSONParser parser = new JSONParser();
	  String startDatef = null;
	  String endDatef = null;
	  JSONArray sPLf =  null;
	  
      try {

          Object obj = parser.parse(new FileReader(
        		  "./src/main/java/input.json"));

          JSONObject jsonObject = (JSONObject) obj;
         
          Map employeeData = ((Map)jsonObject.get("employeeData")); 
        	  

          ArrayList<String> list = new ArrayList<String>();     
          JSONArray sPL =  (JSONArray) employeeData.get("suspensionPeriodList");
          sPLf=sPL;
          startDatef=(String) employeeData.get("employmentStartDate");
          endDatef=(String) employeeData.get("employmentEndDate");
      } catch (Exception e) {
          e.printStackTrace();
      }
	  
    // Okay, now let's work with actual working time and suspensions!
    
    // The employment interval - this should be read from .json
    LocalDate startEmployment = LocalDate.parse(startDatef, format);
    LocalDate endEmployment = LocalDate.parse(endDatef, format);
    OurInterval employment = new OurInterval(startEmployment, endEmployment); // We catch 2020 in here, which is a leap year
	   
    // The suspensions - these should also be read from .json
    List<OurInterval> suspensions = new ArrayList<OurInterval>();
    
    for (int i = 0;i<sPLf.size();i++) {
	    LocalDate startSusp1 = LocalDate.parse(sPLf.get(i).toString().substring(37, 47), format);
	    LocalDate endSusp1 = LocalDate.parse(sPLf.get(i).toString().substring(12, 22), format);
	    OurInterval susp1 = new OurInterval(startSusp1, endSusp1); 
	    suspensions.add(susp1);
    }
	 
    
    int finalTotalHolidayDays;
    
    // Print stuff to make sure we got it right:
    System.out.println("Before capping:");
    System.out.println("Employment: " + employment);
    for(int i = 0; i < suspensions.size(); ++i) {
      System.out.println("Suspension " + (i+1) + ": " + suspensions.get(i));
    }
    
    // Cap the suspension; this is best to do directly when we read them, but I do it here
    List<OurInterval> cappedSuspensions = new ArrayList<OurInterval>();
    for(int i = 0; i < suspensions.size(); ++i) {
      OurInterval cappedSuspension = employment.boundIntervalToThis(suspensions.get(i));
      if(null != cappedSuspension) cappedSuspensions.add(cappedSuspension);
    }
    
    
    // Print stuff to make sure we got this right, too:
    System.out.println("\nAfter capping:");
    System.out.println("Employment: " + employment);
    for(int i = 0; i < cappedSuspensions.size(); ++i) {
      System.out.println("Capped suspension " + (i+1) + ": " + cappedSuspensions.get(i));
    }
    
    
    // Okay, now do calculations
    int startYear = employment.getStart().getYear();
    int endYear = employment.getEnd().getYear();
    int totalEmploymentYears = endYear - startYear + 1; // +1 because from 2001 to 2001 there is 1 employment year
    List<Integer> totalHolidayList = new ArrayList();
    
    System.out.println("\nData for each year of employment:");
    for(int i = 0; i < totalEmploymentYears; ++i) {
      int year = startYear + i;
      long daysThisYear = OurInterval.totalDaysInYear(year);
      
      long employmentDaysThisYear = employment.daysBetweenDuringYear(year);
      long totalDaysSuspendedThisYear = 0;
      for(OurInterval suspension : cappedSuspensions) {
        totalDaysSuspendedThisYear += suspension.daysBetweenDuringYear(year); 
      }
      
      long workedDaysThisYear = employmentDaysThisYear - totalDaysSuspendedThisYear;
      double totalHolidayDays = (i+20) * (double) workedDaysThisYear / daysThisYear;
      if ((int) totalHolidayDays + 0.5 < totalHolidayDays) {
    	 finalTotalHolidayDays  = (int) totalHolidayDays+1;
    	 totalHolidayList.add(finalTotalHolidayDays);
      }
      else {
    	  finalTotalHolidayDays  = (int) totalHolidayDays;
    	  totalHolidayList.add(finalTotalHolidayDays);
      }
      
      System.out.println("Year " + year + ": ");
      System.out.println("\tDays employed: " + employmentDaysThisYear);
      System.out.println("\tDays suspended: " + totalDaysSuspendedThisYear);
      System.out.println("\tDays actually worked: " + workedDaysThisYear);
      System.out.println("\tFraction of year worked: " + workedDaysThisYear / (double)daysThisYear); // AKA fraction of the holiday days we should receive for this year
      System.out.println("\tHoliday days: " + finalTotalHolidayDays);
    }
    
    JSONObject finalOutput = new JSONObject();
    
    JSONObject output = new JSONObject();
    output.put("errorMessage", "no error");
    
    JSONArray list = new JSONArray();
    for(int i = 0; i < totalEmploymentYears; ++i) {
    	int year = startYear + i;
    	JSONObject entry = new JSONObject();
    	entry.put("year", "" + year);
    	entry.put("holidayDays", "" + totalHolidayList.get(i));
    	
    	list.add(entry);
    }
    output.put("holidayRightsPerYearList", list);
    JSONObject output2 = new JSONObject(output); 
    System.out.println(output2.toString()); 
    
    finalOutput.put("output", output2);
    
    try (FileWriter file = new FileWriter("./src/main/java/output.json")) {

        file.write(finalOutput.toJSONString());
        file.flush();

    } catch (IOException e) {
        e.printStackTrace();
    }
  }
}