package org.joget.tutorial;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.base.PluginWebSupport;
import org.json.JSONArray;
import org.json.JSONObject;

public class GetRequestNumberPR extends ExtDefaultPlugin implements PluginWebSupport{
	// call nya mnggunakan http://localhost:8080/jw/web/json/plugin/org.joget.tutorial.GetRequestNumberPR/service?requestNo=ID-000001
	private String requestNumber;
	private Connection conn;
	private JSONObject obj;

	@Override
	public String getDescription() {

		return "Plugin Untuk mencari Request Number";
	}

	@Override
	public String getName() {
		return "Webservice - GetRequestNumberPR";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}
	

	@Override
	public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		requestNumber = request.getParameter("requestNo");
		LogUtil.info(getClass().getName(), "Request No : " + requestNumber);
		DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
		try {
			conn = ds.getConnection();
			StringBuilder query = new StringBuilder();
			query.append("SELECT a.c_request_no, a.c_name, a.c_requestDate, b.c_category, a.c_items FROM app_fd_purchase_req a ");
			query.append("LEFT JOIN app_fd_purchase_cat b ON a.c_category=b.id ");
			query.append("WHERE c_request_no = ? ");
			
			PreparedStatement ps = null;
			ResultSet rs = null;
			ps = conn.prepareStatement(query.toString());
			ps.setString(1, requestNumber);
			rs = ps.executeQuery();
			while (rs.next()) {
				obj = new JSONObject();
				obj.put("requestNo", rs.getString("c_request_no"));
				obj.put("name", rs.getString("c_name"));
				obj.put("date", rs.getString("c_requestDate"));
				obj.put("category", rs.getString("c_category"));
				String strItems = rs.getString("c_items");
				JSONArray jsonArray = new JSONArray(strItems);
				obj.put("items", jsonArray);
			}
			obj.write(response.getWriter());
		}catch (Exception e) {
			LogUtil.error(getClass().getName(), e, e.getMessage());
			
		}finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LogUtil.error(getClass().getName(), e, e.getMessage());
				}
			}

		}
	}

}
