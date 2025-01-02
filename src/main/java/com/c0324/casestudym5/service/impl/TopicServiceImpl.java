package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.dto.RegisterTopicDTO;
import com.c0324.casestudym5.model.MultiFile;
import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.model.Team;
import com.c0324.casestudym5.model.Topic;
import com.c0324.casestudym5.repository.MultiFileRepository;
import com.c0324.casestudym5.repository.StudentRepository;
import com.c0324.casestudym5.repository.TeamRepository;
import com.c0324.casestudym5.repository.TopicRepository;
import com.c0324.casestudym5.service.FirebaseService;
import com.c0324.casestudym5.service.TopicService;
import com.c0324.casestudym5.util.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final TeamRepository teamRepository;
    private final StudentRepository studentRepository;
    private final FirebaseService firebaseService;
    private final MultiFileRepository multiFileRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public TopicServiceImpl(TopicRepository topicRepository, TeamRepository teamRepository, StudentRepository studentRepository, FirebaseService firebaseService, MultiFileRepository multiFileRepository, SimpMessagingTemplate simpMessagingTemplate) {
        this.topicRepository = topicRepository;
        this.teamRepository = teamRepository;
        this.studentRepository = studentRepository;
        this.firebaseService = firebaseService;
        this.multiFileRepository = multiFileRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
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
            topic.setTeam(team);

            // upload image and description file to firebase
            String url_image = firebaseService.uploadFileToFireBase(registerTopicDTO.getImage(), AppConstants.URL_TOPIC);
            String url_description = firebaseService.uploadFileToFireBase(registerTopicDTO.getDescription(), AppConstants.URL_TOPIC);

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

            simpMessagingTemplate.convertAndSend("/topic/teams", team);
        }
        else {
            return false;
        }
        return true;
    }

    @Override
    public Page<Topic> getAllTopics(Pageable pageable) {
        return topicRepository.findAll(pageable);
    }

    @Override
    public Page<Topic> findByStatus(int status, Pageable pageable) {
        return topicRepository.findByStatus(status, pageable);
    }
}
