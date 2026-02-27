package com.tibame.app_generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibame.app_generator.controller.InvitationController.CreateInvitationRequest;
import com.tibame.app_generator.controller.ProjectMemberController.AddMemberRequest;
import com.tibame.app_generator.enums.ProjectRole;
import com.tibame.app_generator.model.Project;
import com.tibame.app_generator.model.ProjectInvitation;
import com.tibame.app_generator.model.ProjectInvitation.InvitationStatus;
import com.tibame.app_generator.model.ProjectMember;
import com.tibame.app_generator.model.User;
import com.tibame.app_generator.repository.ProjectInvitationRepository;
import com.tibame.app_generator.repository.ProjectMemberRepository;
import com.tibame.app_generator.repository.ProjectRepository;
import com.tibame.app_generator.repository.UserRepository;
import com.tibame.app_generator.service.GitService;
import com.tibame.app_generator.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InvitationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private ProjectInvitationRepository invitationRepository;

    @Autowired
    private ProjectService projectService;

    @MockBean
    private GitService gitService; // Mock git service to avoid filesystem ops

    private User adminUser;
    private User inviteeUser;
    private Project project;

    @BeforeEach
    void setUp() throws Exception {
        // Create Admin User
        adminUser = User.builder()
                .username("admin")
                .email("admin@example.com")
                .passwordHash("hash")
                .build();
        adminUser = userRepository.save(adminUser);

        // Create Invitee User
        inviteeUser = User.builder()
                .username("invitee")
                .email("invitee@example.com")
                .passwordHash("hash")
                .build();
        inviteeUser = userRepository.save(inviteeUser);

        // Mock Git Service
        when(gitService.initBareRepository(any(), any())).thenReturn("/tmp/repo");

        // Create Project (Admin is creator)
        project = projectService.createProject(adminUser.getId(), "Test Project", "Desc");
    }

    @Test
    void testCreateInvitation() throws Exception {
        CreateInvitationRequest request = new CreateInvitationRequest();
        request.setEmail(inviteeUser.getEmail());
        request.setRole(ProjectRole.MEMBER);

        mockMvc.perform(post("/api/projects/" + project.getId() + "/invitations")
                        .with(user(adminUser)) // Mock Spring Security User
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(inviteeUser.getEmail()))
                .andExpect(jsonPath("$.status").value("PENDING"));

        assertEquals(1, invitationRepository.count());
    }

    @Test
    void testAcceptInvitation() throws Exception {
        // 1. Create Invitation
        ProjectInvitation invitation = ProjectInvitation.builder()
                .project(project)
                .inviter(adminUser)
                .email(inviteeUser.getEmail())
                .role(ProjectRole.MEMBER)
                .status(InvitationStatus.PENDING)
                .build();
        invitation = invitationRepository.save(invitation);

        // 2. Accept Invitation as Invitee
        mockMvc.perform(post("/api/invitations/" + invitation.getId() + "/accept")
                        .with(user(inviteeUser)))
                .andExpect(status().isOk());

        // 3. Verify
        ProjectInvitation updatedInv = invitationRepository.findById(invitation.getId()).orElseThrow();
        assertEquals(InvitationStatus.ACCEPTED, updatedInv.getStatus());

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(project.getId(), inviteeUser.getId()).orElseThrow();
        assertEquals(ProjectRole.MEMBER, member.getRole());
    }

    @Test
    void testLeaveProject() throws Exception {
        // 1. Add Member directly
        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(inviteeUser)
                .role(ProjectRole.MEMBER)
                .build();
        projectMemberRepository.save(member);

        // 2. Leave
        mockMvc.perform(delete("/api/projects/" + project.getId() + "/members/me")
                        .with(user(inviteeUser)))
                .andExpect(status().isOk());

        // 3. Verify
        assertTrue(projectMemberRepository.findByProjectIdAndUserId(project.getId(), inviteeUser.getId()).isEmpty());
    }

    @Test
    void testLastAdminCannotLeave() throws Exception {
        // Admin tries to leave (they are the only admin)
        mockMvc.perform(delete("/api/projects/" + project.getId() + "/members/me")
                        .with(user(adminUser)))
                .andExpect(status().isBadRequest());
    }
}
