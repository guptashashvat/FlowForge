CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(40) NOT NULL UNIQUE
);

CREATE TABLE app_users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(160) NOT NULL UNIQUE,
    full_name VARCHAR(160) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE workflow_requests (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(180) NOT NULL,
    description TEXT,
    created_by_id BIGINT NOT NULL REFERENCES app_users(id),
    assigned_to_id BIGINT REFERENCES app_users(id),
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE workflow_audits (
    id BIGSERIAL PRIMARY KEY,
    workflow_request_id BIGINT NOT NULL REFERENCES workflow_requests(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES app_users(id),
    action VARCHAR(60) NOT NULL,
    previous_status VARCHAR(40),
    new_status VARCHAR(40) NOT NULL,
    comments TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_app_users_email ON app_users(email);
CREATE INDEX idx_workflow_requests_status ON workflow_requests(status);
CREATE INDEX idx_workflow_requests_created_by ON workflow_requests(created_by_id);
CREATE INDEX idx_workflow_requests_assigned_to ON workflow_requests(assigned_to_id);
CREATE INDEX idx_workflow_requests_updated_at ON workflow_requests(updated_at);
CREATE INDEX idx_workflow_audits_request ON workflow_audits(workflow_request_id);
