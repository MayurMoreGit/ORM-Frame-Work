// example to show the save operation
import java.text.*;
import java.util.*;
import com.thinking.machines.ormframework.exceptions.*;
import com.thinking.machines.ormframework.annotations.*;
import com.thinking.machines.ormframework.framework.*;
import com.thinking.machines.ormframework.pojo.*;
class psp
{
public static void main(String gg[])
{
try
{
DataManager dm=DataManager.getDataManager();

Course c=new Course();
c.title="Angular JS";
dm.begin();
dm.save(c);
dm.end();

SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
Student s=new Student();
s.firstName="Alina";
s.lastName="Mirza";
s.rollNumber=105;
s.dateOfBirth=format.parse("2000-08-30");
s.addharCardNumber="12221221";
s.gender="F";
s.courseCode=7;
dm.begin();
dm.save(s);
dm.end();
}catch(Exception e)
{
System.out.println(e);
}
}
}