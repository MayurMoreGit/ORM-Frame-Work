// example to show the retrieval operation
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
dm.begin();
List<Student> list=dm.query(Student.class).where("rollNumber").eq(1).and().where("name").eq("Gagan Joshi").fire();
dm.end();
dm.begin();
List<Student> list2=dm.query(Student.class).orderBy("firstName").fire();
dm.end();
dm.begin();
List<Student> list5=dm.query(Student.class).where("rollNumber").gt(1).orderBy("lastName").fire();
dm.end();
dm.begin();
List<Student> list3=dm.query(Student.class).where("rollNumber").gt(1).orderBy("firstName").descending().orderBy("rollNumber").fire();
dm.end();
for(Student ss:list3)
{
System.out.println("roll_no::"+ss.rollNumber+"   ,Name::"+ss.firstName+"   ,gender::"+ss.gender);
}
}catch(Exception e)
{
System.out.println(e);
}
}
}