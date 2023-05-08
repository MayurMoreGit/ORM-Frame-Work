package com.thinking.machines.ormframework.utils;
import com.thinking.machines.ormframework.annotations.*;
import com.thinking.machines.ormframework.exceptions.*;
import java.lang.reflect.*;
import java.util.*;
import java.sql.*;
public class Query
{
private Map<String,String> mapping;
private Class<?> tableClass;
private String selectQuery;
private boolean whereExecuted;
private boolean orderByExecuted;
public Connection connection;

public Query(Class<?> c,Connection con) throws DataException
{
mapping=new HashMap<>();
whereExecuted=false;
orderByExecuted=false;
tableClass=c;
connection=con;
populateMap();
prepareInitialStatement();
}
public void prepareInitialStatement() throws DataException
{
try
{
if(tableClass.getAnnotation(Table.class)==null)
{
throw new DataException("Class::"+tableClass.getName()+" does not have Table Annotation,so it cannot be linked with any sql Table");
}
String tableName=((Table)tableClass.getAnnotation(Table.class)).name();
this.selectQuery="select * from "+tableName+" ";
}catch(Exception e)
{
throw new DataException(e.getMessage());
}
}
public void populateMap() throws DataException
{
try
{
Field fields[]=tableClass.getFields();
for(Field f:fields)
{
f.setAccessible(true);
if(f.getAnnotation(Column.class)!=null)
{
mapping.put(f.getName(),f.getAnnotation(Column.class).name());
}
}
}catch(Exception e)
{
throw new DataException(e.getMessage());
}
}
public List fire()throws DataException
{
List list=new LinkedList<>();
try
{
this.selectQuery+=";";
System.out.println(this.selectQuery);
PreparedStatement ps=this.connection.prepareStatement(this.selectQuery);
ResultSet rs=ps.executeQuery();
Object object;
while(rs.next())
{
object=tableClass.newInstance();
for(Map.Entry<String,String> entry:mapping.entrySet())
{
String fieldName=entry.getKey();
String Column=entry.getValue();
Field f=(object.getClass()).getDeclaredField(fieldName);
f.setAccessible(true);
f.set(object,rs.getObject(Column));
}
list.add(object);
}
}
catch(Exception e)
{
throw new DataException(e.getMessage());
}
return list;
}
public Query where(String element)throws DataException
{
if(element==null||element.equals("")) return this;
if(whereExecuted==false)
{
selectQuery+="where ";
whereExecuted=true;
}
selectQuery+=mapping.get(element);
return this;
}
public Query eq(Object val)throws DataException
{
if(val==null) throw new DataException("Class::"+tableClass.getName()+", Does not support null val for comparision");
if(whereExecuted==false) return this;
if((val.getClass()==java.lang.String.class)||(val.getClass()==java.lang.Character.class)||(val.getClass()==java.util.Date.class)) selectQuery+="='"+val+"'";
else selectQuery+="="+val;
return this;
}
public Query lt(Object val)throws DataException
{
if(val==null) throw new DataException("Class::"+tableClass.getName()+", Does not support null val for comparision");
if(whereExecuted==false) return this;
if(val.getClass()==java.lang.String.class||val.getClass()==java.lang.Character.class||val.getClass()==java.util.Date.class) selectQuery+="<'"+val+"'";
else selectQuery+="<"+val;
return this;
}
public Query gt(Object val)throws DataException
{
if(val==null) throw new DataException("Class::"+tableClass.getName()+", Does not support null val for comparision");
if(whereExecuted==false) return this;
if(val.getClass()==java.lang.String.class||val.getClass()==java.lang.Character.class||val.getClass()==java.util.Date.class) selectQuery+=">'"+val+"'";
else selectQuery+=">"+val;
return this;
}
public Query ltet(Object val)throws DataException
{
if(val==null) throw new DataException("Class::"+tableClass.getName()+", Does not support null val for comparision");
if(whereExecuted==false) return this;
if(val.getClass()==java.lang.String.class||val.getClass()==java.lang.Character.class||val.getClass()==java.util.Date.class) selectQuery+="<='"+val+"'";
else selectQuery+="<="+val;
return this;
}
public Query gtet(Object val)throws DataException
{
if(val==null) throw new DataException("Class::"+tableClass.getName()+", Does not support null val for comparision");
if(whereExecuted==false) return this;
if(val.getClass()==java.lang.String.class||val.getClass()==java.lang.Character.class||val.getClass()==java.util.Date.class) selectQuery+=">='"+val+"'";
else selectQuery+=">="+val;
return this;
}
public Query ne(Object val)throws DataException
{
if(val==null) throw new DataException("Class::"+tableClass.getName()+", Does not support null val for comparision");
if(whereExecuted==false) return this;
if(val.getClass()==java.lang.String.class||val.getClass()==java.lang.Character.class||val.getClass()==java.util.Date.class) selectQuery+="!='"+val+"'";
else selectQuery+="!="+val;
return this;
}
public Query orderBy(String orderBy)
{
if(orderBy==null) return this;
if(orderByExecuted==false)
{
selectQuery+=" ORDER BY ";
orderByExecuted=true;
}
else
{
selectQuery+=" , ";
}
selectQuery+=mapping.get(orderBy)+" ";
return this;
}
public Query descending()throws DataException
{
if(orderByExecuted==false) throw new DataException("cannot apply decending before Order BY");
selectQuery+=" Desc ";
return this;
}
public Query ascending()throws DataException
{
if(orderByExecuted==false) throw new DataException("cannot apply decending before Order BY");
selectQuery+=" ASC ";
return this;
}
public Query and()
{
if(whereExecuted==false) return this;
selectQuery+=" and ";
return this;
}
public Query or()
{
if(whereExecuted==false) return this;
selectQuery+=" or ";
return this;
}
public Query openParen()
{
if(whereExecuted==false) return this;
selectQuery=" ( ";
return this;
}
public Query closeParen()
{
if(whereExecuted==false) return this;
selectQuery=" ) ";
return this;
}
}