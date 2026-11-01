-- =============================================================================
-- CYCLOSA HRM - HỆ THỐNG QUẢN TRỊ NGUỒN NHÂN LỰC DOANH NGHIỆP
-- SCRIPT RESET, KHỞI TẠO CẤU TRÚC BẢNG MỚI & NẠP VAI TRÒ - PHÂN QUYỀN CHUẨN
-- Ngày thực hiện: 2026-10-30
-- =============================================================================

-- 1. DROP SCHEMA VÀ KHỞI TẠO LẠI SCHEMA PUBLIC SẠCH SẼ
DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO neondb_owner;
GRANT ALL ON SCHEMA public TO public;

-- 2. TẠO CÁC BẢNG THEO CHUẨN JPA ENTITIES (MODULAR MONOLITH)

-- Bảng users (Tài khoản người dùng hệ thống)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_ACTIVATION',
    last_login_at TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_deleted ON users(deleted);

-- Bảng oauth_accounts (Tài khoản liên kết Google/OAuth2)
CREATE TABLE oauth_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(30) NOT NULL,
    provider_user_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_oauth_provider_user UNIQUE (provider, provider_user_id)
);
CREATE INDEX idx_oauth_user_id ON oauth_accounts(user_id);
CREATE INDEX idx_oauth_provider_user ON oauth_accounts(provider, provider_user_id);

-- Bảng roles (Danh mục vai trò người dùng)
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_system_role BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_roles_code ON roles(code);
CREATE INDEX idx_roles_company_id ON roles(company_id);
CREATE INDEX idx_roles_deleted ON roles(deleted);

-- Bảng permissions (Danh mục quyền chức năng resource.action)
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_permissions_code ON permissions(code);
CREATE INDEX idx_permissions_resource ON permissions(resource);

-- Bảng role_permissions (Ma trận phân quyền vai trò x DataScope)
CREATE TABLE role_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    data_scope VARCHAR(30) NOT NULL DEFAULT 'OWN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_role_permissions_role_perm UNIQUE (role_id, permission_id)
);
CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

-- Bảng user_role_assignments (Gán vai trò cho người dùng theo từng pháp nhân công ty)
CREATE TABLE user_role_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    company_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_user_role_company UNIQUE (user_id, role_id, company_id)
);
CREATE INDEX idx_user_role_assignments_user ON user_role_assignments(user_id);
CREATE INDEX idx_user_role_assignments_role ON user_role_assignments(role_id);
CREATE INDEX idx_user_role_assignments_company ON user_role_assignments(company_id);

-- Bảng role_document_type_permissions (Phân quyền truy cập hồ sơ tài liệu số)
CREATE TABLE role_document_type_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    document_type_id UUID NOT NULL,
    action VARCHAR(30) NOT NULL,
    data_scope VARCHAR(30) NOT NULL DEFAULT 'OWN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_role_doc_type_perms_role ON role_document_type_permissions(role_id);
CREATE INDEX idx_role_doc_type_perms_doc_type ON role_document_type_permissions(document_type_id);

-- Bảng products (Sản phẩm mẫu phục vụ kiểm thử CRUD & kiến trúc chuẩn)
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    sku VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(100) NOT NULL,
    price NUMERIC(15, 2) NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    description TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_deleted ON products(deleted);

-- =============================================================================
-- 3. NẠP DỮ LIỆU BAN ĐẦU (SEED DATA)
-- =============================================================================

-- 3.1. Danh mục 36 Quyền chức năng hệ thống
INSERT INTO permissions (resource, action, code, description) VALUES
-- USER
('USER', 'VIEW', 'user.view', 'Xem danh sách và chi tiết người dùng'),
('USER', 'CREATE', 'user.create', 'Tạo mới tài khoản người dùng'),
('USER', 'UPDATE', 'user.update', 'Cập nhật thông tin tài khoản'),
('USER', 'DELETE', 'user.delete', 'Khóa / Xóa tài khoản người dùng'),
('USER', 'ACTIVATE', 'user.activate', 'Kích hoạt tài khoản nhân viên'),

-- ROLE
('ROLE', 'VIEW', 'role.view', 'Xem danh sách vai trò và ma trận quyền'),
('ROLE', 'MANAGE', 'role.manage', 'Tạo, cập nhật và thiết lập quyền cho vai trò'),
('ROLE', 'ASSIGN', 'role.assign', 'Gán hoặc thu hồi vai trò của người dùng'),

-- ORGANIZATION
('ORG', 'VIEW', 'org.view', 'Xem cơ cấu tổ chức và sơ đồ phòng ban'),
('ORG', 'MANAGE', 'org.manage', 'Quản lý cơ cấu tổ chức, phòng ban, chi nhánh'),
('ORG', 'SIMULATE', 'org.simulate', 'Mô phỏng tái cơ cấu tổ chức'),

-- EMPLOYEE
('EMPLOYEE', 'VIEW', 'employee.view', 'Xem thông tin hồ sơ nhân viên'),
('EMPLOYEE', 'CREATE', 'employee.create', 'Thêm mới hồ sơ nhân sự'),
('EMPLOYEE', 'UPDATE', 'employee.update', 'Cập nhật hồ sơ nhân sự'),
('EMPLOYEE', 'DELETE', 'employee.delete', 'Xóa / Lưu trữ hồ sơ nhân sự'),

-- LEAVE
('LEAVE', 'VIEW', 'leave.view', 'Xem đơn nghỉ phép'),
('LEAVE', 'APPLY', 'leave.apply', 'Tạo và gửi đơn xin nghỉ phép'),
('LEAVE', 'APPROVE', 'leave.approve', 'Phê duyệt hoặc từ chối đơn nghỉ phép'),
('LEAVE', 'MANAGE', 'leave.manage', 'Quản trị cấu hình và quỹ phép doanh nghiệp'),

-- ATTENDANCE
('ATTENDANCE', 'VIEW', 'attendance.view', 'Xem dữ liệu chấm công'),
('ATTENDANCE', 'CHECKIN', 'attendance.checkin', 'Thực hiện chấm công hàng ngày'),
('ATTENDANCE', 'MANAGE', 'attendance.manage', 'Chốt ca, đối soát và điều chỉnh chấm công'),

-- PAYROLL
('PAYROLL', 'VIEW', 'payroll.view', 'Xem bảng tính lương tổng hợp'),
('PAYROLL', 'PROCESS', 'payroll.process', 'Tính toán, phê duyệt và chi trả lương'),
('PAYROLL', 'VIEW_SALARY', 'payroll.view_salary', 'Xem chi tiết mức lương và thu nhập nhân sự'),

-- CONTRACT
('CONTRACT', 'VIEW', 'contract.view', 'Xem hợp đồng lao động'),
('CONTRACT', 'CREATE', 'contract.create', 'Tạo mới và gia hạn hợp đồng'),
('CONTRACT', 'SIGN', 'contract.sign', 'Ký duyệt hợp đồng lao động'),

-- RECRUITMENT
('RECRUITMENT', 'VIEW', 'recruitment.view', 'Xem tin tuyển dụng và hồ sơ ứng viên'),
('RECRUITMENT', 'CREATE', 'recruitment.create', 'Đăng tin tuyển dụng mới'),
('RECRUITMENT', 'MANAGE', 'recruitment.manage', 'Điều phối ứng viên, phỏng vấn và đánh giá'),

-- PERFORMANCE
('PERFORMANCE', 'VIEW', 'performance.view', 'Xem đánh giá hiệu suất và KPI'),
('PERFORMANCE', 'EVALUATE', 'performance.evaluate', 'Thiết lập và chấm điểm hiệu suất'),

-- WORKPLACE
('WORKPLACE', 'VIEW', 'workplace.view', 'Xem sơ đồ mặt bằng và chỗ ngồi'),
('WORKPLACE', 'MANAGE', 'workplace.manage', 'Quản lý cơ sở vật chất và cấp phát tài sản'),

-- AUDIT
('AUDIT', 'VIEW', 'audit.view', 'Xem nhật ký kiểm toán và báo cáo tuân thủ');

-- 3.2. Danh mục 10 Vai trò chuẩn hệ thống
INSERT INTO roles (code, name, description, is_system_role) VALUES
('SUPER_ADMIN', 'Super Admin', 'Quản trị cao nhất toàn hệ thống, cấu hình nền tảng, phân quyền tenant, cấu hình kỹ thuật', TRUE),
('HR_ADMIN', 'HR Admin / HR Manager', 'Trưởng phòng Nhân sự, toàn quyền quản trị nghiệp vụ HR trong phạm vi công ty', TRUE),
('HR_SPECIALIST', 'HR Specialist', 'Chuyên viên Nhân sự thực thi nghiệp vụ (Hồ sơ, Hợp đồng, C&B, Đào tạo, Khen thưởng - Kỷ luật...)', TRUE),
('RECRUITER', 'Recruiter', 'Chuyên viên tuyển dụng, quản lý tin đăng, nguồn ứng viên, điều phối phỏng vấn (không xem lương C&B)', TRUE),
('PAYROLL_ACCOUNTANT', 'Payroll / Accountant', 'Kế toán tiền lương, đối soát bảng lương, bảo hiểm, quyết toán thuế TNCN', TRUE),
('EXECUTIVE', 'Executive / BOD', 'Ban Lãnh đạo, xem dashboard BI tổng thể và báo cáo chiến lược', TRUE),
('MANAGER', 'Manager', 'Quản lý và phê duyệt đơn từ, đề xuất nhân sự, đánh giá KPI theo phạm vi đơn vị', TRUE),
('EMPLOYEE', 'Employee', 'Nhân viên (Chính thức/Thử việc/Thực tập), sử dụng cổng tự phục vụ nhân viên (ESS)', TRUE),
('OFFICE_ADMIN', 'Office Admin / IT & Facility', 'Quản trị cơ sở vật chất, mặt bằng, sơ đồ chỗ ngồi và cấp phát/thu hồi tài sản công nghệ', TRUE),
('AUDITOR', 'Auditor / Compliance', 'Kiểm toán nội bộ, giám sát Audit Logs, rà soát tính hợp lệ quy trình độc lập', TRUE);

-- 3.3. Ma trận Phân quyền & DataScope cho từng vai trò

-- SUPER_ADMIN: Toàn quyền ALL
INSERT INTO role_permissions (role_id, permission_id, data_scope)
SELECT r.id, p.id, 'ALL'
FROM roles r CROSS JOIN permissions p
WHERE r.code = 'SUPER_ADMIN';

-- HR_ADMIN: COMPANY
INSERT INTO role_permissions (role_id, permission_id, data_scope)
SELECT r.id, p.id, 'COMPANY'
FROM roles r JOIN permissions p ON p.code IN (
    'user.view', 'user.create', 'user.update', 'user.activate',
    'role.view', 'role.manage', 'role.assign',
    'org.view', 'org.manage',
    'employee.view', 'employee.create', 'employee.update', 'employee.delete',
    'leave.view', 'leave.approve', 'leave.manage',
    'attendance.view', 'attendance.manage',
    'contract.view', 'contract.create', 'contract.sign',
    'performance.view', 'performance.evaluate',
    'recruitment.view'
)
WHERE r.code = 'HR_ADMIN';

-- HR_SPECIALIST: COMPANY
INSERT INTO role_permissions (role_id, permission_id, data_scope)
SELECT r.id, p.id, 'COMPANY'
FROM roles r JOIN permissions p ON p.code IN (
    'employee.view', 'employee.create', 'employee.update',
    'contract.view', 'contract.create',
    'leave.view', 'leave.approve',
    'attendance.view', 'attendance.manage',
    'org.view'
)
WHERE r.code = 'HR_SPECIALIST';

-- RECRUITER: COMPANY
INSERT INTO role_permissions (role_id, permission_id, data_scope)
SELECT r.id, p.id, 'COMPANY'
FROM roles r JOIN permissions p ON p.code IN (
    'recruitment.view', 'recruitment.create', 'recruitment.manage',
    'employee.view'
)
WHERE r.code = 'RECRUITER';

-- PAYROLL_ACCOUNTANT: COMPANY
INSERT INTO role_permissions (role_id, permission_id, data_scope)
SELECT r.id, p.id, 'COMPANY'
FROM roles r JOIN permissions p ON p.code IN (
    'payroll.view', 'payroll.process', 'payroll.view_salary',
    'attendance.view',
    'employee.view',
    'contract.view'
)
WHERE r.code = 'PAYROLL_ACCOUNTANT';

-- EXECUTIVE: ALL (Chỉ xem tổng thể)
INSERT INTO role_permissions (role_id, permission_id, data_scope)
SELECT r.id, p.id, 'ALL'
FROM roles r JOIN permissions p ON p.code IN (
    'employee.view', 'org.view', 'payroll.view', 'attendance.view',
    'leave.view', 'performance.view', 'audit.view'
)
WHERE r.code = 'EXECUTIVE';

-- MANAGER: DEPARTMENT
INSERT INTO role_permissions (role_id, permission_id, data_scope)
SELECT r.id, p.id, 'DEPARTMENT'
FROM roles r JOIN permissions p ON p.code IN (
    'employee.view',
    'leave.view', 'leave.approve',
    'attendance.view',
    'performance.view', 'performance.evaluate',
    'org.view'
)
WHERE r.code = 'MANAGER';

-- EMPLOYEE: OWN
INSERT INTO role_permissions (role_id, permission_id, data_scope)
SELECT r.id, p.id, 'OWN'
FROM roles r JOIN permissions p ON p.code IN (
    'employee.view',
    'leave.view', 'leave.apply',
    'attendance.view', 'attendance.checkin',
    'payroll.view',
    'performance.view'
)
WHERE r.code = 'EMPLOYEE';

-- OFFICE_ADMIN: COMPANY
INSERT INTO role_permissions (role_id, permission_id, data_scope)
SELECT r.id, p.id, 'COMPANY'
FROM roles r JOIN permissions p ON p.code IN (
    'workplace.view', 'workplace.manage',
    'employee.view', 'org.view'
)
WHERE r.code = 'OFFICE_ADMIN';

-- AUDITOR: ALL
INSERT INTO role_permissions (role_id, permission_id, data_scope)
SELECT r.id, p.id, 'ALL'
FROM roles r JOIN permissions p ON p.code IN (
    'audit.view', 'employee.view', 'contract.view', 'payroll.view', 'role.view'
)
WHERE r.code = 'AUDITOR';

-- 3.4. Tài khoản Super Admin mặc định
INSERT INTO users (id, username, email, password_hash, status, created_at)
VALUES (
    '00000000-0000-0000-0000-000000000001'::uuid,
    'admin',
    'admin@cyclosa.vn',
    '$2a$10$Yalk.UGP0gY5/LJHU7rP6.VDQKXFglVJIqkGWaw0qWH/9xbPBuefO',
    'ACTIVE',
    CURRENT_TIMESTAMP
);

-- Gán quyền SUPER_ADMIN cho admin@cyclosa.vn
INSERT INTO user_role_assignments (user_id, role_id, company_id, created_at)
SELECT 
    '00000000-0000-0000-0000-000000000001'::uuid,
    r.id,
    '00000000-0000-0000-0000-000000000001'::uuid,
    CURRENT_TIMESTAMP
FROM roles r
WHERE r.code = 'SUPER_ADMIN';

-- 3.5. Sản phẩm mẫu kiểm thử CRUD
INSERT INTO products (name, sku, category, price, stock_quantity, description, status) VALUES
('Phần mềm Quản lý Chấm công AI', 'PRD-AI-ATT-001', 'SOFTWARE', 15000000.00, 50, 'Gói phần mềm chấm công nhận diện khuôn mặt AI', 'ACTIVE'),
('Thiết bị Chấm công Vân tay & FaceID', 'DEV-FINGER-002', 'HARDWARE', 4500000.00, 120, 'Máy chấm công quang học hỗ trợ kết nối LAN/WiFi', 'ACTIVE'),
('Thẻ nhân viên thông minh RFID', 'ACC-CARD-003', 'ACCESSORY', 25000.00, 2000, 'Thẻ NFC/RFID tần số 13.56MHz tích hợp ra vào cửa', 'ACTIVE');

