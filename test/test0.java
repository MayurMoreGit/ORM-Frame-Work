// example to generate POJO's
import com.thinking.machines.ormframework.utils.*;
class psp
{
public static void main(String gg[])
{
try
{
POJOGenerator.generatePOJOEquivalentToTable();
}catch(Exception e)
{
System.out.println(e);
}
}
}