# Showroom Website

## Mô tả ngắn

Đây là một ứng dụng web showroom xây dựng bằng Java và Spring Boot. Ứng dụng cho phép hiển thị thông tin công ty, nội dung giới thiệu và danh sách sản phẩm. Người quản trị có thể đăng nhập, quản lý sản phẩm (thêm, sửa, xóa), cập nhật nội dung giới thiệu và thay đổi thông tin chung của công ty.

## Tech stack

- Java 25
- Spring Boot 4.0.3
- Thymeleaf làm template engine
- MySQL làm cơ sở dữ liệu
- JDBC cho truy vấn dữ liệu
- JWT cho xác thực người dùng bằng cookie
- HTML/CSS ở `src/main/resources/templates` và `src/main/resources/static`
- Lưu ảnh upload vào thư mục `uploads`

## Cấu trúc chính

- `src/main/java/com/nguyenthanhphuc/showroom`:
  - `controllers/`: quản lý các route web và xử lý form
  - `services/`: logic nghiệp vụ, validate dữ liệu
  - `repositories/`: lưu trữ và truy xuất dữ liệu MySQL, lưu file upload
  - `models/`: định nghĩa mô hình dữ liệu
- `src/main/resources/templates/`: các file Thymeleaf HTML
- `src/main/resources/static/`: CSS và tài nguyên tĩnh
- `uploads/`: lưu ảnh sản phẩm và ảnh feature upload

## Cách chạy project

1. Cài đặt MySQL và tạo database:
   - database name: `showroom`
   - user: `root`
   - password: `root`
   - host: `localhost`
   - port: `3306`

2. Kiểm tra cấu hình trong `src/main/resources/application.properties`:
   - `mysql.host=localhost`
   - `mysql.port=3306`
   - `mysql.username=root`
   - `mysql.password=root`
   - `mysql.database=showroom`
   - `uploadsFolder=uploads`

3. Chạy project với Maven wrapper:

```bash
./mvnw spring-boot:run
```

Trên Windows dùng:

```powershell
.\mvnw.cmd spring-boot:run
```

4. Mở trình duyệt và vào:

```text
http://localhost:8080
```

5. Đăng nhập quản trị:
   - Tài khoản và mật khẩu được kiểm tra trong bảng `users` của MySQL

## Ghi chú

- Ảnh sản phẩm được lưu vào `uploads/products` với định dạng JPG.
- Ứng dụng dùng cookie JWT để xác thực và hạn chế truy cập trang quản trị.
- Nếu chưa có bảng `general_info`, repository sẽ tạo bảng và giá trị mặc định tự động.
