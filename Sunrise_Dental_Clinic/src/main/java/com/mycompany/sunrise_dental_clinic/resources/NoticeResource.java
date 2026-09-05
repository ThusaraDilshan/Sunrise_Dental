package com.mycompany.sunrise_dental_clinic.resources;

import dao.NoticeDAO;
import model.Notice;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/notice")
public class NoticeResource {

    private final NoticeDAO noticeDAO = new NoticeDAO();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addNotice(Notice notice) {
        if (notice.getDentistId() <= 0
                || notice.getDescription() == null || notice.getDescription().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"dentistId and description are required\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        boolean success = noticeDAO.addNotice(notice);
        if (success) {
            return Response.status(Response.Status.CREATED)
                    .entity(notice)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Failed to send notice\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    @GET
    @Path("/dentist/{dentistId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNoticesForDentist(@PathParam("dentistId") int dentistId) {
        List<Notice> notices = noticeDAO.getNoticesByDentistId(dentistId);
        return Response.ok(notices).build();
    }

    @PUT
    @Path("/{noticeId}/read")
    @Produces(MediaType.APPLICATION_JSON)
    public Response markAsRead(@PathParam("noticeId") int noticeId) {
        boolean success = noticeDAO.markAsRead(noticeId);
        if (success) {
            return Response.ok("{\"message\":\"Notice marked as read\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"Notice not found\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}