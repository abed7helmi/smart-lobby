package episen.si.ing1.pds.backend.server;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClientRequestManager {

	private final static Logger logger = LoggerFactory.getLogger(ClientRequestManager.class);
	private final PrintWriter output;
	private final BufferedReader input;
	private Connection c;
	private String name = "client-thread";
	private Thread self;
	private ObjectMapper mapper = new ObjectMapper(new JsonFactory());

	public ClientRequestManager(Socket socket, Connection connection) throws SQLException, IOException {
		c = connection;
		c.setAutoCommit(true);
		output = new PrintWriter(socket.getOutputStream(), true);
		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		self = new Thread(name) {
			@Override
			public void run() {
				try {
					resetData();

					String clientInput = input.readLine();
					String requestType = clientInput.split("#")[0];
					String values = clientInput.split("#")[1];

					Map<String, String> map = mapper.readValue(values,new TypeReference<Map<String, String>>(){});

					switch (requestType) {
						case "companyReservation":
							companyReservation(map);
							break;
						case "reservationFloor":
							reservationFloor(map);
							break;
						case "floorRoom":
							floorRoom(map);
							break;
						case "roomLocation":
							roomLocation(map);
							break;
						case "locationEquipment":
							locationEquipment(map);
							break;
						case "reservationEquipment":
							reservationEquipment(map);
							break;
						case "setEquipment":
							setEquipment(map);
							break;
						case "homePage1":
							firstPage(map);
							break;
						case "requestLocation1":
							getChoice(map);
							break;
						case "requestLocation2":
							getDevice(map);
							break;
						case "requestLocation3":
							getDisponibility(map);
							break;
						case "requestLocation5":
							getManagerId(map);
							break;
						case "requestLocation4":
							insertReservation(map);
							break;
						case "requestNewBadge":
							SaveBadge(map);
							break;
						case "getpermissions":
							getPermissions(map);
							break;
						case "testpermissions":
							testpermissions(map);
							break;
						case "getdevices":
							getdevices(map);
							break;
						case "requestDetailBadge":
							getDetailPermission(map);
							break;
						case "requestadddevice":
							adddevicepermission(map);
							break;
						case "requestallbadges":
							getallemployees(map);
							break;
						case "requestManyNewBadge":
							saveallemployees(map);
							break;
						case "requestWindow":
							getWindow(map);
							break;
						case "confWindow":
							confWindow(map);
							break;
						case "getbadges":
							getallbadges(map);
							break;


					}

					/*switch (requestType) {
					case "insert":
						StringBuilder request = new StringBuilder();
						request.append("insert into test(name,age) values");
						for (Map<String, String> m : map.values())
							request.append("('" + m.get("name") + "','" + m.get("age") + "'),");
						request.deleteCharAt(request.length() - 1);
						output.println("Successfully inserted " + c.createStatement().executeUpdate(request.toString())
								+ " rows.");
						break;
					case "select":
						StringBuilder sb = new StringBuilder();
						ResultSet result = c.createStatement().executeQuery("select * from test");
						while (result.next()) {
							sb.append("id=" + result.getInt(1) + ",name=" + result.getString(2) + ",age=" + result.getInt(3) +"|");
						}
						output.println(sb.toString());
						break;
					case "update":
						int newAge = Integer.valueOf(map.get("toto").get("age"))+1;
						output.println(
								"Successfully updated "
										+ c.createStatement()
												.executeUpdate("update test set age=" + newAge
														+ " where name='" + map.get("toto").get("name") + "'")
										+ " rows.");
						break;
					case "delete":
						output.println("Successfully deleted " + c.createStatement().executeUpdate("delete from test")
								+ " rows.");
						break;
					default:
						output.println("Invalid request type.");
						break;
					}*/
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		self.start();
	}

	public Thread getSelf() {
		return self;
	}

	public void companyReservation(Map<String,String> map) {
		try {
			String request = "Select reservation_id,start_date,end_date,price,employee_last_name,employee_first_name from reservation inner join employee on (reservation.gs_manager_id = employee.employee_id) where company_id="+map.get("company_id");
			ResultSet rs = c.createStatement().executeQuery(request);
			Map<String,Map<String, String>> result = new HashMap<String,Map<String, String>>();
			int i=0;
			while(rs.next()) {
				Map<String, String> resultmap = new HashMap<String, String>();
				resultmap.put("reservation_id", rs.getString(1));
				resultmap.put("start_date", rs.getString(2));
				resultmap.put("end_date", rs.getString(3));
				resultmap.put("price", rs.getString(4));
				resultmap.put("employee_last_name", rs.getString(5));
				resultmap.put("employee_first_name", rs.getString(6));
				result.put(""+i++, resultmap);
			}
			output.println(mapper.writeValueAsString(result));
		} catch (JsonMappingException e) {} catch (JsonProcessingException e) {} catch (SQLException e) {}

	}
	public void getWindow(Map<String,String> map) throws SQLException {
		System.out.println("ok");
		try {
			ObjectMapper mapper = new ObjectMapper(new JsonFactory());
			String request="select device.device_id from device inner join windows on(device.device_id=windows.device_id);";
			ResultSet rs = c.createStatement().executeQuery(request);
			Map<String,Map<String, String>> result = new HashMap<String,Map<String, String>>();
			int i=0;
			while(rs.next()) {
				Map<String, String> tab = new HashMap<String, String>();
				tab.put("device_id", rs.getString(1));
				result.put(""+i++, tab);
			}System.out.println("ok");
			output.println(mapper.writeValueAsString(result));
		} catch (JsonMappingException e) {} catch (JsonProcessingException e) {}
	}
	public void confWindow(Map<String,String> map) throws SQLException {

		try {
			ObjectMapper mapper = new ObjectMapper(new JsonFactory());
			String request="select * from windows where device-id='"+map.get("device_id")+"';";
			ResultSet rs = c.createStatement().executeQuery(request);
			Map<String,Map<String, String>> result = new HashMap<String,Map<String, String>>();
			int i=0;
			while(rs.next()) {
				Map<String, String> resultmap = new HashMap<String, String>();
				resultmap.put("device_id", rs.getString(1));
				resultmap.put("outside_temperature", rs.getString(2));
				resultmap.put("inside_temperature", rs.getString(3));
				resultmap.put("outside_luminosity", rs.getString(4));
				resultmap.put("inside_luminosity", rs.getString(5));
				resultmap.put("percentage_store", rs.getString(6));
				resultmap.put("percentage_tint", rs.getString(7));
				result.put(""+i++, resultmap);
			}System.out.println("ok");
			output.println(mapper.writeValueAsString(result));
		} catch (JsonMappingException e) {} catch (JsonProcessingException e) {}
	}

	public void reservationFloor(Map<String,String> map) {
		try {
			String request = "select distinct(floor_id),floor_number,building_name from (room natural join floor) natural join building where reservation_id="+map.get("reservation_id");
			ResultSet rs = c.createStatement().executeQuery(request);
			Map<String,Map<String, String>> result = new HashMap<String,Map<String, String>>();
			int i=0;
			while(rs.next()) {
				Map<String, String> resultmap = new HashMap<String, String>();
				resultmap.put("floor_id", rs.getString(1));
				resultmap.put("floor_number", rs.getString(2));
				resultmap.put("building_name", rs.getString(3));
				result.put(""+i++, resultmap);
			}
			output.println(mapper.writeValueAsString(result));
		} catch (JsonMappingException e) {} catch (JsonProcessingException e) {} catch (SQLException e) {}

	}

	public void floorRoom(Map<String,String> map) {
		try {
			String request = "select room_id,room_wording,room_type_id from room where reservation_id="+map.get("reservation_id")+" and floor_id="+map.get("floor_id");
			ResultSet rs = c.createStatement().executeQuery(request);
			Map<String,Map<String, String>> result = new HashMap<String,Map<String, String>>();
			int i=0;
			while(rs.next()) {
				Map<String, String> resultmap = new HashMap<String, String>();
				resultmap.put("room_id", rs.getString(1));
				resultmap.put("room_wording", rs.getString(2));
				resultmap.put("room_type_id", rs.getString(3));
				result.put(""+i++, resultmap);
			}
			output.println(mapper.writeValueAsString(result));
		} catch (JsonMappingException e) {} catch (JsonProcessingException e) {} catch (SQLException e) {}

	}

	public void roomLocation(Map<String,String> map) {
		try {
			String request = "select location_id,occupied_location,is_sensor,x_position,y_position from location where room_id="+map.get("room_id");
			ResultSet rs = c.createStatement().executeQuery(request);
			Map<String,Map<String, String>> result = new HashMap<String,Map<String, String>>();
			int i=0;
			while(rs.next()) {
				Map<String, String> resultmap = new HashMap<String, String>();
				resultmap.put("location_id", rs.getString(1));
				resultmap.put("occupied_location", rs.getString(2));
				resultmap.put("is_sensor", rs.getString(3));
				resultmap.put("x_position", rs.getString(4));
				resultmap.put("y_position", rs.getString(5));
				result.put(""+i++, resultmap);
			}
			output.println(mapper.writeValueAsString(result));
		} catch (JsonMappingException e) {} catch (JsonProcessingException e) {} catch (SQLException e) {}

	}

	public void locationEquipment(Map<String,String> map) {
		try {
			String request = "select device_id,device_wording,device_active,device_price,reservation_id from device where location_id="+map.get("location_id");
			ResultSet rs = c.createStatement().executeQuery(request);
			Map<String, String> result = new HashMap<String, String>();
			while(rs.next()) {
				result.put("device_id", rs.getString(1));
				result.put("device_wording", rs.getString(2));
				result.put("device_active", rs.getString(3));
				result.put("device_price", rs.getString(4));
			}
			output.println(mapper.writeValueAsString(result));
		} catch (JsonMappingException e) {} catch (JsonProcessingException e) {} catch (SQLException e) {}

	}

	public void reservationEquipment(Map<String,String> map) {
		try {
			String request = "";
			if(map.get("is_sensor").equals("f")) {
				request = "select device_id,device_wording,device_price from device where device_type_id in (select device_type_id from could_configure where room_type_id=(select room_type_id from room where room_id="+map.get("room_id")+")) and device_type_id<11 and location_id is null and reservation_id="+map.get("reservation_id");
			}else {
				request = "select device_id,device_wording,device_price from device where device_type_id in (select device_type_id from could_configure where room_type_id=(select room_type_id from room where room_id="+map.get("room_id")+")) and device_type_id>10 and location_id is null and reservation_id="+map.get("reservation_id");
			}
			ResultSet rs = c.createStatement().executeQuery(request);
			Map<String,Map<String, String>> result = new HashMap<String,Map<String, String>>();
			int i=0;
			while(rs.next()) {
				Map<String, String> resultmap = new HashMap<String, String>();
				resultmap.put("device_id", rs.getString(1));
				resultmap.put("device_wording", rs.getString(2));
				resultmap.put("device_price", rs.getString(3));
				result.put(""+i++, resultmap);
			}
			output.println(mapper.writeValueAsString(result));
		} catch (JsonMappingException e) {} catch (JsonProcessingException e) {} catch (SQLException e) {}

	}

	public void setEquipment(Map<String,String> map) {
		try {

			String location = map.get("location_id");
			String newDevice = map.get("new_device_id");
			String oldDevice = map.get("old_device_id");

			Statement s = c.createStatement();
			if(oldDevice.isEmpty()) {
				s.executeUpdate("update device set device_placed='t', location_id="+location+" where device_id="+newDevice);
				s.executeUpdate("update location set occupied_location='t' where location_id="+location);
			}else if(newDevice.isEmpty()) {
				s.executeUpdate("update device set device_placed='f', location_id=null where device_id="+oldDevice);
				s.executeUpdate("update location set occupied_location='f' where location_id="+location);
			}else {
				s.executeUpdate("update device set device_placed='t', location_id="+location+" where device_id="+newDevice);
				s.executeUpdate("update device set device_placed='f', location_id=null where device_id="+oldDevice);
			}

			output.println("Done");
		} catch (SQLException e) {}
	}

	public void getChoice(Map<String,String> map){
		try {
			int numberOpenSpace = Integer.parseInt(map.get("numberOpenSpace")) * 4;
			int numberClosedOffice = Integer.parseInt(map.get("numberClosedOffice")) *4;
			int numberSingleOffice = Integer.parseInt(map.get("numberSingleOffice")) * 4;
			int numberMeetingRoom = Integer.parseInt(map.get("numberMeetingRoom")) * 4;

			String request = "select room_wording, floor_number, building_name, room_price as prix, room_id, room_type_id " +
					"from room r " +
					"inner join floor f on f.floor_id = r.floor_id " +
					"inner join building b on b.building_id = f.building_id " +
					"where room_id in "+
					"(select room_id " +
					"from room r " +
					"inner join floor f on f.floor_id = r.floor_id " +
					"inner join building b on b.building_id = f.building_id " +
					"where status = 'free' and room_type_id = 1 ";
			if( map.keySet().contains("location") && !(map.get("location").equals("")) )	request	= request + " and position = '" + map.get("location")+"' ";
			request = request + "Limit " + numberOpenSpace + ")"+
					"or room_id in "+
					" (select room_id "+
					"from room r " +
					"inner join floor f on f.floor_id = r.floor_id " +
					"inner join building b on b.building_id = f.building_id " +
					"where status = 'free' and room_type_id = 3 ";
			if( map.keySet().contains("location")  && !(map.get("location").equals("")) )	request	= request + " and position = '" + map.get("location")+"' ";
			request = request + "Limit " + numberClosedOffice + ")"+
					"or room_id in "+
					"(select room_id " +
					"from room r " +
					"inner join floor f on f.floor_id = r.floor_id " +
					"inner join building b on b.building_id = f.building_id " +
					"where status = 'free' and room_type_id = 4 " ;
			if( map.keySet().contains("location") && !(map.get("location").equals(""))  )	request	= request + " and position = '" + map.get("location")+"' ";
			request = request + "Limit " + numberSingleOffice + ")"+
					"or room_id in " +
					"(select room_id " +
					"from room r " +
					"inner join floor f on f.floor_id = r.floor_id " +
					"inner join building b on b.building_id = f.building_id " +
					"where status = 'free' and room_type_id = 2 ";
			if( map.keySet().contains("location")  && !(map.get("location").equals(""))  )	request	= request + " and position = '" + map.get("location")+"' ";
			request = request +"Limit " + numberMeetingRoom +") " +
					" order by room_price;";


			ResultSet result = c.createStatement().executeQuery(request);
			Map<String, Map<String, String>> roomProposal1 = new HashMap<>();
			Map<String, Map<String, String>> roomProposal2 = new HashMap<>();
			Map<String, Map<String, String>> roomProposal3 = new HashMap<>();
			Map<String, Map<String, String>> roomProposal4 = new HashMap<>();
			Map<String , Map<String, Map<String ,String>>> proposal = new HashMap<>();

			int numberRoom = Integer.parseInt(map.get("numberClosedOffice"))
					+ Integer.parseInt(map.get("numberSingleOffice"))
					+ Integer.parseInt(map.get("numberOpenSpace"))
					+ Integer.parseInt(map.get("numberMeetingRoom"));

			int countOpenSpaceProposal1 = 0, countMeetingRoomProposal1 = 0, countClosedOfficeProposal1 = 0, countSingleOfficeProposal1 = 0;
			int countOpenSpaceProposal2 = 0,countMeetingRoomProposal2 = 0,countClosedOfficeProposal2 = 0,countSingleOfficeProposal2 = 0;
			int countOpenSpaceProposal3 = 0,countMeetingRoomProposal3 = 0,countClosedOfficeProposal3 = 0,countSingleOfficeProposal3 = 0;
			int countOpenSpaceProposal4 = 0,countMeetingRoomProposal4 = 0,countClosedOfficeProposal4 = 0,countSingleOfficeProposal4 = 0;

			int countRoomProposal1 =1,countRoomProposal2 =1,countRoomProposal3 =1,countRoomProposal4 =1;

			while(result.next()){
				Map<String, String> underMap = new HashMap<>();
				underMap.put("room_wording",result.getString(1));
				underMap.put("floor_number",result.getInt(2)+"");
				underMap.put("building_name",result.getString(3));
				underMap.put("price",result.getString(4));
				underMap.put("room_id",result.getString(5));
				underMap.put("room_type_id",result.getString(6));

				if( result.getInt(6) == 4 ){
					if(countSingleOfficeProposal1 < Integer.parseInt(map.get("numberSingleOffice") )) {
						roomProposal1.put("roomSingleOffice"+countRoomProposal1,underMap);
						countSingleOfficeProposal1++;
					} else if (countSingleOfficeProposal2 < Integer.parseInt(map.get("numberSingleOffice") )) {
						roomProposal2.put("roomSingleOffice"+countRoomProposal2,underMap);
						countSingleOfficeProposal2++;
					} else if(countSingleOfficeProposal3 < Integer.parseInt(map.get("numberSingleOffice") )) {
						roomProposal3.put("roomSingleOffice"+countRoomProposal3,underMap);
						countSingleOfficeProposal3++;
					} else if(countSingleOfficeProposal4 < Integer.parseInt(map.get("numberSingleOffice") )) {
						roomProposal4.put("roomSingleOffice"+countRoomProposal4,underMap);
						countSingleOfficeProposal4++;
					}
				}
				if( result.getInt(6) == 3 ){
					if(countClosedOfficeProposal1 < Integer.parseInt(map.get("numberClosedOffice") )) {
						roomProposal1.put("roomClosedOffice"+countRoomProposal1,underMap);
						countClosedOfficeProposal1++;
					}else if (countClosedOfficeProposal2 < Integer.parseInt(map.get("numberClosedOffice") )) {
						roomProposal2.put("roomClosedOffice"+countRoomProposal2,underMap);
						countClosedOfficeProposal2++;
					}else if(countClosedOfficeProposal3 < Integer.parseInt(map.get("numberClosedOffice") )) {
						roomProposal3.put("roomClosedOffice"+countRoomProposal3,underMap);
						countClosedOfficeProposal3++;
					}else if(countClosedOfficeProposal4 < Integer.parseInt(map.get("numberClosedOffice") )) {
						roomProposal4.put("roomClosedOffice"+countRoomProposal4,underMap);
						countClosedOfficeProposal4++;
					}
				}
				if( result.getInt(6) == 2 ){
					if(countMeetingRoomProposal1 < Integer.parseInt(map.get("numberMeetingRoom") )) {
						roomProposal1.put("roomMeetingRoom"+countRoomProposal1,underMap);
						countMeetingRoomProposal1++;
					}else if (countMeetingRoomProposal2 < Integer.parseInt(map.get("numberMeetingRoom") )) {
						roomProposal2.put("roomMeetingRoom"+countRoomProposal2,underMap);
						countMeetingRoomProposal2++;
					}else if(countMeetingRoomProposal3 < Integer.parseInt(map.get("numberMeetingRoom") )) {
						roomProposal3.put("roomMeetingRoom"+countRoomProposal3,underMap);
						countMeetingRoomProposal3++;
					}else if(countMeetingRoomProposal4 < Integer.parseInt(map.get("numberMeetingRoom") )) {
						roomProposal4.put("roomMeetingRoom"+countRoomProposal4,underMap);
						countMeetingRoomProposal4++;
					}
				}
				if( result.getInt(6) == 1 ){
					if(countOpenSpaceProposal1 < Integer.parseInt(map.get("numberOpenSpace") )) {
						roomProposal1.put("roomOpenSpace"+countRoomProposal1,underMap);
						countOpenSpaceProposal1++;
					}else if (countOpenSpaceProposal2 < Integer.parseInt(map.get("numberOpenSpace") )) {
						roomProposal2.put("roomOpenSpace"+countRoomProposal2,underMap);
						countOpenSpaceProposal2++;
					}else if(countOpenSpaceProposal3 < Integer.parseInt(map.get("numberOpenSpace") )) {
						roomProposal3.put("roomOpenSpace"+countRoomProposal3,underMap);
						countOpenSpaceProposal3++;
					}else if(countOpenSpaceProposal4 < Integer.parseInt(map.get("numberOpenSpace") )) {
						roomProposal4.put("roomOpenSpace"+countRoomProposal4,underMap);
						countOpenSpaceProposal4++;
					}
				}
				if(countRoomProposal1 == numberRoom) countRoomProposal1 = 1;
				else countRoomProposal1++;
				if(countRoomProposal2 == numberRoom) countRoomProposal2 = 1;
				else countRoomProposal2++;
				if(countRoomProposal3 == numberRoom) countRoomProposal3 = 1;
				else countRoomProposal3++;
				if(countRoomProposal4 == numberRoom) countRoomProposal4 = 1;
				else countRoomProposal4++;
			}
			proposal.put("proposal1", roomProposal1);
			proposal.put("proposal2", roomProposal2);
			proposal.put("proposal3", roomProposal3);
			proposal.put("proposal4", roomProposal4);

			ObjectMapper objectMapper = new ObjectMapper();
			String proposals = objectMapper.writeValueAsString(proposal);

			output.println(proposals);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void firstPage(Map<String,String> map) {
		try {
			ResultSet result = c.createStatement().executeQuery("select company_name,company_id from company " +
					"where company_name = '"+ map.get("company_name") +"';");

			if(result.next()) {
				String data = result.getString(1)+ ","+result.getString(2);
				output.println(data);
			} else output.println("false,");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getDevice(Map<String,String> map){
		try {
			int room_id = Integer.parseInt(map.get("room_id"));
			String request = "select distinct device_wording, device_type_wording, device_price " +
					"from device d " +
					"inner join device_type dt on dt.device_type_id = d.device_type_id "+
					"where d.device_type_id in(" +
					"select dt.device_type_id " +
					"from device_type dt " +
					"inner join could_configure cc on cc.device_type_id = dt.device_type_id " +
					"inner join room_type rt on rt.room_type_id = cc.room_type_id " +
					"inner join room r on r.room_type_id = rt.room_type_id " +
					"where room_id = "+ room_id +");";

			ResultSet result = c.createStatement().executeQuery(request);
			StringBuilder sb = new StringBuilder();
			while(result.next()) sb.append(result.getString(1)+ " -- "+ result.getString(3) + "euros /      "+ result.getString(2)+ ",");

			output.println(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getDisponibility(Map<String,String> map){
		try {
			int quantity = Integer.parseInt(map.get("quantity"));
			String device = map.get("device_wording");

			String exceptId = " ";
			if(map.size() > 2){
				for(Map.Entry m : map.entrySet()){
					if( !((m.getKey()+"").equals("device_wording")) && !((m.getKey()+"").equals("quantity")) ){
						exceptId = exceptId + " and device_id <> " + m.getValue() + " ";
					}
				}
			}

			String request = "  select device_id " +
					"  from device d " +
					"  where device_wording ='"+ device +"' and device_status = 'free' ";
			if( !(exceptId.equals(" ")) ) request = request + exceptId;

			request = request + "  limit "+ quantity +"  ;";

			ResultSet result = c.createStatement().executeQuery(request);

			StringBuilder strB = new StringBuilder();
			while(result.next()){
				strB.append(result.getInt(1)+ ",");
			}
			output.println(strB.toString());
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getManagerId(Map<String,String> map){
		try {
			String request = "select gs_manager_id " +
					"from general_services_manager g " +
					"inner join employee e on g.gs_manager_id = e.employee_id " +
					"where company_id = "+ map.get("company_id") +";";
			ResultSet result = c.createStatement().executeQuery(request);

			String companyId ="";
			while(result.next()){
				companyId = result.getString(1);
			}
			output.println(companyId.toString());
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void insertReservation(Map<String,String> map){
		try {
			c.setAutoCommit(true);

			String requestInsert = " insert into reservation (end_date, start_date, price, gs_manager_id) "+
					" values ('" + map.get("end_date") + "', '"+ map.get("start_date")+
					"', '"+ map.get("price") + "', '"+ map.get("gs_manager_id")+ "'); ";

			int i = 0;
			String verifyDataRoom =" select status from room where ";
			String verifyRequestUpdateDevice = "select device_status from device where ";
			List verifyDeviceId = new ArrayList<>();

			String whereRequestUpdateRoom = "where ";
			String requestUpdateDevice = "";
			StringBuffer verifyDevice;
			for(Map.Entry m : map.entrySet()) {
				if((m.getKey()+"").contains("room")){
					whereRequestUpdateRoom = whereRequestUpdateRoom + " room_id = " + m.getValue() + " or ";
					verifyDataRoom = verifyDataRoom + " room_id = " + m.getValue() + " or ";
				}

				if( !((m.getKey()+"").equals("")) ){
					String key = (m.getKey()+"").trim();
					if( key.matches("\\d+")  ){

						String deviceId = m.getValue()+"";
						deviceId = deviceId.replace("[", "");
						deviceId = deviceId.replace("]","");

						String[] listDeviceId = deviceId.split(",");
						if( listDeviceId.length != 1 ){
							String whereRequestUpdateDevice =" where";
							for(int y = 0; y < listDeviceId.length; y++){
								verifyDeviceId.add(listDeviceId[y]);
								whereRequestUpdateDevice = whereRequestUpdateDevice + " device_id = " + listDeviceId[y]+ " or ";
							}
							verifyDevice = new StringBuffer(whereRequestUpdateDevice);
							if(whereRequestUpdateDevice.contains("or")){
								verifyDevice.delete(verifyDevice.length() - 4, verifyDevice.length());
								verifyDevice.append(";");
							}
							requestUpdateDevice = requestUpdateDevice + " update device "+
									"set device_status = 'booked', "+
									"reservation_id = (select max(reservation_id) from reservation), "+
									" room_id = " + m.getKey()+ " "+ verifyDevice;
						}
					}
				}
			}
			StringBuffer verifyRoom = new StringBuffer(verifyDataRoom);
			verifyRoom.delete(verifyRoom.length() - 4, verifyRoom.length());
			verifyRoom.append(";");

			ResultSet resultRoom = c.createStatement().executeQuery(verifyRoom+"");

			boolean verifyDataDB = true;
			while(resultRoom.next()){
				if(resultRoom.getString(1).equals("booked"))	verifyDataDB = false;
			}

			for(int k = 0; k < verifyDeviceId.size(); k++){
				if( !(verifyDeviceId.get(k).equals("")) ) verifyRequestUpdateDevice = verifyRequestUpdateDevice + " device_id = " + verifyDeviceId.get(k)+ " or ";
			}

			if(verifyRequestUpdateDevice.contains("or")){
				StringBuffer verifyRoomUpdateDevice = new StringBuffer(verifyRequestUpdateDevice);
				verifyRoomUpdateDevice.delete(verifyRoomUpdateDevice.length() - 4, verifyRoomUpdateDevice.length());
				verifyRoomUpdateDevice.append(";");
				ResultSet resultDevice = c.createStatement().executeQuery(verifyRoomUpdateDevice+"");

				while(resultDevice.next()){
					if(resultDevice.getString(1).equals("booked")) verifyDataDB = false;
				}
			}

			if(verifyDataDB){
				StringBuffer correctWhere = new StringBuffer(whereRequestUpdateRoom);
				correctWhere.delete(correctWhere.length() - 4, correctWhere.length());
				correctWhere.append(";");

				String requestUpdateRoom = "update room "+
						"set status = 'booked', "+
						"    reservation_id = (select max(reservation_id) from reservation)  "+ correctWhere;

				String requestOrder = requestInsert + requestUpdateRoom + requestUpdateDevice;
				output.println("Successfully updated "+ c.createStatement().executeUpdate(requestOrder));
			} else output.println("Not done");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void resetData(){
		try{
			String request = "select reservation_id from reservation where end_date <= current_date";
			ResultSet result = c.createStatement().executeQuery(request);

			while(result.next()){
				request = "update device set device_status = 'free', reservation_id = null, room_id = null\n" +
						"  where reservation_id = "+  result.getString(1) + "; "+
						"  update room set reservation_id = null, " +
						"  status = 'free' where reservation_id =" +  result.getString(1) + "; " +
						"  delete from reservation where reservation_id = " +  result.getString(1) + ";";
				c.createStatement().executeUpdate(request);
			}
		}catch (Exception e){}

	}










	public void getDetailPermission(Map<String, String> map){
		logger.debug("testdetail");
		try {


			int id_company=Integer.parseInt(map.get("company_id"));
			String permission=map.get("permission");
			String per[]=permission.split(",");
			int idpermission=Integer.parseInt(getNbr(per[0]));
			String namepermission=per[1].split(":")[1];



			ResultSet result = c.createStatement().executeQuery("select device.device_id,device_wording,device_active,device.room_id,permission_device.number_validity_use,equipement_validity_period " +
					"from permission_device inner join device on device.device_id=permission_device.device_id where permission_device.permission_id='"+ idpermission +"';");

			logger.debug("c bon");

			StringBuilder sb = new StringBuilder();

			sb.append(idpermission);

			sb.append("//");
			sb.append(namepermission);
			sb.append("//");



			while (result.next()) {

				///sb.append("device_id=" + result.getInt(1) + ",device_wording=" + result.getString(2) + ",device_active=" + result.getBoolean(3) + ",room_id=" + result.getInt(4) +",number_validity_use=" + result.getInt(5) +",equipement_validity_period="+ result.getDate(6) +"#");
				sb.append(result.getInt(1) + "," + result.getString(2) + "," + result.getBoolean(3) + "," + result.getInt(4) +"," + result.getInt(5) +","+ result.getDate(6) +","+result.getInt(1)+"#");
			}



			ResultSet result2 = c.createStatement().executeQuery("select device.device_id,device.device_wording,device.room_id from device inner join reservation on reservation.reservation_id=device.reservation_id inner join general_services_manager on general_services_manager.gs_manager_id=reservation.gs_manager_id inner join employee on employee.employee_id=general_services_manager.gs_manager_id where employee.company_id='" + id_company+"';");

			sb.append("//");

			while (result2.next()) {

				sb.append("device_id=" + result2.getInt(1) + ",device_wording=" + result2.getString(2) + ",room_id=" + result2.getInt(3) +"#");
			}

			output.println(sb.toString());



		}catch (Exception e){
			output.println("notgood");
			e.printStackTrace();
		}

	}



	static String getNbr(String str)
	{

		str = str.replaceAll("[^\\d]", " ");
		str = str.trim();
		str = str.replaceAll(" +", " ");
		return str;
	}




	public void SaveBadge(Map<String, String> map){
		logger.debug("bravo : test save4");
		try {


			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String name1 =map.get("nomemploye");
			String name2=map.get("prenomemploye");
			String idbadge=map.get("puceemploye");
			Date today = dateFormat.parse(dateFormat.format(new Date()));
			int agent=Integer.parseInt(map.get("idagent"));
			Date dateendbadge = dateFormat.parse(map.get("badge_date"));
			Date datecontract = dateFormat.parse(map.get("contract_date"));
			Date datepermission = dateFormat.parse(map.get("permission_date"));
			String emailagent=map.get("emailagent");
			String permission=map.get("permission");
			int id_company=Integer.parseInt(map.get("company_id"));
			String state="En Fonction";
			int id=0;






			String requestInsert = "insert into employee (employee_last_name,employee_first_name,company_id,contract_duration) values ('"+name1+"','"+name2+"','"+id_company+"','"+datecontract +"') ;";

			c.createStatement().executeUpdate(requestInsert);
			ResultSet lastemployee = c.createStatement().executeQuery("select employee.employee_id from employee order by employee.employee_id DESC LIMIT 1; ");

			while (lastemployee.next()){
				id=lastemployee.getInt(1);
			}

			ResultSet agentid = c.createStatement().executeQuery("select general_services_manager.gs_manager_id from general_services_manager where gs_manager_id = '"+ agent+"';");

			int id2;
			if (agentid.next())
				while (agentid.next()){
					id2=lastemployee.getInt(1);
					String requestupdate ="update general_services_manager set manager_email ='"+ emailagent +"'where gs_manager_id='"+id2   +" ';";
					c.createStatement().executeQuery(requestupdate);

				}
			else{output.println("notgood id agent");}


			String requestInsert2 = "insert into badge (badge_id,badge_start_date,badge_end_date,badge_state,employee_id,gs_manager_id) " +
					"values ('"+idbadge+"','"+today+"','"+dateendbadge+"','"+state +"','"+id+"','"+agent+"') ;";

			try{
				c.createStatement().executeUpdate(requestInsert2);
			}catch (Exception e) {
				output.println("notgood badge");
			}



			String per[]=permission.split(",");
			int idpermission=Integer.parseInt(getNbr(per[0]));
			String insertrequest = "insert into permission_access (badge_id,permission_id,permission_validity_period) VALUES ('"+idbadge+"','"+idpermission+"','"+datepermission+"') ;";
			try {
				c.createStatement().executeUpdate(insertrequest);
			}catch (Exception e){
				output.println("notgood permision");
			}




			output.println("good");
		}catch (Exception e){
			output.println("notgood");
			e.printStackTrace();
		}

	}




	public void getdevices(Map<String, String> map){
		logger.debug("bravo2");
		try {





			ResultSet result = c.createStatement().executeQuery("select device.device_id,device.device_wording,device.room_id from device inner join reservation on reservation.reservation_id=device.reservation_id\n" +
					"inner join general_services_manager on general_services_manager.gs_manager_id=reservation.gs_manager_id\n" +
					"inner join employee on employee.employee_id=general_services_manager.gs_manager_id where employee.company_id='" +
					Integer.parseInt(map.get("company_id"))+"';");



			StringBuilder sb = new StringBuilder();
			while (result.next()) {

				sb.append("device_id=" + result.getInt(1) + ",device_wording=" + result.getString(2) + ",room_id=" + result.getInt(3) +"#");
			}

			output.println(sb.toString());



		}catch (Exception e){
			output.println("notgood");
			e.printStackTrace();
		}

	}




	public void getallbadges(Map<String, String> map){
		logger.debug("getallbadges test");
		try {

			int company = Integer.parseInt(map.get("company_id"));
			logger.debug("getallbfds" +company);
			ResultSet result = c.createStatement().executeQuery("select distinct (employee.employee_id),employee.employee_last_name,permission_badge.name_permission,permission_access.permission_validity_period from employee inner join badge on badge.employee_id=employee.employee_id inner join permission_access on permission_access.badge_id=badge.badge_id inner join permission_badge on permission_badge.permission_id=permission_access.permission_id inner join permission_device on permission_device.permission_id=permission_badge.permission_id where (employee.company_id='"+ company + "'  );");





			logger.debug("bravo test");

			StringBuilder sb = new StringBuilder();
			while (result.next()) {

				sb.append(result.getInt(1) +","+ result.getString(2) +","+  result.getString(3)  +","+ result.getDate(4)  +","+   result.getString(1) +","+  result.getString(1) +"#");
			}

			output.println(sb.toString());



		}catch (Exception e){
			output.println("notgood");
			e.printStackTrace();
		}

	}





	public void adddevicepermission(Map<String, String> map){
		logger.debug("test add3");


		try {


			//int id_company=Integer.parseInt(map.get("requestadddevice").get("idcompany"));
			String namepermission=map.get("permission");
			//logger.debug(namepermission);
			String device=map.get("device");
			//logger.debug(device);

			String dev[]=device.split(",");
			int idpermission=Integer.parseInt(map.get("idpermission"));
			//logger.debug(" waw "+idpermission);
			int iddevice=Integer.parseInt(getNbr(dev[0]));


			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date today = dateFormat.parse(dateFormat.format(new Date()));

			int x=3;

			String requestInsert = "insert into permission_device (device_id,permission_id,equipement_validity_period,number_validity_use) values ('"+    iddevice     +"','"+  idpermission   +    " ','"+     today     +"','"+  x    +"'                                   ) ;";


			c.createStatement().executeUpdate(requestInsert);





			output.println("bravo");



		}catch (Exception e){
			output.println("notgood");
			e.printStackTrace();
		}

	}





	public void getPermissions(Map<String, String> map){
		logger.debug("bravo2");
		try {

			//int company = Integer.parseInt(map.get("getpermissions").get("company_id"));
			ResultSet result = c.createStatement().executeQuery("select permission_badge.permission_id,permission_badge.name_permission from permission_badge "+
					"where company_id='" +
					Integer.parseInt(map.get("company_id"))+"';");
			StringBuilder sb = new StringBuilder();
			while (result.next()) {

				sb.append("ID Permission:" + result.getInt(1) + ",Nom Permission:" + result.getString(2) +"#");
			}

			output.println(sb.toString());



		}catch (Exception e){
			output.println("notgood");
			e.printStackTrace();
		}

	}


	public void getallemployees(Map<String, String> map){
		logger.debug("requestallbadges test");
		try {

			//int company = Integer.parseInt(map.get("requestallbadges").get("company_id"));
			ResultSet result = c.createStatement().executeQuery("select  badge.badge_id,employee.employee_last_name,employee.employee_last_name from employee inner join badge on employee.employee_id=badge.employee_id where company_id='" + Integer.parseInt(map.get("company_id"))+"';");
			StringBuilder sb = new StringBuilder();
			while (result.next()) {

				sb.append("ID Badge:" + result.getInt(1) + ",Nom:" + result.getString(2) +",Prenom:" + result.getString(2) +"#");
			}

			output.println(sb.toString());



		}catch (Exception e){
			output.println("notgood");
			e.printStackTrace();
		}

	}





	public void saveallemployees( Map<String, String> map) {
		logger.debug("test save all employees2");
		try {



			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

			int idcompany =Integer.parseInt(map.get("company_id"));
			logger.debug("waaw"+idcompany);
			String permission=map.get("permission");

			String per[]=permission.split(",");
			int idpermission=Integer.parseInt(getNbr(per[0]));
			//String employees=map.get("requestManyNewBadge").get("permission");
			logger.debug("waaw"+idpermission);
			String empls =map.get("employees");
			String [] employees = empls.split("&");

			Date datepermission = dateFormat.parse(map.get("date"));

			logger.debug("waaw"+empls);
			logger.debug("waaw2"+employees[0]);

			logger.debug("llee"+employees.length);



			for (int i = 0; i < employees.length; i++) {
				logger.debug(employees[i]);
				String emp[]=employees[i].split(",");
				//int idbadge=Integer.parseInt(getNbr(emp[0]));
				String idbadge=emp[0].substring(9,15);
				logger.debug("sdss"+idbadge);
				String insertrequest = "update permission_access set permission_id='" + idpermission + " ', permission_validity_period='" + datepermission +" 'where badge_id like'"+ idbadge +  "'                    ;";

				try {
					c.createStatement().executeUpdate(insertrequest);
				}catch (Exception e){
					output.println("notgood save");
				}

			}







		} catch (Exception e) {
			e.printStackTrace();
		}
	}






	public void testpermissions( Map<String, String> map) {
		logger.debug("tesssssttttt");
		try {


			String name1 =map.get("employee_last_name");
			String name2=map.get("employee_first_name");
			String device=map.get("device_id");

			String per[]=device.split(",");
			int device_id=Integer.parseInt(getNbr(per[0]));

			System.out.println(device_id);



			ResultSet result = c.createStatement().executeQuery("select employee.employee_id from employee inner join badge on badge.employee_id=employee.employee_id inner join permission_access on" +
					"permission_access.badge_id=badge.badge_id inner join permission_badge on permission_badge.permission_id=permission_access.permission_id inner join" +
					"permission_device on permission_device.permission_id=permission_badge.permission_id where (employee_last_name='"+ name1  +"' and employee_first_name='"+name2+  "' and device_id='"+device_id+ "'                 );");

			System.out.println("bravo");

			if(result.next()) {
				//String data = result.getString(1);
				output.println("Good");
			} else output.println("Notgood");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}











}