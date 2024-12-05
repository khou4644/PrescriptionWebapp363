package com.csumb.cst363;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
/*
 * Controller class for patient interactions.
 *   update patient profile.
 */
@SuppressWarnings("unused")
@Controller
public class ControllerPatientUpdate {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	

	/*
	 *  Display patient profile for patient id.
	 */
	@GetMapping("/patient/edit/{id}")
	public String getUpdateForm(@PathVariable int id, Model model) {

		System.out.println("getUpdateForm "+ id );  // debug
		
		// TODO

		try (Connection con = getConnection()) {
			// get a connection to the database
			// using patient id and patient last name from patient object
			// retrieve patient profile and doctor's last name
			PreparedStatement ps = con.prepareStatement(
					"SELECT p.*, d.last_name as doctor_name " +
							"FROM patient p " +
							"JOIN doctor d ON p.doctor_id = d.id " +
							"WHERE p.id = ?");

			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				// update patient object with patient profile data
				Patient p = new Patient();
				p.setId(id);
				p.setFirst_name(rs.getString("first_name"));
				p.setLast_name(rs.getString("last_name"));
				p.setBirthdate(rs.getString("birthdate"));
				p.setStreet(rs.getString("street"));
				p.setCity(rs.getString("city"));
				p.setState(rs.getString("state"));
				p.setZipcode(rs.getString("zip"));
				p.setPrimaryName(rs.getString("doctor_name"));

				model.addAttribute("patient", p);
				return "patient_edit";
			} else {
				model.addAttribute("message", "Patient not found.");
				return "index";
			}

		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error: " + e.getMessage());
			return "index";
		}
	}
	
	
	/*
	 * Process changes to patient profile.  
	 */
	@PostMapping("/patient/edit")
	public String updatePatient(Patient p, Model model) {
		
		System.out.println("updatePatient " + p);  // for debug 
		
		// TODO

		try (Connection con = getConnection()) {
			// get a connection to the database

			// validate the doctor's last name and obtain the doctor id
			PreparedStatement ps = con.prepareStatement(
					"SELECT id FROM doctor WHERE last_name = ?");
			ps.setString(1, p.getPrimaryName());

			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
				model.addAttribute("message", "Error: Doctor with last name " + p.getPrimaryName() + " not found.");
				model.addAttribute("patient", p);
				return "patient_edit";
			}
			int doctorId = rs.getInt("id");

			// update the patient's profile for street, city, state, zip and doctor id
			ps = con.prepareStatement(
					"UPDATE patient SET street=?, city=?, state=?, zip=?, doctor_id=? WHERE id=?");

			ps.setString(1, p.getStreet());
			ps.setString(2, p.getCity());
			ps.setString(3, p.getState());
			ps.setString(4, p.getZipcode());
			ps.setInt(5, doctorId);
			ps.setInt(6, p.getId());

			int rc = ps.executeUpdate();
			if (rc == 1) {
				model.addAttribute("message", "Update successful.");
				model.addAttribute("patient", p);
				return "patient_show";
			} else {
				model.addAttribute("message", "Error: Update failed.");
				model.addAttribute("patient", p);
				return "patient_edit";
			}

		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error: " + e.getMessage());
			model.addAttribute("patient", p);
			return "patient_edit";
		}

	}

	/*
	 * return JDBC Connection using jdbcTemplate in Spring Server
	 */

	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

}
