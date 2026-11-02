-- =============================================================================
-- CYCLOSA HRM - MODULE 01: QUẢN LÝ TỔ CHỨC (ORGANIZATION MANAGEMENT)
-- BẢNG DỮ LIỆU & DỮ LIỆU SEED MẪU CHUẨN (CHUẨN BASEENTITY SOFT DELETE)
-- =============================================================================

-- 1. Bảng companies (Công ty / Pháp nhân)
CREATE TABLE IF NOT EXISTS companies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    tax_code VARCHAR(50),
    address VARCHAR(500),
    phone VARCHAR(20),
    email VARCHAR(100),
    logo_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_companies_code ON companies(code);

-- 2. Bảng regions (Khu vực / Vùng miền)
CREATE TABLE IF NOT EXISTS regions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_regions_company_id ON regions(company_id);
CREATE INDEX IF NOT EXISTS idx_regions_code ON regions(code);

-- 3. Bảng branches (Chi nhánh / Văn phòng làm việc)
CREATE TABLE IF NOT EXISTS branches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    region_id UUID NOT NULL REFERENCES regions(id) ON DELETE CASCADE,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(500),
    latitude NUMERIC(10, 7),
    longitude NUMERIC(10, 7),
    attendance_radius_meters INT NOT NULL DEFAULT 100,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_branches_region_id ON branches(region_id);
CREATE INDEX IF NOT EXISTS idx_branches_code ON branches(code);

-- 4. Bảng cost_centers (Trung tâm chi phí)
CREATE TABLE IF NOT EXISTS cost_centers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_cost_centers_company_id ON cost_centers(company_id);
CREATE INDEX IF NOT EXISTS idx_cost_centers_code ON cost_centers(code);

-- 5. Bảng job_titles (Chức danh công việc)
CREATE TABLE IF NOT EXISTS job_titles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_job_titles_company_id ON job_titles(company_id);
CREATE INDEX IF NOT EXISTS idx_job_titles_code ON job_titles(code);

-- 6. Bảng job_levels (Cấp bậc chức danh)
CREATE TABLE IF NOT EXISTS job_levels (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    rank_order INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_job_levels_company_id ON job_levels(company_id);
CREATE INDEX IF NOT EXISTS idx_job_levels_rank_order ON job_levels(rank_order);

-- 7. Bảng departments (Phòng ban / Đơn vị)
CREATE TABLE IF NOT EXISTS departments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    code VARCHAR(50) NOT NULL,
    dissolved_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_departments_company_id ON departments(company_id);
CREATE INDEX IF NOT EXISTS idx_departments_code ON departments(code);

-- 8. Bảng department_versions (Phiên bản cơ cấu phòng ban theo ngày hiệu lực)
CREATE TABLE IF NOT EXISTS department_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    department_id UUID NOT NULL REFERENCES departments(id) ON DELETE CASCADE,
    effective_from DATE NOT NULL,
    name VARCHAR(150) NOT NULL,
    parent_department_id UUID REFERENCES departments(id) ON DELETE SET NULL,
    cost_center_id UUID REFERENCES cost_centers(id) ON DELETE SET NULL,
    manager_employee_id UUID,
    is_permission_inherited BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_dept_vers_dept_id ON department_versions(department_id);
CREATE INDEX IF NOT EXISTS idx_dept_vers_effective ON department_versions(effective_from);
CREATE INDEX IF NOT EXISTS idx_dept_vers_parent ON department_versions(parent_department_id);

-- =============================================================================
-- SEED DATA MẪU CHO CÔNG TY MẸ VÀ CƠ CẤU TỔ CHỨC BAN ĐẦU
-- =============================================================================

-- Seed Công ty CYCLOSA
INSERT INTO companies (id, code, name, tax_code, address, phone, email, status)
VALUES (
    '00000000-0000-0000-0000-000000000001'::uuid,
    'CYC_CORP',
    'Công ty Cổ phần Tập đoàn CYCLOSA',
    '0109998888',
    'Tòa nhà Keangnam Landmark 72, Đường Phạm Hùng, Nam Từ Liêm, Hà Nội',
    '02473008888',
    'contact@cyclosa.vn',
    'ACTIVE'
) ON CONFLICT (code) DO NOTHING;

-- Seed Vùng miền
INSERT INTO regions (id, company_id, code, name)
VALUES 
    ('10000000-0000-0000-0000-000000000001'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'REG_NORTH', 'Khu vực Miền Bắc'),
    ('10000000-0000-0000-0000-000000000002'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'REG_SOUTH', 'Khu vực Miền Nam')
ON CONFLICT DO NOTHING;

-- Seed Chi nhánh
INSERT INTO branches (id, region_id, code, name, address, latitude, longitude, attendance_radius_meters, is_active)
VALUES 
    ('20000000-0000-0000-0000-000000000001'::uuid, '10000000-0000-0000-0000-000000000001'::uuid, 'BR_HN_HQ', 'Trụ sở chính Hà Nội', 'Tầng 25, Keangnam Landmark 72, Hà Nội', 21.0169380, 105.7836360, 150, TRUE),
    ('20000000-0000-0000-0000-000000000002'::uuid, '10000000-0000-0000-0000-000000000002'::uuid, 'BR_HCM_01', 'Chi nhánh Hồ Chí Minh', 'Tầng 12, Landmark 81, Vinhomes Central Park, TP.HCM', 10.7951230, 106.7218330, 150, TRUE)
ON CONFLICT DO NOTHING;

-- Seed Trung tâm chi phí
INSERT INTO cost_centers (id, company_id, code, name, description)
VALUES 
    ('30000000-0000-0000-0000-000000000001'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'CC_BOD', 'Khối Điều hành Ban Tổng Giám đốc', 'Ngân sách phục vụ chỉ đạo và điều hành cấp cao'),
    ('30000000-0000-0000-0000-000000000002'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'CC_TECH', 'Khối Công nghệ & Sản phẩm', 'Ngân sách R&D, hạ tầng máy chủ và phát triển phần mềm'),
    ('30000000-0000-0000-0000-000000000003'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'CC_HR_ADMIN', 'Khối Nhân sự & Hành chính', 'Ngân sách tuyển dụng, đào tạo, phúc lợi và hành chính văn phòng'),
    ('30000000-0000-0000-0000-000000000004'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'CC_FINANCE', 'Khối Tài chính Kế toán', 'Ngân sách quản trị tài chính, thuế và kế toán doanh nghiệp')
ON CONFLICT DO NOTHING;

-- Seed Cấp bậc chức danh
INSERT INTO job_levels (id, company_id, code, name, rank_order)
VALUES 
    ('40000000-0000-0000-0000-000000000001'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'LVL_01', 'Level 1 - Thực tập sinh (Intern)', 1),
    ('40000000-0000-0000-0000-000000000002'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'LVL_02', 'Level 2 - Nhân viên (Junior)', 2),
    ('40000000-0000-0000-0000-000000000003'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'LVL_03', 'Level 3 - Chuyên viên (Mid-level)', 3),
    ('40000000-0000-0000-0000-000000000004'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'LVL_04', 'Level 4 - Chuyên viên cao cấp (Senior)', 4),
    ('40000000-0000-0000-0000-000000000005'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'LVL_05', 'Level 5 - Trưởng nhóm (Lead / Team Leader)', 5),
    ('40000000-0000-0000-0000-000000000006'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'LVL_06', 'Level 6 - Trưởng phòng (Manager)', 6),
    ('40000000-0000-0000-0000-000000000007'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'LVL_07', 'Level 7 - Giám đốc khối (Director)', 7),
    ('40000000-0000-0000-0000-000000000008'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'LVL_08', 'Level 8 - Ban Giám đốc điều hành (C-Level)', 8)
ON CONFLICT DO NOTHING;

-- Seed Chức danh công việc
INSERT INTO job_titles (id, company_id, code, name, description)
VALUES 
    ('50000000-0000-0000-0000-000000000001'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'CEO', 'Tổng Giám đốc Điều hành', 'Chịu trách nhiệm toàn diện về hoạt động của tập đoàn'),
    ('50000000-0000-0000-0000-000000000002'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'CTO', 'Giám đốc Công nghệ', 'Định hướng chiến lược kiến trúc kỹ thuật và hạ tầng công nghệ'),
    ('50000000-0000-0000-0000-000000000003'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'CFO', 'Giám đốc Tài chính', 'Quản lý tài chính, phân bổ vốn và hạch toán kế toán'),
    ('50000000-0000-0000-0000-000000000004'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'CHRO', 'Giám đốc Nhân sự', 'Xây dựng văn hóa doanh nghiệp và chiến lược thu hút nhân tài'),
    ('50000000-0000-0000-0000-000000000005'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'DEV_LEAD', 'Trưởng nhóm Lập trình', 'Quản lý kỹ thuật và dẫn dắt đội ngũ kỹ sư phần mềm'),
    ('50000000-0000-0000-0000-000000000006'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'DEV_SR', 'Kỹ sư Phần mềm cao cấp', 'Thiết kế, xây dựng tính năng và tối ưu hóa hệ thống backend/frontend'),
    ('50000000-0000-0000-0000-000000000007'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'QA_LEAD', 'Trưởng nhóm Quản lý Chất lượng', 'Xây dựng quy trình kiểm thử và đảm bảo chất lượng phát hành'),
    ('50000000-0000-0000-0000-000000000008'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'HR_SPEC', 'Chuyên viên Nhân sự', 'Thực hiện công tác tuyển dụng, chấm công và chế độ phúc lợi')
ON CONFLICT DO NOTHING;

-- Seed Phòng ban (Departments)
INSERT INTO departments (id, company_id, code)
VALUES 
    ('60000000-0000-0000-0000-000000000001'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'DEPT_BOD'),
    ('60000000-0000-0000-0000-000000000002'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'DEPT_TECH'),
    ('60000000-0000-0000-0000-000000000003'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'DEPT_DEV'),
    ('60000000-0000-0000-0000-000000000004'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'DEPT_QA'),
    ('60000000-0000-0000-0000-000000000005'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'DEPT_HR'),
    ('60000000-0000-0000-0000-000000000006'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'DEPT_FIN')
ON CONFLICT DO NOTHING;

-- Seed Phiên bản hiệu lực phòng ban (Department Versions)
INSERT INTO department_versions (id, department_id, effective_from, name, parent_department_id, cost_center_id, is_permission_inherited)
VALUES 
    -- 1. Ban Tổng Giám đốc (Root)
    ('70000000-0000-0000-0000-000000000001'::uuid, '60000000-0000-0000-0000-000000000001'::uuid, '2026-01-01', 'Ban Tổng Giám đốc', NULL, '30000000-0000-0000-0000-000000000001'::uuid, TRUE),
    -- 2. Khối Công nghệ & Sản phẩm (con của BOD)
    ('70000000-0000-0000-0000-000000000002'::uuid, '60000000-0000-0000-0000-000000000002'::uuid, '2026-01-01', 'Khối Công nghệ & Sản phẩm', '60000000-0000-0000-0000-000000000001'::uuid, '30000000-0000-0000-0000-000000000002'::uuid, TRUE),
    -- 3. Phòng Phát triển Phần mềm (con của TECH)
    ('70000000-0000-0000-0000-000000000003'::uuid, '60000000-0000-0000-0000-000000000003'::uuid, '2026-01-01', 'Phòng Phát triển Phần mềm', '60000000-0000-0000-0000-000000000002'::uuid, '30000000-0000-0000-0000-000000000002'::uuid, TRUE),
    -- 4. Phòng Đảm bảo Chất lượng (con của TECH)
    ('70000000-0000-0000-0000-000000000004'::uuid, '60000000-0000-0000-0000-000000000004'::uuid, '2026-01-01', 'Phòng Đảm bảo Chất lượng (QA)', '60000000-0000-0000-0000-000000000002'::uuid, '30000000-0000-0000-0000-000000000002'::uuid, TRUE),
    -- 5. Ban Nhân sự & Hành chính (con của BOD)
    ('70000000-0000-0000-0000-000000000005'::uuid, '60000000-0000-0000-0000-000000000005'::uuid, '2026-01-01', 'Ban Nhân sự & Hành chính', '60000000-0000-0000-0000-000000000001'::uuid, '30000000-0000-0000-0000-000000000003'::uuid, TRUE),
    -- 6. Phòng Tài chính Kế toán (con của BOD)
    ('70000000-0000-0000-0000-000000000006'::uuid, '60000000-0000-0000-0000-000000000006'::uuid, '2026-01-01', 'Phòng Tài chính Kế toán', '60000000-0000-0000-0000-000000000001'::uuid, '30000000-0000-0000-0000-000000000004'::uuid, TRUE)
ON CONFLICT DO NOTHING;
