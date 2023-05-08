// example to show the update operation
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
SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
Student s=new Student();
s.firstName="Aashi";
s.lastName="pandya";
s.rollNumber=103;
s.dateOfBirth=format.parse("1998-05-19");
s.addharCardNumber="PQRST1234";
s.gender="F";
s.courseCode=6;
dm.begin();
dm.update(s);
dm.end();
}catch(Exception e)
{
System.out.println(e);
}
}
}