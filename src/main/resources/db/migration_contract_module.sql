-- =============================================================================
-- CYCLOSA HRM - MODULE 08: QUẢN LÝ HỢP ĐỒNG LAO ĐỘNG & HỢP ĐỒNG ĐIỆN TỬ
-- BẢNG DỮ LIỆU & DỮ LIỆU MẪU (CHUẨN BASEENTITY SOFT DELETE & POSTGRESQL UUID)
-- =============================================================================

-- 1. Bảng contract_templates (Mẫu biểu hợp đồng & thư mời)
CREATE TABLE IF NOT EXISTS contract_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    template_kind VARCHAR(30) NOT NULL DEFAULT 'LABOR_CONTRACT',
    contract_type VARCHAR(30),
    job_title_id UUID REFERENCES job_titles(id) ON DELETE SET NULL,
    department_id UUID REFERENCES departments(id) ON DELETE SET NULL,
    branch_id UUID REFERENCES branches(id) ON DELETE SET NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_contract_templates_company ON contract_templates(company_id);
CREATE INDEX IF NOT EXISTS idx_contract_templates_kind ON contract_templates(template_kind);
CREATE INDEX IF NOT EXISTS idx_contract_templates_type ON contract_templates(contract_type);

-- 2. Bảng contract_template_versions (Phiên bản tệp mẫu DOCX/HTML)
CREATE TABLE IF NOT EXISTS contract_template_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id UUID NOT NULL REFERENCES contract_templates(id) ON DELETE CASCADE,
    version_no INT NOT NULL,
    format VARCHAR(20) NOT NULL DEFAULT 'DOCX',
    file_url VARCHAR(500) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    CONSTRAINT uq_template_version UNIQUE (template_id, version_no)
);

CREATE INDEX IF NOT EXISTS idx_template_versions_template_id ON contract_template_versions(template_id);

-- 3. Bảng contracts (Hợp đồng lao động cốt lõi)
CREATE TABLE IF NOT EXISTS contracts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_no VARCHAR(50) NOT NULL UNIQUE,
    employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    employer_company_id UUID NOT NULL REFERENCES companies(id) ON DELETE RESTRICT,
    employer_representative_id UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT,
    template_version_id UUID REFERENCES contract_template_versions(id) ON DELETE SET NULL,
    contract_type VARCHAR(30) NOT NULL DEFAULT 'FIXED_TERM',
    previous_contract_id UUID REFERENCES contracts(id) ON DELETE SET NULL,
    source_offer_id UUID,
    job_title_id UUID NOT NULL REFERENCES job_titles(id) ON DELETE RESTRICT,
    department_id UUID NOT NULL REFERENCES departments(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    work_location VARCHAR(500) NOT NULL,
    working_time_description VARCHAR(500) NOT NULL,
    base_salary NUMERIC(18, 2) NOT NULL,
    insurance_salary NUMERIC(18, 2),
    payment_method VARCHAR(30) NOT NULL DEFAULT 'BANK_TRANSFER',
    pay_day_of_month SMALLINT NOT NULL DEFAULT 5,
    signing_form VARCHAR(30) NOT NULL DEFAULT 'ELECTRONIC',
    start_date DATE NOT NULL,
    end_date DATE,
    signed_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    archived_document_id UUID UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_contract_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX IF NOT EXISTS idx_contracts_no ON contracts(contract_no);
CREATE INDEX IF NOT EXISTS idx_contracts_employee_id ON contracts(employee_id);
CREATE INDEX IF NOT EXISTS idx_contracts_company_id ON contracts(employer_company_id);
CREATE INDEX IF NOT EXISTS idx_contracts_type ON contracts(contract_type);
CREATE INDEX IF NOT EXISTS idx_contracts_status ON contracts(status);
CREATE INDEX IF NOT EXISTS idx_contracts_start_date ON contracts(start_date);
CREATE INDEX IF NOT EXISTS idx_contracts_end_date ON contracts(end_date);

-- 4. Bảng contract_allowances (Phụ cấp thỏa thuận trong hợp đồng)
CREATE TABLE IF NOT EXISTS contract_allowances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_id UUID NOT NULL REFERENCES contracts(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    amount NUMERIC(18, 2) NOT NULL DEFAULT 0,
    is_taxable BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    CONSTRAINT uq_contract_allowance UNIQUE (contract_id, name)
);

CREATE INDEX IF NOT EXISTS idx_contract_allowances_contract_id ON contract_allowances(contract_id);

-- 5. Bảng contract_appendices (Phụ lục hợp đồng lao động)
CREATE TABLE IF NOT EXISTS contract_appendices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_id UUID NOT NULL REFERENCES contracts(id) ON DELETE CASCADE,
    appendix_no INT NOT NULL,
    template_version_id UUID REFERENCES contract_template_versions(id) ON DELETE SET NULL,
    effective_date DATE NOT NULL,
    description TEXT NOT NULL,
    new_base_salary NUMERIC(18, 2),
    new_insurance_salary NUMERIC(18, 2),
    new_job_title_id UUID REFERENCES job_titles(id) ON DELETE SET NULL,
    new_branch_id UUID REFERENCES branches(id) ON DELETE SET NULL,
    new_work_location VARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    archived_document_id UUID UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    CONSTRAINT uq_contract_appendix_no UNIQUE (contract_id, appendix_no)
);

CREATE INDEX IF NOT EXISTS idx_contract_appendices_contract ON contract_appendices(contract_id);
CREATE INDEX IF NOT EXISTS idx_contract_appendices_date ON contract_appendices(effective_date);

-- 6. Bảng contract_appendix_allowances (Phụ cấp điều chỉnh theo phụ lục)
CREATE TABLE IF NOT EXISTS contract_appendix_allowances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appendix_id UUID NOT NULL REFERENCES contract_appendices(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    amount NUMERIC(18, 2) NOT NULL DEFAULT 0,
    is_taxable BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    CONSTRAINT uq_appendix_allowance UNIQUE (appendix_id, name)
);

CREATE INDEX IF NOT EXISTS idx_appendix_allowances_appendix_id ON contract_appendix_allowances(appendix_id);

-- 7. Bảng contract_versions (Phiên bản văn bản tệp PDF hợp đồng/phụ lục)
CREATE TABLE IF NOT EXISTS contract_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_id UUID NOT NULL REFERENCES contracts(id) ON DELETE CASCADE,
    appendix_id UUID REFERENCES contract_appendices(id) ON DELETE CASCADE,
    version_no INT NOT NULL DEFAULT 1,
    file_url VARCHAR(500) NOT NULL,
    sha256_hash VARCHAR(64) NOT NULL,
    change_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_contract_versions_contract ON contract_versions(contract_id);
CREATE INDEX IF NOT EXISTS idx_contract_versions_appendix ON contract_versions(appendix_id);

-- 8. Bảng contract_signers (Các bên ký kết & Bằng chứng giao kết E-Contract)
CREATE TABLE IF NOT EXISTS contract_signers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_version_id UUID NOT NULL REFERENCES contract_versions(id) ON DELETE CASCADE,
    signer_employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT,
    signer_side VARCHAR(20) NOT NULL DEFAULT 'EMPLOYEE',
    signing_order INT NOT NULL DEFAULT 1,
    signing_method VARCHAR(30) NOT NULL DEFAULT 'OTP_CONFIRMATION',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    signed_at TIMESTAMP,
    decline_reason VARCHAR(500),
    otp_code VARCHAR(10),
    otp_expired_at TIMESTAMP,
    certificate_serial VARCHAR(100),
    certificate_issuer VARCHAR(200),
    certificate_validity VARCHAR(20),
    transaction_id VARCHAR(100) UNIQUE,
    timestamp_token TEXT,
    evidence_file_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    CONSTRAINT uq_signer_version_order UNIQUE (contract_version_id, signing_order),
    CONSTRAINT uq_signer_version_employee UNIQUE (contract_version_id, signer_employee_id)
);

CREATE INDEX IF NOT EXISTS idx_contract_signers_version ON contract_signers(contract_version_id);
CREATE INDEX IF NOT EXISTS idx_contract_signers_employee ON contract_signers(signer_employee_id);
CREATE INDEX IF NOT EXISTS idx_contract_signers_status ON contract_signers(status);

-- =============================================================================
-- THÊM DANH MỤC QUYỀN (PERMISSIONS) CHO MODULE 08: HỢP ĐỒNG LAO ĐỘNG
-- =============================================================================
INSERT INTO permissions (id, code, resource, action, description, created_at)
VALUES
    (gen_random_uuid(), 'contract.view', 'CONTRACT', 'VIEW', 'Xem danh sách và chi tiết hợp đồng lao động', CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'contract.create', 'CONTRACT', 'CREATE', 'Soạn thảo, điều chỉnh và trình duyệt hợp đồng', CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'contract.template_manage', 'CONTRACT', 'TEMPLATE_MANAGE', 'Quản trị danh mục mẫu biểu hợp đồng', CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'contract.sign_manage', 'CONTRACT', 'SIGN_MANAGE', 'Phát hành bản ký và xác nhận ký hợp đồng', CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'contract.archive', 'CONTRACT', 'ARCHIVE', 'Đồng bộ hồ sơ hợp đồng vào kho lưu trữ số', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Gán quyền cho SUPER_ADMIN
INSERT INTO role_permissions (role_id, permission_id, data_scope)
SELECT r.id, p.id, 'ALL'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'SUPER_ADMIN'
  AND p.code IN (
    'contract.view',
    'contract.create',
    'contract.template_manage',
    'contract.sign_manage',
    'contract.archive'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

