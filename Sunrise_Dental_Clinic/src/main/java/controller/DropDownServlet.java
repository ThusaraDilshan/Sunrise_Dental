package controller;

import dao.DropdownDAO;
import model.Dentist;
import model.TreatmentType;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/dropdown/*")
public class DropDownServlet extends HttpServlet {

    private final DropdownDAO dropdownDAO = new DropdownDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        PrintWriter out = response.getWriter();

        if (pathInfo == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\":\"Unknown dropdown resource\"}");
            return;
        }

        switch (pathInfo) {
            case "/dentists":
                out.print(buildDentistsJson());
                break;
            case "/treatments":
                out.print(buildTreatmentsJson());
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Unknown dropdown resource\"}");
        }
    }

    private String buildDentistsJson() {
        List<Dentist> dentists = dropdownDAO.getAllDentists();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < dentists.size(); i++) {
            Dentist d = dentists.get(i);
            if (i > 0) sb.append(",");
            sb.append("{")
              .append("\"dentistId\":").append(d.getDentistId()).append(",")
              .append("\"dentistName\":\"").append(escape(d.getDentistName())).append("\",")
              .append("\"specialization\":\"").append(escape(d.getSpecialization())).append("\",")
              .append("\"contactNo\":\"").append(escape(d.getContactNo())).append("\",")
              .append("\"consultationFee\":").append(d.getConsultationFee())
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String buildTreatmentsJson() {
        List<TreatmentType> treatments = dropdownDAO.getAllTreatments();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < treatments.size(); i++) {
            TreatmentType t = treatments.get(i);
            if (i > 0) sb.append(",");
            sb.append("{")
              .append("\"treatmentId\":").append(t.getTreatmentId()).append(",")
              .append("\"treatmentName\":\"").append(escape(t.getTreatmentName())).append("\",")
              .append("\"treatmentCost\":").append(t.getTreatmentCost())
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}