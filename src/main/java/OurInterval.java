import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ArrayList;

public class OurInterval {
  private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // We initialise our formatter, so we can parse dates quickly
  
  private final LocalDate start;
  private final LocalDate end;
  
  public OurInterval(LocalDate start, LocalDate end) {
    this.start = start;
    this.end = end;
  }
  
  public LocalDate getStart() { return start; }
  public LocalDate getEnd() { return end; }
  
  /**
   * Gets the total days between the start and end of this interval, regardless of year
   */
  public long daysBetween() {
    long daysBetween = ChronoUnit.DAYS.between(start, end); // Pretty intuitive - ask for the difference between two dates, in days
    daysBetween += 1; // Because in the above, the end date is non-inclusive, so between 2018-01-01 and 2018-01-01 are 0 days, not one
    
    return daysBetween;
  }
  
  /**
   * Gets the total days between the start and end of this interval that fit within the given year
   */
  public long daysBetweenDuringYear(int year) {
    LocalDate startOfYear = LocalDate.parse("2000-01-01", format).withYear(year); // Hacky way of saying "1st of January of the given year", since 2000 gets replaced by our year
    LocalDate endOfYear = LocalDate.parse("2000-12-31", format).withYear(year); // Idem for "31st of December"
    
    LocalDate startDate, endDate;
    
    if(start.compareTo(startOfYear) > 0) // We start sometime after the year begins
      startDate = start;
    else
      startDate = startOfYear;
    
    if(end.compareTo(endOfYear) < 0) // We end sometime before the year end
      endDate = end;
    else
      endDate = endOfYear;
    
    if(endDate.compareTo(startDate) < 0) return 0; // It means that we have no days in the given year
    
    long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
    daysBetween += 1; // Because in the above, the end date is non-inclusive, so between 2018-01-01 and 2018-01-01 are 0 days, not one
    
    return daysBetween;
  }
  
  public String toString() {
    return "Interval [" + start + ", " + end + "]";
  }
  
  /**
   * Takes an input OurInterval that might lie partially or totally outside this interval.
   * Returns an OurInterval that represents the part of the input interval that overlaps this one.
   * Returns null if there is no overlap whatsoever
   */
  public OurInterval boundIntervalToThis(OurInterval val) {
    LocalDate newStart, newEnd;
    
    if(val.getStart().compareTo(start) < 0) // Check if the interval starts before this one; if yes, cap the beginning
      newStart = start;
    else
      newStart = val.getStart();
    
    if(val.getEnd().compareTo(end) > 0) // Check if the interva ends after this one; if yes, cap the ending
      newEnd = end;
    else
      newEnd = val.getEnd();
    
    if(newEnd.compareTo(newStart) < 0) return null; // Return null if we got an invalid interval
    else return new OurInterval(newStart, newEnd);
  }
  
  public static long totalDaysInYear(int year) { 
    LocalDate startOfYear = LocalDate.parse("2000-01-01", format).withYear(year); // Hacky way of saying "1st of January of the given year", since 2000 gets replaced by our year
    LocalDate endOfYear = LocalDate.parse("2000-12-31", format).withYear(year); // Same as above
    
    return ChronoUnit.DAYS.between(startOfYear, endOfYear) + 1;
  }
}