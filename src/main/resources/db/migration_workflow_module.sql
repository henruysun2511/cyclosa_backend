-- =============================================================================
-- MIGRATION: MODULE QUY TRÌNH PHÊ DUYỆT DÙNG CHUNG (APPROVAL WORKFLOW ENGINE)
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Định nghĩa Quy trình phê duyệt
CREATE TABLE IF NOT EXISTS workflow_definitions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    request_type VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_workflow_def_company ON workflow_definitions(company_id);
CREATE INDEX IF NOT EXISTS idx_workflow_def_type ON workflow_definitions(request_type);

-- 2. Bước định nghĩa của quy trình
CREATE TABLE IF NOT EXISTS workflow_definition_steps (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    definition_id UUID NOT NULL REFERENCES workflow_definitions(id) ON DELETE CASCADE,
    step_order INT NOT NULL CHECK (step_order >= 1),
    step_name VARCHAR(150) NOT NULL,
    approver_type VARCHAR(50) NOT NULL,
    approver_role_id UUID REFERENCES roles(id) ON DELETE SET NULL,
    approver_employee_id UUID REFERENCES employees(id) ON DELETE SET NULL,
    condition_type VARCHAR(50) NOT NULL DEFAULT 'ALWAYS',
    condition_threshold NUMERIC(18, 2),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_wf_def_step_order ON workflow_definition_steps(definition_id, step_order);

-- 3. Yêu cầu phê duyệt thực tế (Instance)
CREATE TABLE IF NOT EXISTS workflow_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    definition_id UUID NOT NULL REFERENCES workflow_definitions(id) ON DELETE RESTRICT,
    request_type VARCHAR(50) NOT NULL,
    reference_id UUID NOT NULL,
    reference_code VARCHAR(100),
    title VARCHAR(255) NOT NULL,
    requested_by_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    requester_employee_id UUID REFERENCES employees(id) ON DELETE SET NULL,
    request_amount NUMERIC(18, 2),
    metadata TEXT,
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    cancel_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_workflow_req_ref ON workflow_requests(reference_id, request_type);
CREATE INDEX IF NOT EXISTS idx_workflow_req_user ON workflow_requests(requested_by_user_id, submitted_at DESC);

-- 4. Bước phê duyệt chi tiết của yêu cầu
CREATE TABLE IF NOT EXISTS workflow_request_steps (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    request_id UUID NOT NULL REFERENCES workflow_requests(id) ON DELETE CASCADE,
    definition_step_id UUID NOT NULL REFERENCES workflow_definition_steps(id) ON DELETE RESTRICT,
    step_order INT NOT NULL,
    step_name VARCHAR(150) NOT NULL,
    assignee_employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT,
    decision VARCHAR(30),
    decided_at TIMESTAMP WITH TIME ZONE,
    comment TEXT,
    is_current BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_wf_req_step_assignee ON workflow_request_steps(assignee_employee_id, is_current);
CREATE INDEX IF NOT EXISTS idx_wf_req_step_order ON workflow_request_steps(request_id, step_order);

-- 5. Ủy quyền phê duyệt (Delegations)
CREATE TABLE IF NOT EXISTS workflow_delegations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    delegator_employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    delegatee_employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT,
    request_type VARCHAR(50),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_wf_delegation_delegator ON workflow_delegations(delegator_employee_id, is_active);
CREATE INDEX IF NOT EXISTS idx_wf_delegation_dates ON workflow_delegations(start_date, end_date);

