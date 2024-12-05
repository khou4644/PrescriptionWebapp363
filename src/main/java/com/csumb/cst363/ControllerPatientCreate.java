package com.csumb.cst363;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/*
 * Controller class for patient interactions.
 *   register as a new patient.
 */
@SuppressWarnings("unused")
@Controller
public class ControllerPatientCreate {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/*
	 * Request blank patient registration form.
	 * Do not modify this method.
	 */
	@GetMapping("/patient/new")
	public String getNewPatientForm(Model model) {
		model.addAttribute("patient", new Patient());
		return "patient_register";
	}
	
	/*
	 * Process new patient registration
	 */
	@PostMapping("/patient/new")
	public String createPatient(Patient p, Model model) {
		
		System.out.println("createPatient "+p);  // debug

		// TODO

		try (Connection con = getConnection()) {
			// get a connection to the database

			// validate the doctor's last name and obtain the doctor id
			PreparedStatement ps = con.prepareStatement(
					"SELECT id FROM doctor WHERE last_name = ?");
			ps.setString(1, p.getPrimaryName());

			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
				// if doctor not found, return error
				model.addAttribute("message", "Error: Doctor with last name " + p.getPrimaryName() + " not found.");
				model.addAttribute("patient", p);
				return "patient_register";
			}
			int doctorId = rs.getInt("id");

			// insert the patient profile into the patient table
			ps = con.prepareStatement(
					"INSERT INTO patient (ssn, first_name, last_name, birthdate, street, city, state, zip, doctor_id) " +
							"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);

			ps.setString(1, p.getSsn());
			ps.setString(2, p.getFirst_name());
			ps.setString(3, p.getLast_name());
			ps.setString(4, p.getBirthdate());
			ps.setString(5, p.getStreet());
			ps.setString(6, p.getCity());
			ps.setString(7, p.getState());
			ps.setString(8, p.getZipcode());
			ps.setInt(9, doctorId);

			ps.executeUpdate();

			// obtain the generated id for the patient and update patient object
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				p.setId(rs.getInt(1));
			}

			// display message and patient information
			model.addAttribute("message", "Registration successful.");
			model.addAttribute("patient", p);
			return "patient_show";

		} catch (SQLException e) {
			// if there is error
			model.addAttribute("message", "SQL Error: " + e.getMessage());
			model.addAttribute("patient", p);
			return "patient_register";
		}

	}
	
	/*
	 * Request blank form to search for patient by and and id
	 * Do not modify this method.
	 */
	@GetMapping("/patient/edit")
	public String getSearchForm(Model model) {
		model.addAttribute("patient", new Patient());
		return "patient_get";
	}
	
	/*
	 * Perform search for patient by patient id and name.
	 */
	@PostMapping("/patient/show")
	public String showPatient(Patient p, Model model) {

		System.out.println("showPatient " + p); // debug

		// TODO

		try (Connection con = getConnection()) {
			// get a connection to the database

			// using patient id and patient last name from patient object
			// retrieve patient profile and doctor's last name
			PreparedStatement ps = con.prepareStatement(
					"SELECT p.*, d.last_name as doctor_name " +
							"FROM patient p " +
							"JOIN doctor d ON p.doctor_id = d.id " +
							"WHERE p.id = ? AND p.last_name = ?");

			ps.setInt(1, p.getId());
			ps.setString(2, p.getLast_name());

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				// update patient object with patient profile data
				p.setFirst_name(rs.getString("first_name"));
				p.setBirthdate(rs.getString("birthdate"));
				p.setStreet(rs.getString("street"));
				p.setCity(rs.getString("city"));
				p.setState(rs.getString("state"));
				p.setZipcode(rs.getString("zip"));
				p.setPrimaryName(rs.getString("doctor_name"));

				model.addAttribute("patient", p);
				return "patient_show";
			} else {
				// if there is error
				model.addAttribute("message", "Patient not found.");
				model.addAttribute("patient", p);
				return "patient_get";
			}

		} catch (SQLException e) {
			// if there is error
			model.addAttribute("message", "SQL Error: " + e.getMessage());
			model.addAttribute("patient", p);
			return "patient_get";
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
