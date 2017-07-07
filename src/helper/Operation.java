package helper;

import java.sql.ResultSet;

public class Operation {
	public void createTable(String sql) throws Exception
	{
		Connector conn=new Connector();
		conn.execute(sql);
		conn.close();
	}
	public void dropTable(String sql) throws Exception
	{
		Connector conn=new Connector();
		conn.execute(sql);
		conn.close();
	}
	public void insert(String sql) throws Exception
	{
		Connector conn=new Connector();
		conn.execute(sql);
		conn.close();
	}
	public void modify(String sql) throws Exception
	{
		Connector conn=new Connector();
		conn.execute(sql);
		conn.close();
	}
	public void delete(String sql) throws Exception
	{
		Connector conn=new Connector();
		conn.execute(sql);
		conn.close();
	}
	public ResultSet select(Connector conn,String sql) throws Exception
	{
		ResultSet set= conn.executeQuery(sql);
		return set;
	}

}
