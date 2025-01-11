package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.dto.RegisterTopicDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.repository.MultiFileRepository;
import com.c0324.casestudym5.repository.StudentRepository;
import com.c0324.casestudym5.repository.TeamRepository;
import com.c0324.casestudym5.repository.TopicRepository;
import com.c0324.casestudym5.service.FirebaseService;
import com.c0324.casestudym5.service.MailService;
import com.c0324.casestudym5.service.NotificationService;
import com.c0324.casestudym5.service.TopicService;
import com.c0324.casestudym5.util.AppConstants;
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

import java.util.List;

@Service
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final TeamRepository teamRepository;
    private final StudentRepository studentRepository;
    private final FirebaseService firebaseService;
    private final MultiFileRepository multiFileRepository;
    private final NotificationService notificationService;
    private final TeacherRepository teacherRepository;
    private final StudentService studentService;
    private final MailService mailService;

    @Autowired

    public TopicServiceImpl(TopicRepository topicRepository, TeamRepository teamRepository, StudentRepository studentRepository, FirebaseService firebaseService, MultiFileRepository multiFileRepository, NotificationService notificationService, TeacherRepository teacherRepository, com.c0324.casestudym5.service.StudentService studentService, MailService mailService) {
        this.topicRepository = topicRepository;
        this.teamRepository = teamRepository;
        this.studentRepository = studentRepository;
        this.firebaseService = firebaseService;
        this.multiFileRepository = multiFileRepository;
        this.notificationService = notificationService;
        this.teacherRepository = teacherRepository;
        this.studentService = studentService;
        this.mailService = mailService;
    }

    @Override
    @Transactional
    public boolean registerTopic(RegisterTopicDTO registerTopicDTO, String studentEmail) {
        Student student = studentRepository.findByUserEmail(studentEmail);
        Team team = teamRepository.findTeamByStudentsId(student.getId());
        if(team != null && team.getStudents().contains(student) && team.getTopic() == null && student.isLeader()){
            Topic topic = new Topic();
            topic.setName(registerTopicDTO.getName());
            topic.setContent(registerTopicDTO.getContent());
            topic.setStatus(0);
            topic.setApproved(AppConstants.PENDING);
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
                notification.setContent("đã đăng ký đề tài và chờ sự bạn phê duyệt");
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
    public List<Topic> getPendingTopics() {
        return topicRepository.findByApprovedFalse(Pageable.unpaged()).getContent();
    }

    @Override
    @Transactional
    public void approveTopic(Long id) {
        Topic topic = getTopicById(id);
        topic.setStatus(1);
        topic.setStatus(AppConstants.APPROVED);
        topic.setApprovedBy(getCurrentTeacher());

        // Create and set start date, end date for each phase; strategy: 1 week for each phase; get start date from the next day the topic is approved
        Set<Phase> phases = new HashSet<>();
        LocalDate startDate = LocalDate.now().plusDays(1);
        for (int i = 1; i <= 4; i++) {
            Phase phase = new Phase();
            phase.setTopic(topic);
            phase.setPhaseNumber(i);
            phase.setStatus(0);
            phase.setStartDate(startDate);
            phase.setEndDate(startDate.plusWeeks(1));
            startDate = phase.getEndDate().plusDays(1);
            phaseRepository.save(phase);
            phases.add(phase);
        }
        topic.setPhases(phases);

        topicRepository.save(topic);

        Long teamId = topic.getTeam().getId();
        List<Student> students = studentRepository.findStudentsByTeamId(teamId);
        if (students != null) {
            String action = " đã được Giáo viên phê duyệt.";
            String subject = "Thông báo kiểm duyệt đề tài của giáo viên ";
            for (Student student : students) {
                mailService.sendMailApprovedToTeam(student.getUser().getEmail(), subject, student.getTeam().getName(), topic.getApprovedBy().getUser().getName(), student.getTeam().getTopic().getName(), action);
                // Send notification to the team
                Notification notification = new Notification();
                notification.setSender(topic.getApprovedBy().getUser());
                notification.setReceiver(student.getUser());
                notification.setContent("đã phê duyệt đề tài " + topic.getName() + " của nhóm bạn.");
                notificationService.sendNotification(notification);
            }
        }
    }

    @Override
    @Transactional
    public void rejectTopic(Long id) {
        Topic topic = getTopicById(id);
        topic.setApproved(AppConstants.REJECTED);
        topic.setStatus(AppConstants.UNAPPROVED);
        topicRepository.save(topic);

        Long teamId = topic.getTeam().getId();
        List<Student> students = studentRepository.findStudentsByTeamId(teamId);
        if (students != null) {
            String action = " đã được Giáo viên xem xét và không được thông qua.";
            String subject = "Thông báo kiểm duyệt đề tài của giáo viên ";
            for (Student student : students) {
                mailService.sendMailApprovedToTeam(student.getUser().getEmail(), subject, student.getTeam().getName(), topic.getApprovedBy().getUser().getName(), student.getTeam().getTopic().getName(), action);
                //Send notification to the team
                Notification notification = new Notification();
                notification.setSender(topic.getApprovedBy().getUser());
                notification.setReceiver(student.getUser());
                notification.setContent("đã từ chối đề tài " + topic.getName() + " của nhóm bạn.");
                notificationService.sendNotification(notification);
            }
        }
    }

    @Override
    public Page<Topic> getPendingTopicsPage(Pageable pageable) {
        return topicRepository.findAll(pageable);
    }

    @Override
    public List<Topic> getTopicCurrentStudent(Long id) {
        return topicRepository.findByTeam_Id(id);
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