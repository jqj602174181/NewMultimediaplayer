package com.centerm.common;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class RWIniClass
{
	private static HashMap<String, IniEditorClass> iniEditorMap = 
			new HashMap<String,IniEditorClass>();//ini读写类的记录map
	
	synchronized static IniEditorClass loadIniFile( String fileName ) 
	{
		File file = new File( fileName );
  		if (!file.exists() )
  		{
  			iniEditorMap.remove( fileName );
  			return null;
  		}
		IniEditorClass iniEditor = iniEditorMap.get( fileName );
		if( iniEditor == null )
		{
  			try
  	  		{
  				iniEditor = new IniEditorClass();
  	  			//加载配置文件信息
  				iniEditor.load(fileName);
  				iniEditorMap.put( fileName, iniEditor );
  	  		} 
  	  		catch (IOException e)
  	  		{
  	  			e.printStackTrace();
  	  		}
		}
		return iniEditor;
	}
	
	//以字符串形式读取ini配置文件信息
  	public static String ReadIniString( String filename, String section, String key )
  	{
  	    //参数合法性判断
  		if (filename == null || section == null || key == null)
  		{
  			return null;
  		}

  		String value = null;
  		IniEditorClass iniEditor = loadIniFile(filename);//加载ini
  		if( iniEditor == null )//说明文件不存在
  		{
  			return null;
  		}
  		
  		//判断字段是否存在
		if (iniEditor.hasOption(section, key) == false)
		{
			return null;
		}
		
		//读取字段对应健值
		value = iniEditor.get(section, key);
  		return value;
  	}
  	
  	//字符串形式写ini配置文件信息
  	public static void WriteIniString( String filename, String section, String key, String value )
  	{	
  	    //参数合法性判断
  		if (filename == null || section == null || key == null || value == null)
  		{
  			return;
  		}
  		
  		String readValue = null;
  		IniEditorClass iniEditor = loadIniFile(filename);//加载ini  		
  		
  		if (iniEditor == null)//文件不存在，则创建该文件
  		{	
  			try 
  		    {
	  		    //文件上一目录不存在，创建新目录
	  			File file = new File(filename);
	  			if (file.getParentFile().exists() == false) 
	  			{
					file.getParentFile().mkdirs();
					Runtime.getRuntime().exec("chmod 0777 " + file.getParentFile().getAbsolutePath());
				}

  		    	file.createNewFile();
  		    	Runtime.getRuntime().exec("sync");
  		    	Runtime.getRuntime().exec("chmod 0777 " + filename);
  			} 
  		    catch (IOException e) 
  		    {
  				e.printStackTrace();
  			}
  		    iniEditor = loadIniFile(filename);//加载ini
  		}
  		else//文件存在，则直接读取，判断是否取值相同，如果相同，则不更改
  		{		
  			readValue = ReadIniString(filename, section, key);       
  			if ( readValue != null )
  			{
  				if ( readValue.equals(value) == true)
  	  			{
  	  				return;
  	  			}
  			}
  		}  		
  		
  	    //写入配置内容
  		try
  		{
  			if( iniEditor == null )
  			{
  				System.out.println( "Error: iniEditor is null!" );
  				return;
  			}
  		    //判断字段是否存在
  			if (iniEditor.hasSection(section) == false)
  			{
  				iniEditor.addSection(section);
  			}
  			
  			//设置并保存字段对应的健值信息
  			iniEditor.set(section, key, value);
  			iniEditor.save(filename);
  			
  			Runtime.getRuntime().exec("sync");
  		} 
  		catch (IOException e)
  		{
  			e.printStackTrace();
  		}
  	}
  	
    //以整数形式读取ini配置文件信息
  	public static int ReadIniInt( String filename, String section, String key, int defvalue )
  	{
  		String readValue = null;
  		readValue = ReadIniString(filename, section, key);
  		if (readValue == null)
  		{
  			return defvalue;
  		}
  		
  		//判断字符串为'0'-'9'字符串
  		for ( int i = 0; i < readValue.length(); i++ )
  		{
  			if (i == 0)
  			{
  				if (readValue.getBytes()[i] == '-')
  				{
  					continue;
  				}
  			}
  			if ((readValue.getBytes()[i]&0xFF) < 0x30 || (readValue.getBytes()[i]&0xFF) > 0x39)
  	  		{
  	  			return defvalue;
  	  		}
  		}
  		
  		return Integer.parseInt(readValue);
  	}
  	
    //以整数形式写ini配置文件信息
  	public static void WriteIniInt( String filename, String section, String key, int value )
  	{
  		String strvalue = String.valueOf(value);
  		WriteIniString( filename, section, key, strvalue );
  	}
  	
  	public static void resetIniMap( String filename )
  	{
  		File file = new File( filename );
  		if (file.exists() )
  		{
  			iniEditorMap.remove( filename );
  		}
  	}
}