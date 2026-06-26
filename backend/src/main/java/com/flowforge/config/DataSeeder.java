package com.flowforge.config;

import com.flowforge.domain.entity.AppUser;
import com.flowforge.domain.entity.Role;
import com.flowforge.domain.entity.WorkflowAudit;
import com.flowforge.domain.entity.WorkflowRequest;
import com.flowforge.domain.model.RoleName;
import com.flowforge.domain.model.WorkflowAction;
import com.flowforge.domain.model.WorkflowStatus;
import com.flowforge.repository.AppUserRepository;
import com.flowforge.repository.RoleRepository;
import com.flowforge.repository.WorkflowAuditRepository;
import com.flowforge.repository.WorkflowRequestRepository;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {
    public static final String SAMPLE_PASSWORD = "FlowForge@123";

    @Bean
    CommandLineRunner seedData(
            RoleRepository roleRepository,
            AppUserRepository userRepository,
            WorkflowRequestRepository workflowRepository,
            WorkflowAuditRepository auditRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Map<RoleName, Role> roles = Arrays.stream(RoleName.values())
                    .collect(Collectors.toMap(Function.identity(), roleName -> roleRepository.findByName(roleName)
                            .orElseGet(() -> roleRepository.save(new Role(roleName)))));

            AppUser employee = ensureUser(
                    userRepository,
                    passwordEncoder,
                    "employee@flowforge.com",
                    "Aarav Employee",
                    Set.of(roles.get(RoleName.EMPLOYEE))
            );
            AppUser manager = ensureUser(
                    userRepository,
                    passwordEncoder,
                    "manager@flowforge.com",
                    "Meera Manager",
                    Set.of(roles.get(RoleName.MANAGER))
            );
            AppUser hrAdmin = ensureUser(
                    userRepository,
                    passwordEncoder,
                    "hr@flowforge.com",
                    "Harini HR Admin",
                    Set.of(roles.get(RoleName.HR_ADMIN))
            );

            if (workflowRepository.count() == 0) {
                seedWorkflowSamples(workflowRepository, auditRepository, employee, manager, hrAdmin);
            }
        };
    }

    private AppUser ensureUser(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String email,
            String fullName,
            Set<Role> roles
    ) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> userRepository.save(new AppUser(
                        email,
                        fullName,
                        passwordEncoder.encode(SAMPLE_PASSWORD),
                        roles
                )));
    }

    private void seedWorkflowSamples(
            WorkflowRequestRepository workflowRepository,
            WorkflowAuditRepository auditRepository,
            AppUser employee,
            AppUser manager,
            AppUser hrAdmin
    ) {
        WorkflowRequest draft = workflowRepository.save(new WorkflowRequest(
                "Laptop and account provisioning",
                "Prepare laptop, SSO, VPN, and email accounts before joining date.",
                employee,
                manager
        ));
        audit(auditRepository, draft, employee, WorkflowAction.CREATE, null, WorkflowStatus.DRAFT, "Draft created by employee.");

        WorkflowRequest submitted = workflowRepository.save(new WorkflowRequest(
                "New hire onboarding - Priya Nair",
                "Onboard new engineering hire with required systems access and induction checklist.",
                employee,
                manager
        ));
        audit(auditRepository, submitted, employee, WorkflowAction.CREATE, null, WorkflowStatus.DRAFT, "Draft created.");
        submitted.transitionTo(WorkflowStatus.SUBMITTED);
        workflowRepository.save(submitted);
        audit(auditRepository, submitted, employee, WorkflowAction.SUBMIT, WorkflowStatus.DRAFT, WorkflowStatus.SUBMITTED, "Submitted for manager review.");

        WorkflowRequest managerApproved = workflowRepository.save(new WorkflowRequest(
                "Workspace access approval",
                "Request workspace badge, payroll profile, and HR policy acknowledgement.",
                employee,
                manager
        ));
        audit(auditRepository, managerApproved, employee, WorkflowAction.CREATE, null, WorkflowStatus.DRAFT, "Draft created.");
        managerApproved.transitionTo(WorkflowStatus.SUBMITTED);
        workflowRepository.save(managerApproved);
        audit(auditRepository, managerApproved, employee, WorkflowAction.SUBMIT, WorkflowStatus.DRAFT, WorkflowStatus.SUBMITTED, "Submitted for manager review.");
        managerApproved.setAssignedTo(hrAdmin);
        managerApproved.transitionTo(WorkflowStatus.MANAGER_APPROVED);
        workflowRepository.save(managerApproved);
        audit(auditRepository, managerApproved, manager, WorkflowAction.MANAGER_APPROVE, WorkflowStatus.SUBMITTED, WorkflowStatus.MANAGER_APPROVED, "Manager approved access package.");

        WorkflowRequest hrApproved = workflowRepository.save(new WorkflowRequest(
                "Benefits enrollment setup",
                "Complete benefits enrollment for employee and dependent documentation.",
                employee,
                manager
        ));
        audit(auditRepository, hrApproved, employee, WorkflowAction.CREATE, null, WorkflowStatus.DRAFT, "Draft created.");
        hrApproved.transitionTo(WorkflowStatus.SUBMITTED);
        workflowRepository.save(hrApproved);
        audit(auditRepository, hrApproved, employee, WorkflowAction.SUBMIT, WorkflowStatus.DRAFT, WorkflowStatus.SUBMITTED, "Submitted for approval.");
        hrApproved.setAssignedTo(hrAdmin);
        hrApproved.transitionTo(WorkflowStatus.MANAGER_APPROVED);
        workflowRepository.save(hrApproved);
        audit(auditRepository, hrApproved, manager, WorkflowAction.MANAGER_APPROVE, WorkflowStatus.SUBMITTED, WorkflowStatus.MANAGER_APPROVED, "Manager approved benefits request.");
        hrApproved.transitionTo(WorkflowStatus.HR_APPROVED);
        workflowRepository.save(hrApproved);
        audit(auditRepository, hrApproved, hrAdmin, WorkflowAction.HR_APPROVE, WorkflowStatus.MANAGER_APPROVED, WorkflowStatus.HR_APPROVED, "HR verified documents.");

        WorkflowRequest completed = workflowRepository.save(new WorkflowRequest(
                "Contractor onboarding pack",
                "Complete contractor profile, badge, NDA, and system access provisioning.",
                employee,
                manager
        ));
        audit(auditRepository, completed, employee, WorkflowAction.CREATE, null, WorkflowStatus.DRAFT, "Draft created.");
        completed.transitionTo(WorkflowStatus.SUBMITTED);
        workflowRepository.save(completed);
        audit(auditRepository, completed, employee, WorkflowAction.SUBMIT, WorkflowStatus.DRAFT, WorkflowStatus.SUBMITTED, "Submitted to manager.");
        completed.setAssignedTo(hrAdmin);
        completed.transitionTo(WorkflowStatus.MANAGER_APPROVED);
        workflowRepository.save(completed);
        audit(auditRepository, completed, manager, WorkflowAction.MANAGER_APPROVE, WorkflowStatus.SUBMITTED, WorkflowStatus.MANAGER_APPROVED, "Manager approved contractor onboarding.");
        completed.transitionTo(WorkflowStatus.HR_APPROVED);
        workflowRepository.save(completed);
        audit(auditRepository, completed, hrAdmin, WorkflowAction.HR_APPROVE, WorkflowStatus.MANAGER_APPROVED, WorkflowStatus.HR_APPROVED, "HR approved onboarding pack.");
        completed.transitionTo(WorkflowStatus.COMPLETED);
        workflowRepository.save(completed);
        audit(auditRepository, completed, hrAdmin, WorkflowAction.COMPLETE, WorkflowStatus.HR_APPROVED, WorkflowStatus.COMPLETED, "All onboarding tasks completed.");

        WorkflowRequest rejected = workflowRepository.save(new WorkflowRequest(
                "Relocation onboarding exception",
                "Request exception approval for relocation onboarding outside policy window.",
                employee,
                manager
        ));
        audit(auditRepository, rejected, employee, WorkflowAction.CREATE, null, WorkflowStatus.DRAFT, "Draft created.");
        rejected.transitionTo(WorkflowStatus.SUBMITTED);
        workflowRepository.save(rejected);
        audit(auditRepository, rejected, employee, WorkflowAction.SUBMIT, WorkflowStatus.DRAFT, WorkflowStatus.SUBMITTED, "Submitted for exception approval.");
        rejected.transitionTo(WorkflowStatus.REJECTED);
        workflowRepository.save(rejected);
        audit(auditRepository, rejected, manager, WorkflowAction.REJECT, WorkflowStatus.SUBMITTED, WorkflowStatus.REJECTED, "Rejected because supporting documents were missing.");
    }

    private void audit(
            WorkflowAuditRepository auditRepository,
            WorkflowRequest workflow,
            AppUser user,
            WorkflowAction action,
            WorkflowStatus previousStatus,
            WorkflowStatus newStatus,
            String comments
    ) {
        auditRepository.save(new WorkflowAudit(workflow, user, action, previousStatus, newStatus, comments));
    }
}
