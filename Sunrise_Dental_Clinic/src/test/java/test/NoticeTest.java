package test;

import dao.NoticeDAO;
import model.Notice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NoticeDAOTest {

    private NoticeDAO noticeDAO;

    @BeforeEach
    void setUp() {
        noticeDAO = new NoticeDAO();
    }

    @Test
    @DisplayName("Test getNoticesByDentistId - Success with DB Dump Data (dentist_id 2)")
    void testGetNoticesByDentistIdSuccess() {
        List<Notice> notices = noticeDAO.getNoticesByDentistId(2);

        assertNotNull(notices, "Notice list should not be null");
        assertEquals(4, notices.size(), "Dentist 2 should have exactly 4 seeded notices");

        // ORDER BY created_at DESC -> most recent notice first
        Notice mostRecent = notices.get(0);
        assertEquals(4, mostRecent.getNoticeId());
        assertEquals("Hello Doc!!!", mostRecent.getDescription());
        assertEquals("admin", mostRecent.getSentBy());
        assertTrue(mostRecent.isRead(), "Seeded notices are all marked as read");

        Notice oldest = notices.get(notices.size() - 1);
        assertEquals(1, oldest.getNoticeId());
        assertEquals("hello", oldest.getDescription());

        // Every notice returned must actually belong to the requested dentist
        for (Notice n : notices) {
            assertEquals(2, n.getDentistId());
        }
    }

    @Test
    @DisplayName("Test getNoticesByDentistId - Dentist with no notices returns empty list")
    void testGetNoticesByDentistIdNoNotices() {
        // No seeded notice references this dentist id
        List<Notice> notices = noticeDAO.getNoticesByDentistId(9999);

        assertNotNull(notices, "Should return an empty list, not null");
        assertTrue(notices.isEmpty(), "Dentist with no notices should return an empty list");
    }

    @Test
    @DisplayName("Test markAsRead - Success on existing notice (notice_id 1)")
    void testMarkAsReadSuccess() {
        boolean marked = noticeDAO.markAsRead(1);

        assertTrue(marked, "Marking an existing notice as read should return true");
    }

    @Test
    @DisplayName("Test markAsRead - Non-existent notice id")
    void testMarkAsReadNotFound() {
        boolean marked = noticeDAO.markAsRead(999999);

        assertFalse(marked, "Marking a non-existent notice as read should return false");
    }

    @Test
    @DisplayName("Test addNotice - Success, inserts a new notice for dentist_id 1")
    void testAddNoticeSuccess() {
        // dentist_id 1 (Dr. Shehan Jayasinghe) has no seeded notices, so this
        // also exercises the insert path independently of testGetNoticesByDentistIdSuccess.
        Notice notice = new Notice(1, "JUnit test notice", "admin");

        boolean added = noticeDAO.addNotice(notice);

        assertTrue(added, "addNotice should return true on successful insert");
        assertTrue(notice.getNoticeId() > 0, "Generated notice id should be populated after insert");

        // Confirm it is retrievable straight back out of the database
        List<Notice> notices = noticeDAO.getNoticesByDentistId(1);
        assertTrue(
            notices.stream().anyMatch(n -> n.getNoticeId() == notice.getNoticeId()
                    && "JUnit test notice".equals(n.getDescription())),
            "Newly inserted notice should be retrievable via getNoticesByDentistId"
        );
    }
}