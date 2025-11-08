📱 AntiScam - Ứng dụng Phát hiện Cuộc gọi & Tin nhắn Lừa đảo
AntiScam là ứng dụng Android giúp người dùng phát hiện, cảnh báo và chặn các cuộc gọi hoặc tin nhắn có dấu hiệu lừa đảo. Ứng dụng hoạt động như ứng dụng gọi điện & nhắn tin mặc định, đồng thời phân tích dữ liệu để cảnh báo người dùng theo thời gian thực.
✨ Tính năng nổi bật
•	📞 Phát hiện cuộc gọi lừa đảo – Hiển thị cảnh báo khi có cuộc gọi từ số nghi ngờ.
•	💬 Phát hiện tin nhắn lừa đảo – Phân tích nội dung SMS và cảnh báo nếu chứa từ khóa nguy hiểm.
•	🚫 Chặn số điện thoại đáng ngờ – Cho phép người dùng chặn hoặc báo cáo số điện thoại.
•	🧠 Phân tích thông minh – Sử dụng mô hình AI hoặc danh sách cập nhật từ server để nhận diện lừa đảo.
•	🗂 Lưu lịch sử – Lưu trữ log cuộc gọi, tin nhắn và báo cáo người dùng bằng Room Database.
•	🔒 Bảo mật cao – Không thu thập dữ liệu cá nhân, chỉ lưu cục bộ trên thiết bị.
🏗️ Kiến trúc dự án

AntiScamApp/
│
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/com/example/antiscam/
│       │   │   ├── model/                 # Khai báo entity (CallLog, Message, Report)
│       │   │   ├── data/                  # DAO + Room Database
│       │   │   ├── ui/                    # Giao diện Jetpack Compose
│       │   │   ├── viewmodel/             # ViewModel xử lý logic
│       │   │   ├── repository/            # Kết nối Database & API
│       │   │   └── utils/                 # Hàm tiện ích (phân tích, regex, …)
│       │   └── res/                       # Icon, layout, màu sắc, strings
│       ├── AndroidManifest.xml
│       └── build.gradle
│
└── README.md

🧩 Công nghệ sử dụng
Thành phần	Mô tả
Kotlin	Ngôn ngữ chính của ứng dụng
Jetpack Compose	Xây dựng giao diện hiện đại và linh hoạt
Room Database	Lưu trữ dữ liệu cục bộ
ViewModel + StateFlow	Quản lý trạng thái và luồng dữ liệu
Retrofit (tùy chọn)	Gọi API để cập nhật danh sách số lừa đảo
Coroutines	Xử lý bất đồng bộ hiệu quả
🔐 Quyền cần thiết
•	READ_CALL_LOG, READ_PHONE_STATE, READ_CONTACTS
•	READ_SMS, RECEIVE_SMS, SEND_SMS
•	CALL_PHONE, ANSWER_PHONE_CALLS
•	INTERNET (nếu có đồng bộ dữ liệu online)
📅 Tiến độ thực hiện
Giai đoạn	Thời gian	Mô tả
Phân tích & thiết kế	30/10 - 01/11	Vẽ sơ đồ, xác định chức năng
Xây dựng giao diện Compose	02/11 - 10/11	Tạo UI cho Cuộc gọi & Tin nhắn
Tích hợp Room + ViewModel	11/11 - 16/11	Lưu trữ và hiển thị dữ liệu
Xử lý quyền & logic hệ thống	17/11 - 22/11	Quyền truy cập và cảnh báo lừa đảo
Kiểm thử & tối ưu	23/11 - 28/11	Test, fix bug và hoàn thiện báo cáo
👨‍💻 Tác giả
Đoàn Quốc Thông
Đại học Giao thông Vận tải
Sinh viên CNTT - Đam mê phát triển ứng dụng Android
Liên hệ: your.email@example.com
📄 Giấy phép
Phát hành theo giấy phép MIT License – bạn có thể sử dụng và chỉnh sửa tự do, miễn ghi nguồn gốc dự án.
