package assessment;
import org.apache.poi.ss.usermodel.*;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Assignment {
    public static void main(String[] args)
    {
        try
        {
            // Load the XLSX file
            FileInputStream fis = new FileInputStream(new File("./files/Assignment_Timecard.xlsx"));
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

            // Create a map to store employee data
            Map<String, List<Shift>> employeeMap = new HashMap<String, List<Shift>>();

            // Iterate through the rows in the sheet
            for (Row row : sheet)
            {
                if (row.getRowNum() == 0)
                {
                    // Skip the header row
                    continue;
                }

                // Read data from the row
                
                String positionId = row.getCell(0).getStringCellValue();
                String positionState = row.getCell(1).getStringCellValue();
                Date timeIn=null;
                Date timeOut=null;
                try 
                {
                 timeIn = row.getCell(2).getDateCellValue();
                 timeOut = row.getCell(3).getDateCellValue();
                }
                catch(IllegalStateException e)
                {
                	
                }
                
                String timecardHoursstr = row.getCell(4).getStringCellValue();
                String employeeName = row.getCell(7).getStringCellValue();
                double timecardHours;
                if (!timecardHoursstr.isEmpty())
                {
                    try 
                    {
                        timecardHours = Double.parseDouble(timecardHoursstr);
                    } 
                    catch (NumberFormatException e)
                    {
                        
                        timecardHours = 0.1; // You can choose a default value or handle the error accordingly
                    }
                } 
                else 
                {
                    
                    timecardHours = 0.1; // You can choose a default value or handle the empty cell accordingly
                }
                // Create or update employee data in the map
                if (!employeeMap.containsKey(employeeName))
                {
                    employeeMap.put(employeeName, new ArrayList<>());
                }
                List<Shift> shifts = employeeMap.get(employeeName);
                shifts.add(new Shift(positionId, positionState, timeIn, timeOut));
            }

            // Close the workbook
            workbook.close();

            // Analyze and print the results
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            boolean workedFor7ConsecutiveDaysFound = false;
            boolean lessThan10HoursBetweenShiftsFound = false;
            boolean moreThan14HoursInSingleShiftFound = false;

            for (Map.Entry<String, List<Shift>> entry : employeeMap.entrySet()) {
                String employeeName = entry.getKey();
                List<Shift> shifts = entry.getValue();

                // Implementing criteria here based on the 'shifts' list

                if (hasWorkedFor7ConsecutiveDays(shifts)) 
                {
                    workedFor7ConsecutiveDaysFound = true;
                    System.out.println("Employee Name: " + employeeName + " has worked for 7 consecutive days");
                    for (Shift shift : shifts)
                    {
                        System.out.println("  Position: " + shift.getPositionId() +
                                ", State: " + shift.getPositionState() +
                                ", Time In: " + sdf.format(shift.getTimeIn()) +
                                ", Time Out: " + sdf.format(shift.getTimeOut()) +
                                ", Timecard Hours: " + shift.getTimecardHours());
                    }
                }

                if (hasLessThan10HoursBetweenShifts(shifts)) 
                {
                    lessThan10HoursBetweenShiftsFound = true;
                    System.out.println("Employee Name: " + employeeName + " has worked less than 10 hours between shifts");
                    for (Shift shift : shifts) 
                    {
                        System.out.println("  Position: " + shift.getPositionId() +
                                ", State: " + shift.getPositionState() +
                                ", Time In: " + sdf.format(shift.getTimeIn()) +
                                ", Time Out: " + sdf.format(shift.getTimeOut()));
                    }
                }

                if (hasWorkedMoreThan14HoursInSingleShift(shifts))
                {
                    moreThan14HoursInSingleShiftFound = true;
                    System.out.println("Employee Name: " + employeeName + " has worked more than 14 hours in a single shift");
                    for (Shift shift : shifts)
                    {
                        System.out.println("  Position: " + shift.getPositionId() +
                                ", State: " + shift.getPositionState() +
                                ", Time In: " + sdf.format(shift.getTimeIn()) +
                                ", Time Out: " + sdf.format(shift.getTimeOut()));
                    }
                }
            }

            if (!workedFor7ConsecutiveDaysFound)
            {
                System.out.println("No employees worked for 7 consecutive days.");
            }
            if (!lessThan10HoursBetweenShiftsFound)
            {
                System.out.println("No employees worked less than 10 hours between shifts.");
            }
            if (!moreThan14HoursInSingleShiftFound) 
            {
                System.out.println("No employees worked more than 14 hours in a single shift.");
            }        
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static boolean hasWorkedFor7ConsecutiveDays(List<Shift> shifts)
    {
        // Implementing logic to check if the employee has worked for 7 consecutive days
        if (shifts.size() < 7)
        {
            return false;
        }

        // Sort shifts by timeIn
        Collections.sort(shifts, Comparator.comparing(Shift::getTimeIn));

        for (int i = 0; i <= shifts.size() - 7; i++)
        {
            boolean consecutive = true;
            for (int j = 0; j < 7; j++)
            {
                Date currentDay = shifts.get(i + j).getTimeIn();
                Calendar cal = Calendar.getInstance();
                cal.setTime(currentDay);
                cal.add(Calendar.DAY_OF_MONTH, j);
                Date expectedDay = cal.getTime();
                if (!currentDay.equals(expectedDay))
                {
                    consecutive = false;
                    break;
                }
            }
            if (consecutive)
            {
                return true;
            }
        }
        return false;
    }

    private static boolean hasLessThan10HoursBetweenShifts(List<Shift> shifts) 
    {
        // Implementing logic to check if the employee has less than 10 hours between shifts
        for (int i = 1; i < shifts.size(); i++) 
        {
            long diffInMilliseconds = shifts.get(i).getTimeIn().getTime() - shifts.get(i - 1).getTimeOut().getTime();
            long hoursBetween = diffInMilliseconds / (60 * 60 * 1000);
            if (hoursBetween > 1 && hoursBetween < 10) 
            {
                return true;
            }
        }
        return false;
    }

    private static boolean hasWorkedMoreThan14HoursInSingleShift(List<Shift> shifts)
    {
        // Implementing logic to check if the employee has worked more than 14 hours in a single shift
        for (Shift shift : shifts) 
        {
            if (shift.getTimecardHours() > 14)
            {
                return true;
            }
        }
        return false;
    }
}

class Shift 
{
    private String positionId;
    private String positionState;
    private Date timeIn;
    private Date timeOut;
    private double timecardHours;

    public Shift(String positionId, String positionState, Date timeIn, Date timeOut) 
    {
        this.positionId = positionId;
        this.positionState = positionState;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.timecardHours = timecardHours;
    }

    public String getPositionId() 
    {
        return positionId;
    }

    public String getPositionState()
    {
        return positionState;
    }

    public Date getTimeIn()
    {
        return timeIn;
    }

    public Date getTimeOut()
    {
        return timeOut;
    }

    public double getTimecardHours()
    {
        return timecardHours;
    }
}
