// example to show the delete operation
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
Student s=new Student();
s.rollNumber=103;
dm.begin();
dm.delete(s);
dm.end();
}catch(Exception e)
{
System.out.println(e);
}
}
}