package controller;

import dao.DentistDAO;
import dao.StaffDAO;
import model.Dentist;
import model.Staff;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "LoginServlet", urlPatterns = {"/LoginServlet"})
public class LoginServlet extends HttpServlet {

    private final StaffDAO staffDAO = new StaffDAO();
    private final DentistDAO dentistDAO = new DentistDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            String errorMsg = URLEncoder.encode("Username and Password are required.", StandardCharsets.UTF_8);
            response.sendRedirect("index.html?error=" + errorMsg);
            return;
        }
        
        HttpSession session = request.getSession(true);

        Staff staff = staffDAO.validateLogin(username, password);
        if (staff != null) {
            String staffRole = staff.getRole() != null ? staff.getRole().toUpperCase() : "STAFF";
            session.setAttribute("loggedInUser", staff);
            session.setAttribute("role", staffRole);
            String encodedUsername = URLEncoder.encode(staff.getUsername(), StandardCharsets.UTF_8);
            String encodedRole = URLEncoder.encode(staffRole, StandardCharsets.UTF_8);
            response.sendRedirect("staff_dash.html?user=" + encodedUsername + "&role=" + encodedRole);
            return;
        }

        // 2. Try DENTIST login
        Dentist dentist = dentistDAO.validateLogin(username, password);
        if (dentist != null) {
            session.setAttribute("loggedInUser", dentist);
            session.setAttribute("role", "DENTIST");
            String encodedName = URLEncoder.encode(dentist.getDentistName(), StandardCharsets.UTF_8);
            String encodedDentistUsername = URLEncoder.encode(dentist.getUsername(), StandardCharsets.UTF_8);
            response.sendRedirect("dentist_dash.html?id=" + dentist.getDentistId()
                    + "&name=" + encodedName
                    + "&username=" + encodedDentistUsername);
            return;
        }

        // 3. Invalid credentials
        String errorMsg = URLEncoder.encode("Invalid username or password.", StandardCharsets.UTF_8);
        response.sendRedirect("index.html?error=" + errorMsg);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("index.html");
    }
}