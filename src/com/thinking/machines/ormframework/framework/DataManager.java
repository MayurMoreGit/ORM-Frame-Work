package com.thinking.machines.ormframework.framework;
import com.thinking.machines.ormframework.framework.*;
import com.thinking.machines.ormframework.exceptions.*;
import com.thinking.machines.ormframework.annotations.*;
import com.thinking.machines.ormframework.utils.*;
import java.io.*;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;
import java.sql.*;
import java.lang.reflect.*;
import java.math.*;
import java.text.*;
public class DataManager
{
final private static DataManager dataManager;
private Map<String,Map<String,FieldInfoContainer>> map;
private Connection connection;
static
{
dataManager=new DataManager();
}
private DataManager() 
{
JSONParser parser=new JSONParser();
try
{
LinkedList <String> tableNames=new LinkedList<>();
this.map=new HashMap<>(); 
Object obj=parser.parse(new FileReader("conf.json"));
JSONObject jsonObject=(JSONObject)obj;
String jdbcDriver=(String)jsonObject.get("jdbc-driver");
String connectionURL=(String)jsonObject.get("connection-url");
String database=(String)jsonObject.get("database");
String username=(String)jsonObject.get("username");
String password=(String)jsonObject.get("password");
String packageName=(String)jsonObject.get("package-name");
String jarFileName=(String)jsonObject.get("jar-file-name");
String path=packageName.replaceAll("\\.","\\\\");
File file1=new File(path);
String absolutePath=file1.getAbsolutePath();
File file2=new File(absolutePath);
if(!file2.exists())
{
file2.mkdirs();
}
Class.forName(jdbcDriver);
this.connection=DriverManager.getConnection(connectionURL+database,username,password);
//JDBC metaData
DatabaseMetaData databaseMetaData=connection.getMetaData();
ResultSet resultSet = databaseMetaData.getTables(null, null, null, new String[]{"TABLE"});
Map<String,FieldInfoContainer> map1;
String tableName;
String columnName;
FieldInfoContainer fic;
ResultSet primaryKeys;
ResultSet columns;
ResultSet foreignKeys;
String primaryKeyColumnName;
String parentTableName;
String parentColumnName;
String isAutoIncrement=null;
while(resultSet.next())
{
map1=new HashMap<>();
tableName=resultSet.getString("TABLE_NAME");
columns=databaseMetaData.getColumns(null,null,tableName,null);
while(columns.next())
{
isAutoIncrement=columns.getString("IS_AUTOINCREMENT");
fic=new FieldInfoContainer();
fic.typeName=columns.getString("TYPE_NAME");
fic.columnSize=columns.getString("COLUMN_SIZE");
if(columns.getString("IS_NULLABLE").equals("YES"))fic.isNull=true;
if(isAutoIncrement.equals("YES"))fic.isAutoIncrement=true;
fic.datatype=columns.getString("DATA_TYPE");
columnName=columns.getString("COLUMN_NAME");
map1.put(columnName,fic);
}
primaryKeys=databaseMetaData.getPrimaryKeys(null, null,tableName); 
while(primaryKeys.next())
{ 
primaryKeyColumnName=primaryKeys.getString("COLUMN_NAME"); 
map1.get(primaryKeyColumnName).isPrimaryKey=true;
}
foreignKeys=databaseMetaData.getImportedKeys(null, null, tableName);
String fkColumnName=null;
while(foreignKeys.next())
{
fkColumnName=foreignKeys.getString("FKCOLUMN_NAME");
if(fkColumnName!=null)
{
fic=map1.get(fkColumnName);
fic.parentTableName=foreignKeys.getString("PKTABLE_NAME");
fic.parentColumnName=foreignKeys.getString("PKCOLUMN_NAME");
}
}
map.put(tableName,map1);
}
// code to create DTO's
String className;
String propertyName;
String sp[];
String g;
StringBuffer sb;
BufferedWriter bwr;
for(Map.Entry<String,Map<String,FieldInfoContainer>> entry : map.entrySet())
{
sb=new StringBuffer();
tableName=entry.getKey();
className=tableName.substring(0,1).toUpperCase()+tableName.substring(1);
sb.append("package "+packageName+";\n");
sb.append("import com.thinking.machines.ormframework.annotations.*;\n");
sb.append("import java.util.*;\n");
sb.append("import java.math.*;\n");
sb.append("@Table(name=\"");
sb.append(tableName);
sb.append("\")\n");
sb.append("public class ");
sb.append(className);
sb.append("{\n");
Map <String,FieldInfoContainer> m=entry.getValue();
for(Map.Entry<String,FieldInfoContainer> entry1 :m.entrySet())
{
sp=entry1.getKey().split("_");
propertyName=sp[0];
for(int i=1;i<sp.length;i++)
{
g=sp[i];
g=g.substring(0,1).toUpperCase()+g.substring(1);
propertyName+=g;
}
if(entry1.getValue().isPrimaryKey)
{
sb.append("@PrimaryKey\n");
}
if(entry1.getValue().isAutoIncrement)
{
sb.append("@AutoIncrement\n");
}
if(entry1.getValue().parentTableName!=null)
{
sb.append("@ForeignKey(parent=\"");
sb.append(entry1.getValue().parentTableName);
sb.append("\",column=\"");
sb.append(entry1.getValue().parentColumnName);
sb.append("\")\n");
}
sb.append("@Column(name=\"");
sb.append(entry1.getKey());
sb.append("\")\n");
sb.append("public ");
String datatype=entry1.getValue().typeName;
String fdt="";
if(datatype.equals("INT"))
{
fdt="int";
}
else if(datatype.equals("BIGINT"))
{
fdt="long ";
}
else if(datatype.equals("CHAR")||datatype.equals("VARCHAR"))
{
fdt="String";
}
else if(datatype.equals("FLOAT"))
{
fdt="float";
}
else if(datatype.equals("DATE") || datatype.equals("DATETIME"))
{
fdt="Date";
}
else if(datatype.equals("NUMERIC"))
{
datatype="Object";
}
else if(datatype.equals("DOUBLE"))
{
fdt="double";
}
else if(datatype.equals("DECIMAL") || datatype.equals("DEC"))
{
fdt="BigDecimal";
}
else if(datatype.equals("BIT") || datatype.equals("BOOL") || datatype.equals("BOOLEAN"))
{
fdt="boolean";
}
sb.append(fdt);
sb.append(" ");
sb.append(propertyName);
sb.append(";\n\n");
} 
sb.append("}");
bwr=new BufferedWriter(new FileWriter(new File(absolutePath+"\\"+className+".java")));
bwr.write(sb.toString());
bwr.flush();
bwr.close();
} 
//close connection
//here we will do the work of compiling generated classes and creating jar file
File file4=new File("dist");
if(!file4.exists()) 
{
file4.mkdir();
}
File file3=new File("");
Runtime runtime=Runtime.getRuntime();
String app1="javac -classpath ..\\lib\\*;. "+path+"\\*.java";
String app2="jar -cvf dist\\"+jarFileName+" "+path;
try
{
Process p1=runtime.exec(app1);
while(p1.isAlive())
{
//do nothing
}
Process p2=runtime.exec(app2);
}catch(Exception ee)
{
throw new DataException(ee.getMessage());
}
}catch(Exception e)
{
System.out.println(e.getMessage());
}
}
public static DataManager getDataManager() throws DataException
{
return dataManager;
}
public void begin() throws DataException
{
try
{
this.connection.setAutoCommit(false);
}catch(Exception e)
{
throw new DataException(e.getMessage());
}
}
public void end() throws DataException
{
try
{
this.connection.commit();
}catch(Exception e)
{
throw new DataException(e.getMessage());
}
}
public void rollback() throws SQLException
{
this.connection.rollback();
}
public void save(Object o) throws DataException
{
try
{
Class classObject=o.getClass();
Table tableAnnotation=(Table)classObject.getAnnotation(Table.class);
if(tableAnnotation==null)return;
String tableName=tableAnnotation.name();
String insertSqlStatement;
Field fields[]=classObject.getFields();
Map<String,Field> map1=new HashMap<>();
Field autoIncrementField=null;
for(Field f:fields)
{
if(f.getAnnotation(AutoIncrement.class)==null)
{
map1.put(f.getAnnotation(Column.class).name(),f);
}
else
{
autoIncrementField=f;
}
}
StringBuffer sb1=new StringBuffer();
StringBuffer sb2=new StringBuffer();
sb1.append("insert into ");
sb1.append(tableName);
sb1.append(" (");
sb2.append(" values(");
int i=1;
String columnName;
for(Map.Entry<String,Field> entry:map1.entrySet())
{
columnName=entry.getKey();
sb1.append(columnName);
sb2.append("?");
if(i<map1.size())
{
sb1.append(",");
sb2.append(",");
}
i++;
}
sb1.append(")");
sb2.append(");");
insertSqlStatement=sb1.toString()+sb2.toString();
PreparedStatement ps=this.connection.prepareStatement(insertSqlStatement,PreparedStatement.RETURN_GENERATED_KEYS);
i=1;
Map<String,Map<String,FieldInfoContainer>> m1=this.map;
Map<String,FieldInfoContainer> m2=m1.get(tableName);
FieldInfoContainer fic;
for(Map.Entry<String,Field> ee:map1.entrySet())
{
String cn=ee.getKey();
fic=m2.get(cn);
String dt=fic.typeName;
Field f=ee.getValue();
f.setAccessible(true);
if(dt.equalsIgnoreCase("INT"))
{
ps.setInt(i,(Integer)f.get(o));
}
else
if(dt.equalsIgnoreCase("bigint"))
{
ps.setLong(i,(Long)f.get(o));
}
else
if(dt.equalsIgnoreCase("Char")||dt.equalsIgnoreCase("varchar")||dt.equalsIgnoreCase("VARBINARY")||dt.equalsIgnoreCase("BINARY"))
{
ps.setString(i,(String)f.get(o));
}
else
if(dt.equalsIgnoreCase("float"))
{
ps.setFloat(i,(Float)f.get(o));
}
else
if(dt.equalsIgnoreCase("double"))
{
ps.setDouble(i,(Double)f.get(o));
}
else
if(dt.equalsIgnoreCase("date"))
{
if(f.getType().getSimpleName().equalsIgnoreCase("String"))
{
ps.setDate(i,java.sql.Date.valueOf((String)f.get(o)));
}
else
if(f.getType().getSimpleName().equalsIgnoreCase("Date"))
{
java.util.Date d=(java.util.Date)f.get(o);
ps.setDate(i,new java.sql.Date(d.getYear(),d.getMonth(),d.getDate()));
}
}
else
if(dt.equalsIgnoreCase("datetime"))
{
if(f.getType().getSimpleName().equalsIgnoreCase("String"))
{
ps.setTimestamp(i,java.sql.Timestamp.valueOf((String)f.get(o)));
}
else
if(f.getType().getSimpleName().equalsIgnoreCase("Date"))
{
ps.setTimestamp(i,new java.sql.Timestamp(((java.util.Date)f.get(o)).getTime()));
}
}
else
if(dt.equalsIgnoreCase("time"))
{
if(f.getType().getSimpleName().equalsIgnoreCase("String"))
{
ps.setTime(i, java.sql.Time.valueOf((String)f.get(o)));
}
else
if(f.getType().getSimpleName().equalsIgnoreCase("Date"))
{
ps.setTime(i,new java.sql.Time(((java.util.Date)f.get(o)).getTime()));
}
}
else
if(dt.equalsIgnoreCase("numeric"))
{
ps.setObject(i,f.get(o),java.sql.Types.NUMERIC);
}
else
if(dt.equalsIgnoreCase("bool")||dt.equalsIgnoreCase("boolean")||dt.equalsIgnoreCase("bit"))
{
ps.setBoolean(i,(Boolean)f.get(o));
}
else
if(dt.equalsIgnoreCase("decimal")||dt.equalsIgnoreCase("dec"))
{
ps.setBigDecimal(i,(BigDecimal)f.get(o));
}
i++;
}
ps.executeUpdate();
ResultSet rs=ps.getGeneratedKeys();
if(rs!=null && rs.next())
{
if(autoIncrementField!=null)
{
autoIncrementField.setAccessible(true);
autoIncrementField.set(o,rs.getInt(1));
}
}
}
catch(Exception e)
{
throw new DataException(e.getMessage());
}
}
public void update(Object o) throws DataException
{
try
{
Class classObject=o.getClass();
if(classObject.getAnnotation(Table.class)==null)
{
return;
}
String tableName=((Table)classObject.getAnnotation(Table.class)).name();
Field fields[]=classObject.getFields();
String updateSqlStatement;
StringBuffer sb=new StringBuffer();
sb.append("update ");
sb.append(tableName);
sb.append(" set ");
Field primaryKeyField=null;
String columnName=null;
Map<String,Field> m=new HashMap<>();
int i=1;
for(Field f:fields)
{
if(f.getAnnotation(PrimaryKey.class)!=null)
{
primaryKeyField=f;
}
if(f.getAnnotation(AutoIncrement.class)==null )
{
if(f.getAnnotation(PrimaryKey.class)!=null)
{
i++;
continue;
}
columnName=((Column)f.getAnnotation(Column.class)).name();
m.put(columnName,f);
if(columnName==null) return;//later stage exception is thrown
sb.append(columnName+"=?");
if(i<fields.length)
{
sb.append(",");
}
i++;
}
}//for ends

sb.append(" where ");
if(primaryKeyField==null) return;
if(primaryKeyField.getAnnotation(Column.class)==null) return;
String cn=((Column)primaryKeyField.getAnnotation(Column.class)).name();
m.put(cn,primaryKeyField);
sb.append(cn+"=?;");
updateSqlStatement=sb.toString();
PreparedStatement ps=this.connection.prepareStatement(updateSqlStatement);
i=1;
Map<String,Map<String,FieldInfoContainer>> m1=this.map;
Map<String,FieldInfoContainer> m2=m1.get(tableName);
FieldInfoContainer fic;
for(Map.Entry<String,Field> ee:m.entrySet())
{
cn=ee.getKey();
fic=m2.get(cn);
String dt=fic.typeName;
//System.out.println(dt);
Field f=ee.getValue();
f.setAccessible(true);
if(dt.equalsIgnoreCase("INT"))
{
ps.setInt(i,(Integer)f.get(o));
}
else
if(dt.equalsIgnoreCase("bigint"))
{
ps.setLong(i,(Long)f.get(o));
}
else
if(dt.equalsIgnoreCase("Char")||dt.equalsIgnoreCase("varchar")||dt.equalsIgnoreCase("VARBINARY")||dt.equalsIgnoreCase("BINARY"))
{
ps.setString(i,(String)f.get(o));
}
else
if(dt.equalsIgnoreCase("float"))
{
ps.setFloat(i,(Float)f.get(o));
}
else
if(dt.equalsIgnoreCase("double"))
{
ps.setDouble(i,(Double)f.get(o));
}
else
if(dt.equalsIgnoreCase("date"))
{
if(f.getType().getSimpleName().equalsIgnoreCase("String"))
{
ps.setDate(i,java.sql.Date.valueOf((String)f.get(o)));
}
else
if(f.getType().getSimpleName().equalsIgnoreCase("Date"))
{
java.util.Date d=(java.util.Date)f.get(o);
ps.setDate(i,new java.sql.Date(d.getYear(),d.getMonth(),d.getDate()));
}
}
else
if(dt.equalsIgnoreCase("datetime"))
{
if(f.getType().getSimpleName().equalsIgnoreCase("String"))
{
ps.setTimestamp(i,java.sql.Timestamp.valueOf((String)f.get(o)));
}
else
if(f.getType().getSimpleName().equalsIgnoreCase("Date"))
{
ps.setTimestamp(i,new java.sql.Timestamp(((java.util.Date)f.get(o)).getTime()));
}
}
else
if(dt.equalsIgnoreCase("time"))
{
if(f.getType().getSimpleName().equalsIgnoreCase("String"))
{
ps.setTime(i, java.sql.Time.valueOf((String)f.get(o)));
}
else
if(f.getType().getSimpleName().equalsIgnoreCase("Date"))
{
ps.setTime(i,new java.sql.Time(((java.util.Date)f.get(o)).getTime()));
}
}
else
if(dt.equalsIgnoreCase("numeric"))
{
ps.setObject(i,f.get(o),java.sql.Types.NUMERIC);
}
else
if(dt.equalsIgnoreCase("bool")||dt.equalsIgnoreCase("boolean")||dt.equalsIgnoreCase("bit"))
{
ps.setBoolean(i,(Boolean)f.get(o));
}
else
if(dt.equalsIgnoreCase("decimal")||dt.equalsIgnoreCase("dec"))
{
ps.setBigDecimal(i,(BigDecimal)f.get(o));
}
i++;
}
ps.executeUpdate();
}catch(Exception e)
{
throw new DataException(e.getMessage());
}
}
public void delete(Object o) throws DataException
{
try
{
Class classObject=o.getClass();
if(classObject.getAnnotation(Table.class)==null)
{
return;
}
String tableName=((Table)classObject.getAnnotation(Table.class)).name();
String deleteSqlStatement;
StringBuffer sb=new StringBuffer();
sb.append("delete from ");
sb.append(tableName);
sb.append(" where ");
Map<String,Field> m=new HashMap<>();
String cn;
Field primaryKeyField=null;
Field fields[]=classObject.getFields();
for(Field f:fields)
{
if(f.getAnnotation(Column.class)==null) return;
if(f.getAnnotation(PrimaryKey.class)!=null) primaryKeyField=f;
}
cn=((Column)primaryKeyField.getAnnotation(Column.class)).name();
sb.append(cn+"=?;");
deleteSqlStatement=sb.toString();
PreparedStatement ps=this.connection.prepareStatement(deleteSqlStatement);
int i=1;
Map<String,Map<String,FieldInfoContainer>> m1=this.map;
Map<String,FieldInfoContainer> m2=m1.get(tableName);
FieldInfoContainer fic;
fic=m2.get(cn);
String dt=fic.typeName;
Field f=primaryKeyField;
f.setAccessible(true);
if(dt.equalsIgnoreCase("INT"))
{
ps.setInt(i,(Integer)f.get(o));
}
else
if(dt.equalsIgnoreCase("bigint"))
{
ps.setLong(i,(Long)f.get(o));
}
else
if(dt.equalsIgnoreCase("Char")||dt.equalsIgnoreCase("varchar")||dt.equalsIgnoreCase("VARBINARY")||dt.equalsIgnoreCase("BINARY"))
{
ps.setString(i,(String)f.get(o));
}
else
if(dt.equalsIgnoreCase("float"))
{
ps.setFloat(i,(Float)f.get(o));
}
else
if(dt.equalsIgnoreCase("double"))
{
ps.setDouble(i,(Double)f.get(o));
}
else
if(dt.equalsIgnoreCase("date"))
{
if(f.getType().getSimpleName().equalsIgnoreCase("String"))
{
ps.setDate(i,java.sql.Date.valueOf((String)f.get(o)));
}
else
if(f.getType().getSimpleName().equalsIgnoreCase("Date"))
{
java.util.Date d=(java.util.Date)f.get(o);
ps.setDate(i,new java.sql.Date(d.getYear(),d.getMonth(),d.getDate()));
}
}
else
if(dt.equalsIgnoreCase("datetime"))
{
if(f.getType().getSimpleName().equalsIgnoreCase("String"))
{
ps.setTimestamp(i,java.sql.Timestamp.valueOf((String)f.get(o)));
}
else
if(f.getType().getSimpleName().equalsIgnoreCase("Date"))
{
ps.setTimestamp(i,new java.sql.Timestamp(((java.util.Date)f.get(o)).getTime()));
}
}
else
if(dt.equalsIgnoreCase("time"))
{
if(f.getType().getSimpleName().equalsIgnoreCase("String"))
{
ps.setTime(i, java.sql.Time.valueOf((String)f.get(o)));
}
else
if(f.getType().getSimpleName().equalsIgnoreCase("Date"))
{
ps.setTime(i,new java.sql.Time(((java.util.Date)f.get(o)).getTime()));
}
}
else
if(dt.equalsIgnoreCase("numeric"))
{
ps.setObject(i,f.get(o),java.sql.Types.NUMERIC);
}
else
if(dt.equalsIgnoreCase("bool")||dt.equalsIgnoreCase("boolean")||dt.equalsIgnoreCase("bit"))
{
ps.setBoolean(i,(Boolean)f.get(o));
}
else
if(dt.equalsIgnoreCase("decimal")||dt.equalsIgnoreCase("dec"))
{
ps.setBigDecimal(i,(BigDecimal)f.get(o));
}
ps.executeUpdate();
}
catch(Exception e)
{
throw new DataException(e.getMessage());
}
}
public Query query(Class tableClass) throws DataException
{
Query query=new Query(tableClass,this.connection);
return query;
}
}