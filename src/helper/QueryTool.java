package helper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class QueryTool {
	public static List<HashMap<String,Object>> resultSetToList(ResultSet rs) throws java.sql.SQLException {   
		if (rs == null)
			return Collections.emptyList();
		ResultSetMetaData md = rs.getMetaData(); // 得到结果集(rs)的结构信息，比如字段数、字段名等
		int columnCount = md.getColumnCount(); // 返回此 ResultSet 对象中的列数
		List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
		HashMap<String,Object> rowData = new HashMap<String,Object>();
		while (rs.next()) {
			rowData = new HashMap<String,Object>(columnCount);
			for (int i = 1; i <= columnCount; i++) {
				rowData.put(md.getColumnName(i),rs.getObject(i));
//				System.out.println("ColumnName"+md.getColumnName(i));
//				System.out.println("Object"+rs.getObject(i));
			}
			list.add(rowData);
		}
		rs.close();
		return list;
	}
}
