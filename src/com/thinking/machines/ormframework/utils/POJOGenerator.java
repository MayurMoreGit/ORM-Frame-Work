package com.thinking.machines.ormframework.utils;
import com.thinking.machines.ormframework.framework.*;
import com.thinking.machines.ormframework.exceptions.*;
public class POJOGenerator
{
public static void generatePOJOEquivalentToTable()
{
try
{
DataManager dm=DataManager.getDataManager();
System.out.println("POJO's Generated");
}catch(DataException de)
{
}
}
}