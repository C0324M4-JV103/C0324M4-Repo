package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.dto.ProgressReportDTO;
import com.c0324.casestudym5.dto.RegisterTopicDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.repository.MultiFileRepository;
import com.c0324.casestudym5.repository.StudentRepository;
import com.c0324.casestudym5.repository.TeamRepository;
import com.c0324.casestudym5.repository.TopicRepository;
import com.c0324.casestudym5.service.*;
import com.c0324.casestudym5.repository.*;
import com.c0324.casestudym5.util.AppConstants;
import com.c0324.casestudym5.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.c0324.casestudym5.repository.TeacherRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final TeamRepository teamRepository;
    private final StudentRepository studentRepository;
    private final FirebaseService firebaseService;
    private final MultiFileRepository multiFileRepository;
    private final NotificationService notificationService;
    private final TeacherRepository teacherRepository;
    private final PhaseRepository phaseRepository;
    private final MailService mailService;

    @Autowired

    public TopicServiceImpl(TopicRepository topicRepository, TeamRepository teamRepository, StudentRepository studentRepository,
                            FirebaseService firebaseService, MultiFileRepository multiFileRepository, NotificationService notificationService,
                            TeacherRepository teacherRepository, PhaseRepository phaseRepository, MailService mailService) {
        this.topicRepository = topicRepository;
        this.teamRepository = teamRepository;
        this.studentRepository = studentRepository;
        this.firebaseService = firebaseService;
        this.multiFileRepository = multiFileRepository;
        this.notificationService = notificationService;
        this.teacherRepository = teacherRepository;
        this.phaseRepository = phaseRepository;
        this.mailService = mailService;
    }




    @Override
    @Transactional
    public boolean registerTopic(RegisterTopicDTO registerTopicDTO, String studentEmail) {
        Student student = studentRepository.findByUserEmail(studentEmail);
        Team team = teamRepository.findTeamByStudentsId(student.getId());
        if (team != null && team.getStudents().contains(student) && team.getTopic() == null && student.isLeader()) {
            Topic topic = new Topic();
            topic.setName(registerTopicDTO.getName());
            topic.setContent(registerTopicDTO.getContent());
            topic.setStatus(0);
            topic.setApproved(AppConstants.TOPIC_PENDING);
            topic.setTeam(team);

            String url_image, url_description;
            // upload image and description file to firebase
            try {
                if (registerTopicDTO.getImage().isEmpty() || registerTopicDTO.getDescription().isEmpty()) {
                    return false;
                }
                url_image = firebaseService.uploadFileToFireBase(registerTopicDTO.getImage(), AppConstants.URL_TOPIC);
                url_description = firebaseService.uploadFileToFireBase(registerTopicDTO.getDescription(), AppConstants.URL_TOPIC);
            } catch (Exception e) {
                return false;
            }

            // save image and description to database
            MultiFile image = new MultiFile();
            image.setUrl(url_image);
            MultiFile description = new MultiFile();
            description.setUrl(url_description);
            multiFileRepository.save(image);
            multiFileRepository.save(description);

            // set image and description to topic
            topic.setImage(image);
            topic.setDescription(description);

            // save topic to database
            topicRepository.save(topic);

            // Send notification to the teacher
            User teacher = student.getTeam().getTeacher().getUser();
            if (teacher != null) {
                // Send email to the teacher
                String subject = team.getName() + " - Thông báo đăng ký đề tài";
                mailService.sendRegisterTopicEmail(teacher.getEmail(), subject, student.getUser().getName(), teacher.getName(), topic.getName(), team.getName());

                // Send notification to the teacher
                Notification notification = new Notification();
                notification.setSender(student.getUser());
                notification.setReceiver(teacher);
                notification.setContent("đã đăng ký đề tài và chờ bạn phê duyệt");
                notification.setUrl("/teacher/topics");
                notificationService.sendNotification(notification);
            }
        }
        else {
            return false;
        }
        return true;
    }

    @Override
    public Page<Topic> getAllTopics(Pageable pageable) {
        return topicRepository.findByApprovedTrueAndStatus(pageable);
    }

    @Override
    public Topic getTopicById(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dự án"));
    }

    @Override
    public List<Topic> getLatestTopics(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by("id").descending());
        return topicRepository.findByApprovedTrueAndStatus(pageRequest).getContent();
    }

    @Override
    @Transactional
    public void approveTopic(Long id) {
        Topic topic = getTopicById(id);
        topic.setStatus(1);
        topic.setApproved(AppConstants.TOPIC_APPROVED);
        topic.setApprovedBy(getCurrentTeacher());

        Long teamId = topic.getTeam().getId();
        List<Student> students = studentRepository.findStudentsByTeamId(teamId);
        if (students != null) {
            String action = " đã được Giáo viên phê duyệt.";
            String subject = "Thông báo kiểm duyệt đề tài của giáo viên ";
            for (Student student : students) {
                try {
                    mailService.sendMailApprovedToTeam(topic,student.getUser().getEmail(), subject, student.getTeam().getName(), topic.getApprovedBy().getUser().getName(), student.getTeam().getTopic().getName(),topic.getId(), action);
                    // Send notification to the team
                    Notification notification = new Notification();
                    notification.setSender(topic.getApprovedBy().getUser());
                    notification.setReceiver(student.getUser());
                    notification.setContent("đã phê duyệt đề tài " + topic.getName() + " của nhóm bạn.");
                    notification.setUrl("/topics/" + id);
                    notificationService.sendNotification(notification);
                } catch (Exception e) {
                    System.err.println("Error sending email: " + e.getMessage());
                }
            }
        }

        // Create and set start date, end date for each phase; strategy: 1 week for each phase; get start date from the next day the topic is approved
        Set<Phase> phases = new HashSet<>();
        LocalDate startDate = LocalDate.now().plusDays(1);
        for (int i = 1; i <= 4; i++) {
            Phase phase = new Phase();
            phase.setTopic(topic);
            phase.setPhaseNumber(i);
            if (i == 1)
                phase.setStatus(AppConstants.PHASE_OPENED);
            else
                phase.setStatus(AppConstants.PHASE_CLOSED);
            phase.setPhaseProgressPercent(0);
            phase.setStartDate(startDate);
            phase.setEndDate(startDate.plusWeeks(1));
            startDate = phase.getEndDate().plusDays(1);
            phaseRepository.save(phase);
            phases.add(phase);
        }
        topic.setPhases(phases);

        topicRepository.save(topic);
    }

    @Override
    @Transactional
    public void rejectTopic(Long id, String reason) {
        Topic topic = getTopicById(id);
        topicRepository.delete(topic);

        Long teamId = topic.getTeam().getId();
        Teacher teacher = topic.getTeam().getTeacher();
        List<Student> students = studentRepository.findStudentsByTeamId(teamId);
        if (students != null) {
            String action = " đã được Giáo viên xem xét và không được thông qua.";
            String subject = "Thông báo kiểm duyệt đề tài của giáo viên ";
            for (Student student : students) {
                mailService.sendMailRejectToTeam(
                        topic, student.getUser().getEmail(), subject, student.getTeam().getName(),
                        teacher.getUser().getName(), student.getTeam().getTopic().getName(), topic.getId(), action, reason
                );

                Notification notification = new Notification();
                notification.setSender(teacher.getUser());
                notification.setReceiver(student.getUser());
                notification.setContent("đã từ chối đề tài " + topic.getName() + " của nhóm bạn với lý do: " + reason);
                notification.setUrl(null);
                notificationService.sendNotification(notification);
            }
        }
    }


    @Override
    public Page<Topic> getPendingTopicsPage(Pageable pageable) {
        return topicRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public String submitProgressReport(Long topicId, ProgressReportDTO progressReportDTO, Student student) {
        List<Phase> phases = phaseRepository.findByTopicIdAndPhaseNumber(topicId, progressReportDTO.getPhaseNumber());
        if (phases.isEmpty()) {
            return "Không tìm thấy giai đoạn";
        }
        Phase phase = phases.get(0); // Assuming you want to work with the first result
        if (!Objects.equals(student.getTeam().getTopic().getId(), topicId) || phase.getStatus() == AppConstants.PHASE_CLOSED) {
            return "Không thể báo cáo giai đoạn này";
        }
        if (progressReportDTO.getPhaseProgressPercent() > 100 || progressReportDTO.getPhaseProgressPercent() < 50) {
            return "Tiến độ không hợp lệ (50% - 100%)";
        }

        // Clone the phase if it has been submitted before (status = 2)
        if (phase.getStatus() == AppConstants.PHASE_COMPLETED) {
            Phase newPhase = new Phase();
            newPhase.setTopic(phase.getTopic());
            newPhase.setPhaseNumber(phase.getPhaseNumber());
            newPhase.setStatus(AppConstants.PHASE_COMPLETED);
            newPhase.setStartDate(phase.getStartDate());
            newPhase.setEndDate(phase.getEndDate());
            newPhase.setPhaseProgressPercent(progressReportDTO.getPhaseProgressPercent());
            newPhase.setReportContent(progressReportDTO.getReportContent());
            newPhase.setReportDate(LocalDateTime.now());
            newPhase.setReporter(student.getUser());

            // Upload report file to firebase
            try {
                if (progressReportDTO.getReportFile() == null) {
                    return "Không tìm thấy file báo cáo";
                }
                String url_report = firebaseService.uploadFileToFireBase(progressReportDTO.getReportFile(), AppConstants.URL_REPORT);
                MultiFile report = new MultiFile();
                report.setUrl(url_report);
                multiFileRepository.save(report);
                newPhase.setReportFile(report);
            } catch (Exception e) {
                return "Lỗi khi tải file lên";
            }
            phaseRepository.save(newPhase);
        } else {
            phase.setStatus(AppConstants.PHASE_COMPLETED);
            phase.setReportContent(progressReportDTO.getReportContent());
            phase.setPhaseProgressPercent(progressReportDTO.getPhaseProgressPercent());
            phase.setReportDate(LocalDateTime.now());
            phase.setReporter(student.getUser());

            // Upload report file to firebase
            try {
                if (progressReportDTO.getReportFile() == null) {
                    return "Không tìm thấy file báo cáo";
                }
                String url_report = firebaseService.uploadFileToFireBase(progressReportDTO.getReportFile(), AppConstants.URL_REPORT);
                MultiFile report = new MultiFile();
                report.setUrl(url_report);
                multiFileRepository.save(report);
                phase.setReportFile(report);
            } catch (Exception e) {
                return "Lỗi khi tải file lên";
            }
            phaseRepository.save(phase);
        }

        // Send notification to the teacher
        Topic topic = phase.getTopic();
        User teacher = topic.getTeam().getTeacher().getUser();
        if (teacher != null) {
            // Send email to the teacher
            String subject = topic.getTeam().getName() + " - Thông báo báo cáo tiến độ giai đoạn " + progressReportDTO.getPhaseNumber();
            mailService.sendSubmittedProgressReportEmail(teacher.getEmail(), subject, student.getUser().getName(), teacher.getName(), topic, topic.getTeam().getName(), phase.getPhaseNumber().toString());

            // Send notification to the teacher
            Notification notification = new Notification();
            notification.setSender(getCurrentUser());
            notification.setReceiver(teacher);
            notification.setContent("đã báo cáo tiến độ giai đoạn " + progressReportDTO.getPhaseNumber() + " của đề tài " + topic.getName());
            notification.setUrl("/progress/" + topicId);
            notificationService.sendNotification(notification);
        }

        // Open next phase and check if all phases are completed
        Phase next_phase = phaseRepository.findByTopicIdAndPhaseNumber(topicId, progressReportDTO.getPhaseNumber() + 1).stream().findFirst().orElse(null);
        if (next_phase != null && next_phase.getStatus() == AppConstants.PHASE_CLOSED) {
            next_phase.setStatus(AppConstants.PHASE_OPENED);
            phaseRepository.save(next_phase);
        } else {
            boolean allPhasesCompleted = true;
            for (int i = 1; i <= 4; i++) {
                Phase p = phaseRepository.findByTopicIdAndPhaseNumber(topicId, i).stream().findFirst().orElse(null);
                if (p == null || p.getStatus() != AppConstants.PHASE_COMPLETED) {
                    allPhasesCompleted = false;
                    break;
                }
            }
            if (allPhasesCompleted) {
                topic.setStatus(AppConstants.TOPIC_CLOSED);
                topicRepository.save(topic);
            }
        }
        return "";
    }

    @Override
    @Transactional
    public void setNewDeadline(Long teamId, Date newDeadline, User setBy) {

        Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Không tìm thấy nhóm"));
        Topic topic = team.getTopic();
        if (topic == null) {
            throw new RuntimeException("Nhóm chưa đăng ký đề tài");
        }
        if (topic.getDeadline() != null && topic.getDeadline().after(newDeadline)) {
            throw new RuntimeException("Hạn nộp mới phải sau hạn nộp cũ");
        }

        List<Student> students = team.getStudents();

        // set new deadline for the topic
        topic.setDeadline(newDeadline);

        // set last phase end date to new deadline
        List<Phase> lastPhases = phaseRepository.findByTopicIdAndPhaseNumber(topic.getId(), 4);
        if (lastPhases.isEmpty()) {
            throw new RuntimeException("Không tìm thấy giai đoạn cuối");
        }
        Phase lastPhase = lastPhases.get(0); // Only one last phase
        lastPhase.setEndDate(LocalDate.from(DateTimeUtil.convertDateToLocalDate(newDeadline).atStartOfDay()));
        phaseRepository.save(lastPhase);

        for(Student student : students) {
            // Send notification to the team
            Notification notification = new Notification();
            notification.setSender(setBy);
            notification.setReceiver(student.getUser());
            notification.setContent("đã đặt lại hạn nộp cho đề tài " + topic.getName());
            notification.setUrl(null);
            notificationService.sendNotification(notification);

            // Send email to the team
            String subject = "Thông báo thay đổi hạn nộp đề tài";
            mailService.sendNewDeadlineEmail(student.getUser().getEmail(), subject, setBy.getName(), student.getUser().getName(), topic.getName(), topic.getId());
        }

        topicRepository.save(topic);
    }

    @Override
    public Page<Topic> getTopicByTeacher(Long id, PageRequest pageRequest) {
        return topicRepository.findTopicsByTeacher(id, pageRequest);
    }

    private User getCurrentUser() {
        // Logic để lấy thông tin user đang đăng nhập
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return studentRepository.findByUserEmail(auth.getName()).getUser();
    }

    @Override
    public Page<Topic> findByStatus(int status, Pageable pageable) {
        return topicRepository.findByStatus(status, pageable);
    }

    private Teacher getCurrentTeacher() {
        // Logic để lấy thông tin giáo viên đang đăng nhập
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return teacherRepository.findTeacherByUserEmail(auth.getName());
    }

}